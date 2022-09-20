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
import java.io.IOException;

/**
 * This class represents the File Name Table
 */
class FNT {

	/**
	 * Write the FNT section in the ROM
	 *
	 * @param rom ROM binary stream
	 * @param root Root nitro directory of the ROM
	 * @throws IOException
	 */
	public static void writeFNT(DataIOStream rom, NitroDirectory root) throws IOException {
		if (root.id == 0xf000) {
			DataIOStream fntMainTable = new DataIOStream(new byte[(getDirectoryCount(root) + 1) * 8]);
			DataIOStream fntSubTable = new DataIOStream(new byte[getSubTableSize(root)]);

			// Write the first main table entry
			fntMainTable.writeInt(fntMainTable.getLength()); // Relative offset to the first sub table item
			fntMainTable.writeShort(getFirstFileID(root)); // The first file ID we'll encounter
			fntMainTable.writeShort(getDirectoryCount(root) + 1); // Total number of directories (root included)

			writeFNT(root, fntMainTable, fntSubTable);
			rom.write(fntMainTable.toByteArray());
			rom.write(fntSubTable.toByteArray());
		} else {
			throw new IOException("This is not the root directory!");
		}
	}

	/**
	 * Pre-calculate the size of the FNT section starting from a path
	 *
	 * @param path Path of the folder to calculate the FNT
	 * @return Size in bytes of the FNT section
	 */
	public static int calculateFNTSize(FSFile path) {
		return (getDirectoryCount(path) + 1) * 8 + getSubTableSize(path);
	}

	/**
	 * Recursively write FNT sections
	 *
	 * @param currentDir Current sub-directory
	 * @throws IOException
	 */
	private static void writeFNT(NitroDirectory currentDir, DataIOStream fntMainTable, DataIOStream fntSubTable) throws IOException {
		for (NitroFile f : currentDir.fileList) {
			fntSubTable.write(f.name.length());
			fntSubTable.writeStringUnterminated(f.name);
		}
		for (NitroDirectory d : currentDir.directoryList) {
			fntSubTable.write(d.name.length() | (1 << 7));
			fntSubTable.writeStringUnterminated(d.name);
			fntSubTable.writeShort(d.id);
		}
		fntSubTable.write(0);
		for (NitroDirectory d : currentDir.directoryList) {
			fntMainTable.writeInt(fntMainTable.getLength() + fntSubTable.getPosition());
			fntMainTable.writeShort(getFirstFileID(d));
			fntMainTable.writeShort(d.parent.id);
			writeFNT(d, fntMainTable, fntSubTable);
		}
	}

	/**
	 * Calculate the number of directories of the directory tree
	 *
	 * @param d The root directory from where we start counting
	 * @return Number of directories
	 */
	private static int getDirectoryCount(NitroDirectory d) {
		int n = d.directoryList.size();
		for (NitroDirectory t : d.directoryList) {
			n += getDirectoryCount(t);
		}
		return n;
	}

	/**
	 * Calculate the number of directories of the directory tree
	 *
	 * @param path The root directory from where we start counting
	 * @return Number of directories
	 */
	private static int getDirectoryCount(FSFile path) {
		int count = 0;
		for (FSFile f : path.listFiles()) {
			if (f.isDirectory()) {
				count++;
				count += getDirectoryCount(f);
			}
		}
		return count;
	}

	/**
	 * Find the ID of the first file in the directory tree
	 *
	 * @param d The root directory where to start searching
	 * @return ID of the first file found
	 */
	private static int getFirstFileID(NitroDirectory d) {
		if (d.fileList.size() > 0) {
			return d.fileList.get(0).id;
		} else if (d.directoryList.size() > 0) {
			return getFirstFileID(d.directoryList.get(0));
		}
		return -1;
	}

	/**
	 * Calculate the size of the FNT sub table
	 *
	 * @param current The root directory
	 * @return Size of FNT sub table
	 */
	private static int getSubTableSize(NitroDirectory current) {
		int a = 0;
		for (NitroDirectory d : current.directoryList) {
			a += d.name.length() + 3;
		}
		for (NitroFile f : current.fileList) {
			a += f.name.length() + 1;
		}
		a += 1;
		for (NitroDirectory d : current.directoryList) {
			a += getSubTableSize(d);
		}
		return a;
	}

	/**
	 * Calculate the size of the FNT sub table
	 *
	 * @param path The root directory
	 * @return Size of FNT sub table
	 */
	private static int getSubTableSize(FSFile path) {
		int a = 0;
		for (FSFile child : path.listFiles()) {
			if (child.isDirectory()) {
				a += child.getName().length() + 3;
				a += getSubTableSize(child);
			} else {
				a += child.getName().length() + 1;
			}
		}
		return a;
	}
}
