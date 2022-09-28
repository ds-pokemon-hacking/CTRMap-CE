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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import ctrmap.formats.ntr.rom.OverlayTable;
import ctrmap.formats.ntr.rom.srl.newlib.SRLHeader;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *	Nintendo DS ROM extractor/rebuilder.
 *
 *	This class is largely a fork of jNDSTool, adapted for use with CTRMapStandardLibrary's faster IO, FS and crypto.
 * 
 *	Other improvements include:
 *		- Automatic overlay table updates
 *		- Support for normal overlay table names (y7.bin, y9.bin)
 *		- Correct checksum calculation
 *		- Resource leak fixes
 */
public class NDSROM {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nintendo DS ROM", "*.nds");
	
	/**
	 * Extract the entire NDSROM in the host file system
	 *
	 * @param romPath The path of the .nds file
	 * @param dirPath The path where to extract files
	 * @throws IOException If something goes wrong
	 */
	public static void extractROM(FSFile romPath, FSFile dirPath) throws IOException {
		dirPath.mkdirs();
		if (!dirPath.canWrite()) // If we can't read or write, we don't own the directory
		{
			throw new IOException("Can't write in the directory! Check permissions!");
		}

		DataIOStream rom = romPath.getDataIOStream();
		NitroDirectory root = new NitroDirectory("data", 0xf000, null);
		SRLHeader header = new SRLHeader(rom);
		Map<Integer, Integer> startOffset = new HashMap<>(); // The NDSROM's files start offset
		Map<Integer, Integer> endOffset = new HashMap<>(); // The NDSROM's files end offsets

		rom.seek(header.fatOffset);

		for (int i = 0; i < (header.fatSize >> 3); i++) {
			startOffset.put(i, rom.readInt());
			endOffset.put(i, rom.readInt());
		}

		// Load the directory structure
		rom.seek(header.fntOffset);
		NitroDirectory.loadDir(root, rom, rom.getPosition(), startOffset, endOffset);

		// Let's create the directory tree
		FSFile dataDir = dirPath.getChild("data");
		dataDir.mkdirs();
		NitroDirectory.unpackFileTree(rom, dataDir, root);

		// The overlays
		FSFile ovlDir = dirPath.getChild("overlay");
		ovlDir.mkdirs();

		int arm9OvSize = (header.arm9OverlaySize / OverlayTable.OverlayInfo.BYTES);
		for (int i = 0; i < arm9OvSize; i++) {
			FSFile ovlFile = ovlDir.getChild(String.format("overlay_%04d.bin", i));

			if (!ovlFile.exists()) {
				rom.seek(startOffset.get(i));
				ovlFile.setBytes(rom.readBytes(endOffset.get(i) - startOffset.get(i)));
			}
		}
		int arm7OvSize = (header.arm7OverlaySize / OverlayTable.OverlayInfo.BYTES);
		for (int i = 0; i < arm7OvSize; i++) {
			FSFile ovlFile = ovlDir.getChild(String.format("overlay_%04d.bin", i + arm9OvSize));

			if (!ovlFile.exists()) {
				rom.seek(startOffset.get(i));
				ovlFile.setBytes(rom.readBytes(endOffset.get(i + arm9OvSize) - startOffset.get(i + arm9OvSize)));
			}
		}

		// The header and the two arms
		FSFile headerBin = dirPath.getChild("header.bin");
		if (!headerBin.exists()) {
			rom.seek(0);
			headerBin.setBytes(rom.readBytes(0x200));
		}

		FSFile arm9Bin = dirPath.getChild("arm9.bin");
		if (!arm9Bin.exists()) {
			rom.seek(header.arm9RomOffset);
			arm9Bin.setBytes(rom.readBytes(header.arm9Size));
		}

		FSFile arm7Bin = dirPath.getChild("arm7.bin");
		if (!arm7Bin.exists()) {
			rom.seek(header.arm7RomOffset);
			arm7Bin.setBytes(rom.readBytes(header.arm7Size));
		}

		FSFile y9Bin = dirPath.getChild("y9.bin");
		if (!y9Bin.exists()) {
			rom.seek(header.arm9OverlayOffset);
			y9Bin.setBytes(rom.readBytes(header.arm9OverlaySize));
		}

		FSFile y7Bin = dirPath.getChild("y7.bin");
		if (!y7Bin.exists()) {
			rom.seek(header.arm7OverlayOffset);
			y7Bin.setBytes(rom.readBytes(header.arm7OverlaySize));
		}

		FSFile banner = dirPath.getChild("banner.bin");
		if (!banner.exists()) {
			rom.seek(header.iconOffset);
			banner.setBytes(rom.readBytes(0x840));
		}

		rom.close();
	}

	private static void ensureFilesExist(FSFile... files) throws FileNotFoundException {
		for (FSFile f : files) {
			if (!f.exists()) {
				throw new FileNotFoundException("File " + f + " does not exist!");
			}
			if (f.isDirectory()) {
				throw new FileNotFoundException("File " + f + " must not be a directory!");
			}
		}
	}

	private static void ensureDirectoriesExist(FSFile... files) throws FileNotFoundException {
		for (FSFile f : files) {
			if (!f.exists()) {
				throw new FileNotFoundException("File " + f + " does not exist!");
			}
			if (!f.isDirectory()) {
				throw new FileNotFoundException("File " + f + " must be a directory!");
			}
		}
	}
	
	public static void main(String[] args) {
		try {
			buildROM(new DiskFile("D:\\Emugames\\DS\\Pokemon - White Version 2 (USA, Europe) (NDSi Enhanced)_SDSME"), new DiskFile("D:\\Emugames\\DS\\mw.nds"));
		} catch (IOException ex) {
			Logger.getLogger(NDSROM.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Build the entire NDSROM from the given directory
	 *
	 * @param sourceDir The path of the directory containing the files
	 * @param romPath The path of the .nds file
	 * @throws IOException If something goes wrong
	 */
	public static void buildROM(FSFile sourceDir, FSFile romPath) throws IOException {
		FSFile dataDir = sourceDir.getChild("data");
		FSFile ovlDir = sourceDir.getChild("overlay");
		FSFile arm9bin = sourceDir.getChild("arm9.bin");
		FSFile arm7bin = sourceDir.getChild("arm7.bin");
		FSFile y9bin = sourceDir.getChild("arm9ovltable.bin");
		FSFile y7bin = sourceDir.getChild("arm7ovltable.bin");
		FSFile headerBin = sourceDir.getChild("header.bin");
		FSFile banner = sourceDir.getChild("banner.bin");

		if (!y9bin.exists()) {
			y9bin = sourceDir.getChild("y9.bin");
		}
		if (!y7bin.exists()) {
			y7bin = sourceDir.getChild("y7.bin");
		}

		ensureFilesExist(arm7bin, arm9bin, y7bin, y9bin, headerBin, banner);
		ensureDirectoriesExist(dataDir, ovlDir);

		romPath.delete();
		DataIOStream out = romPath.getDataIOStream(); // The stream for the .nds file

		OverlayTable ovltable = new OverlayTable(y9bin);
		ovltable.updateByDir(ovlDir);
		ovltable.write();

		// Loading the actual data and the overlay and pre-calculate offsets
		List<? extends FSFile> overlays = ovlDir.listFiles();
		Collections.sort(overlays);
		
		NitroDirectory root = new NitroDirectory("data", 0xf000, null);

		// Recursively load directories and files, getting the root nitro directory
		NitroDirectory.loadDir(
			dataDir,
			root,
			new NitroDirectory.FSLoaderState(0xf000, ovltable.overlays.size(), 0)
		);

		// Reading the header template, but skipping the section for now as we have to adjust some values
		DataIOStream reader = headerBin.getDataIOStream();
		SRLHeader header = new SRLHeader(reader);
		reader.close();
		out.write(new byte[0x4000]); //allocate
		
		byte[] temp;

		// The ARM9
		header.arm9RomOffset = out.getPosition();
		temp = arm9bin.getBytes();
		header.arm9Size = temp.length;
		out.write(temp);
		out.pad(4, 0xFF);

		// The ARM9 overlay table
		header.arm9OverlayOffset = out.getPosition();
		temp = y9bin.getBytes();
		header.arm9OverlaySize = temp.length;
		out.write(temp);
		out.pad(4, 0xFF);

		// This will be needed for the FAT
		List<Integer> overlayStartOffsets = new ArrayList<>();
		List<Integer> overlayEndOffsets = new ArrayList<>();

		// The ARM9 overlays
		for (int i = 0; i < header.arm9OverlaySize / OverlayTable.OverlayInfo.BYTES; i++) {
			FSFile ovl = overlays.get(i);
			byte[] ovlBytes = ovl.getBytes();
			overlayStartOffsets.add(out.getPosition());
			overlayEndOffsets.add(out.getPosition() + ovlBytes.length);
			out.write(ovlBytes);
			out.pad(4, 0xFF);
		}

		// The ARM7
		header.arm7RomOffset = out.getPosition();
		temp = arm7bin.getBytes();
		header.arm7Size = temp.length;
		out.write(temp);
		out.pad(4, 0xFF);

		// The ARM7 overlay table
		header.arm7OverlayOffset = out.getPosition();
		temp = y7bin.getBytes();
		header.arm7OverlaySize = temp.length;
		out.write(temp);
		out.pad(4, 0xFF);

		// The ARM7 overlays
		int ovl7Base = header.arm9OverlaySize / OverlayTable.OverlayInfo.BYTES;
		for (int i = 0; i < header.arm7OverlaySize / OverlayTable.OverlayInfo.BYTES; i++) {
			FSFile ovl = overlays.get(i + ovl7Base);
			overlayStartOffsets.add(out.getPosition());
			overlayEndOffsets.add(out.getPosition() + ovl.length());
			out.write(ovl.getBytes());
			out.pad(4, 0xFF);
		}

		// The File Name Table
		header.fntOffset = out.getPosition();
		FNT.writeFNT(out, root);
		header.fntSize = out.getPosition() - header.fntOffset;
		out.pad(4, 0xFF);

		// The File Allocation Table
		header.fatOffset = out.getPosition();
		header.fatSize = FAT.calculateFATSize(dataDir) + overlays.size() * 8;
		out.write(new byte[header.fatSize]); //allocate FAT
		out.pad(4, 0xFF);

		// The banner
		header.iconOffset = out.getPosition();
		out.write(banner.getBytes());
		out.pad(4, 0xFF);

		int fimgOffset = out.getPosition();

		out.seek(header.fatOffset);
		FAT.writeFAT(out, root, fimgOffset, overlayStartOffsets, overlayEndOffsets);

		// The actual files
		out.seek(fimgOffset);
		NitroDirectory.repackFileTree(out, fimgOffset, dataDir, root);
		
		out.pad(1 << 0x13);
		int size = out.getPosition();
		header.ntrRomRegionEnd = size >> 0x13;
		header.twlRomRegionStart = size >> 0x13;

		// Write updated header
		header.updateHeaderChecksum(out);
		out.seek(0);
		header.write(out);

		out.close();
	}
}
