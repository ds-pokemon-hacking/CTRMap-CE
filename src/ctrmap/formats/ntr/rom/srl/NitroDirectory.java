/*
 * This file is part of jNdstool.
 *
 * jNdstool is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jNdstool. If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2020-2021 JackHack96, Hello007
 */
package ctrmap.formats.ntr.rom.srl;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.math.MathEx;
import java.io.IOException;
import java.util.*;

/**
 * This class represents a folder of a Nitro file system
 */
public class NitroDirectory implements Comparable<NitroDirectory> {

	public final String name; // Directory name
	public final int id; // Directory ID
	public final NitroDirectory parent; // Parent directory
	public final List<NitroFile> fileList; // The list of directory's files
	public final List<NitroDirectory> directoryList; // The list of subdirectories

	public NitroDirectory(String name, int id, NitroDirectory parent) {
		this.name = name;
		this.id = id;
		this.parent = parent;
		this.fileList = new ArrayList<>();
		this.directoryList = new ArrayList<>();
	}

	@Override
	public String toString() {
		return "NitroDirectory{"
			+ "name='" + name + '\''
			+ ", id=" + id
			+ '}';
	}

	@Override
	public int compareTo(NitroDirectory nitroDirectory) {
		return (name.compareToIgnoreCase(nitroDirectory.name));
	}

	/**
	 * Recursively load the FNT structure
	 *
	 * @param parent The current parent directory
	 * @param stream FNT source stream
	 * @param origin Initial stream offset
	 * @throws IOException If a file is corrupted or something is wrong
	 */
	public static void loadDir(NitroDirectory parent, DataIOStream stream, int origin, Map<Integer, Integer> startOffset, Map<Integer, Integer> endOffset) throws IOException {
		stream.checkpoint();
		stream.seek(origin + (8 * (parent.id & 0xfff))); // Go the the FNT main table entry

		int subTableOffset = stream.readInt();
		int firstFileID = stream.readShort();

		stream.seek(origin + subTableOffset); // Go to the FNT sub table entry

		int header; // The header tells us if the entry is a file or a parent, and then it tells us the name size

		while (((header = stream.read()) & 0x7f) != 0) { // Until we found a 0x00 or a 0xff header, we can go further
			String name = stream.readPaddedString(header & 0x7f); // This could be a parent name or a file name
			if (header > 0x7f) { // This is a directory
				int newID = stream.readShort(); // This will be the next parent ID
				NitroDirectory newDirectory = new NitroDirectory(name, newID, parent);
				parent.directoryList.add(newDirectory);
				loadDir(newDirectory, stream, origin, startOffset, endOffset);
			} else { // This is a file
				parent.fileList.add(
					new NitroFile(
						name,
						firstFileID,
						startOffset.get(firstFileID),
						endOffset.get(firstFileID) - startOffset.get(firstFileID),
						parent
					)
				);
				firstFileID++;
			}
		}
		stream.resetCheckpoint();
	}

	/**
	 * Recursively construct the NitroDirectory structure
	 *
	 * @param currentPath The current host file system path
	 * @param parent The current parent directory
	 */
	public static void loadDir(FSFile currentPath, NitroDirectory parent, FSLoaderState state) {
		List<? extends FSFile> allFiles = currentPath.listFiles();

		List<FSFile> dirList = new ArrayList<>();
		List<FSFile> fileList = new ArrayList<>();

		for (FSFile f : allFiles) {
			if (f.isDirectory()) {
				dirList.add(f);
			} else {
				fileList.add(f);
			}
		}

		if (!fileList.isEmpty()) {
			Collections.sort(fileList, (FSFile o1, FSFile o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
			for (FSFile file : fileList) {
				parent.fileList.add(new NitroFile(file.getName(), state.file, state.offset, file.length(), parent));
				state.file++;
				state.offset += MathEx.padInteger(file.length(), NDSROM.CARTRIDGE_OPTIMAL_ALIGNMENT);
			}
		}
		// it's important to sort the file lists alphabetically
		if (!dirList.isEmpty()) {
			Collections.sort(dirList, (FSFile o1, FSFile o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
			for (FSFile dir : dirList) {
				state.dir++;
				NitroDirectory newDirectory = new NitroDirectory(dir.getName(), state.dir, parent);
				parent.directoryList.add(newDirectory);
				if (dir.getChildCount() > 0) {
					loadDir(dir, newDirectory, state);
				}
			}
		}
	}

	/**
	 * Recursively unpack the files of the ROM
	 *
	 * @param currentDir The path to use for creating the tree
	 * @param rootDir The current root directory
	 * @throws IOException If a file is corrupted or something is wrong
	 */
	public static void unpackFileTree(DataIOStream rom, FSFile currentDir, NitroDirectory rootDir) throws IOException {
		// we scan for directories first, thus exploring a path in depth as in DFS algorithm
		for (NitroDirectory d : rootDir.directoryList) {
			FSFile target = currentDir.getChild(d.name);
			target.mkdirs();
			unpackFileTree(rom, target, d);
		}
		// then whenever we reach the end of a path we unpack the files
		for (NitroFile f : rootDir.fileList) {
			FSFile target = currentDir.getChild(f.name);
			if (!target.exists()) {
				rom.seek(f.offset);
				target.setBytes(rom.readBytes(f.size));
			}
		}
	}

	/**
	 * Recursively repack the files in the ROM
	 *
	 * @param rom BinaryWriter stream of the .nds ROM
	 * @param currentDir Current path
	 * @param rootDir The current root directory
	 * @throws IOException If a file is corrupted or something is wrong
	 */
	public static void repackFileTree(DataIOStream rom, int ofsBase, FSFile currentDir, NitroDirectory rootDir) throws IOException {

		// then whenever we reach the end of a path we unpack the files
		for (NitroFile f : rootDir.fileList) {
			FSFile source = currentDir.getChild(f.name);
			if (source.exists()) {
				if (f.offset + ofsBase != rom.getPosition()) {
					System.out.println("WARNING! " + f + " real offset differs from assumed one! Assumed: "
						+ (f.offset + ofsBase) + " Real: " + rom.getPosition());
					f.offset = rom.getPosition();
				}
				//System.out.println("writing file " + source);
				rom.write(source.getBytes());
				// padding with 0xff for 4-byte alignment
				// edit: pad to 512 bytes for faster flash memory speed
				rom.pad(NDSROM.CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);
			} else {
				throw new IOException(f.name + " file does not exist");
			}
		}
		// we scan for directories first, thus exploring a path in depth as in DFS algorithm
		for (NitroDirectory d : rootDir.directoryList) {
			repackFileTree(rom, ofsBase, currentDir.getChild(d.name), d);
		}
	}

	public static class FSLoaderState {

		public int dir;
		public int file;
		public int offset;

		public FSLoaderState(int dir, int file, int offset) {
			this.dir = dir;
			this.file = file;
			this.offset = offset;
		}
	}
}
