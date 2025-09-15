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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import ctrmap.formats.ntr.common.compression.BLZ;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import java.util.*;

import ctrmap.formats.ntr.rom.OverlayTable;
import ctrmap.formats.ntr.rom.srl.newlib.SRLHeader;
import java.security.GeneralSecurityException;

import xstandard.gui.file.ExtensionFilter;
import xstandard.io.util.IOUtils;
import xstandard.crypto.Modcrypt;
import xstandard.io.base.impl.access.MemoryStream;

/**
 * Nintendo DS ROM extractor/rebuilder.
 *
 * This class is largely a fork of jNDSTool, adapted for use with CTRMapStandardLibrary's faster IO, FS and
 * crypto.
 *
 * Other improvements include: - Automatic overlay table updates - Support for normal overlay table names
 * (y7.bin, y9.bin) - Correct checksum calculation - Resource leak fixes
 */
public class NDSROM {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Nintendo DS ROM", "*.nds");
	
	static final int CARTRIDGE_OPTIMAL_ALIGNMENT = 512;
	static final int TWL_BINARY_ALIGNMENT = 0x1000;
        
    static final String TWL_HMAC_FUNCTION = "HmacSHA1";
	static final byte[] TWL_HMAC_KEY = HexFormat.of().parseHex("2106C0DEBA98CE3FA692E39D46F2ED0176E3CC08562363FACAD4ECDF9A6278348F6D633CFE22CA9220889723D2CFAEC232678DFECA836498ACFD3E3787465824");

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
			headerBin.setBytes(rom.readBytes(0x1000));
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
			int iconSize = (header.iconSize != 0) ? header.iconSize : 0x840;
			banner.setBytes(rom.readBytes(iconSize));
		}
                
		// DSi enhanced games
		
		if(header.unitCode == 2) {
			
			// TWL Blowfish table (needed by DSi system menu, depends only on gamecode)
			FSFile twlBlowfishTbl = dirPath.getChild("twlblowfishtable.bin");
			if(!twlBlowfishTbl.exists()) {
				rom.seek(header.arm9iRomOffset - 0x3000);
				twlBlowfishTbl.setBytes(rom.readBytes(0x3000));
			}

			// ARM9i binary. Decrypt now so that it can be encrypted back when writing.
			// This is needed because editing ARM9 changes the IV for Modcrypt.
			rom.seek(header.arm9iRomOffset);
			byte[] arm9iBinary = rom.readBytes(header.arm9iSize);
			
			// Technically area1 can be anywhere, but it seems to always be in the first 0x4000 bytes of arm9i.
			// If any exceptions are found this should be updated to handle them.
			byte[] area1 = new byte[0x4000];
			System.arraycopy(arm9iBinary, 0, area1, 0, 0x4000);

			Modcrypt modcrypt = new Modcrypt(header.gameCode, header.hmacArm9i, header.hmacArm9WithSecureArea);
			ByteArrayInputStream is = new ByteArrayInputStream(area1);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
                        
			try {
				modcrypt.transform(new DataInputStream(is), new DataOutputStream(os));
			} catch (GeneralSecurityException e) {
				throw new RuntimeException("Failed to decrypt DSi binaries", e);
			}
			
			area1 = os.toByteArray();
			System.arraycopy(area1, 0, arm9iBinary, 0, 0x4000);

			FSFile arm9i = dirPath.getChild("arm9i.bin");
			if(!arm9i.exists()) {
				arm9i.setBytes(arm9iBinary);
			}

			// ARM7i binary. This should be unencrypted (area2 offset seems to always be 0)
			FSFile arm7i = dirPath.getAnyChild("arm7i.bin");
			if(!arm7i.exists()) {
				rom.seek(header.arm7iRomOffset);
				arm7i.setBytes(rom.readBytes(header.arm7iSize));
			}
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
		// DSi-enhanced only
        FSFile twlBlowfishTbl = sourceDir.getChild("twlblowfishtable.bin");
		FSFile arm9ibin = sourceDir.getChild("arm9i.bin");
		FSFile arm7ibin = sourceDir.getChild("arm7i.bin");

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

		if(header.unitCode == 2) {
			ensureFilesExist(arm9ibin, arm7bin, twlBlowfishTbl);
		}

		reader.close();
		out.write(new byte[0x4000]); //allocate

		byte[] temp;

		// The ARM9
		temp = arm9bin.getBytes();
		header.arm9RomOffset = out.getPosition();
		header.arm9Size = temp.length;

		DataIOStream arm9editor = new DataIOStream(temp);
		if (seekBootstrapHeader(arm9editor)) {
			//TwilightMenu/nds-bootstrap use this to decompress the ROM
			//by themselves and inject their code. As such we have to adjust it
			//when the ROM is not compressed.
			int arm9DecompressRamAddress;
			if (BLZ.getBLZHeader(arm9bin).valid()) {
				//ARM9 is compressed
				arm9DecompressRamAddress = header.arm9RamAddress + header.arm9Size;
			} else {
				arm9DecompressRamAddress = 0;
			}
			System.out.println("Writing ARM9 compression footer 0x" + Integer.toHexString(arm9DecompressRamAddress));
			arm9editor.skipBytes(-8);
			arm9editor.writeInt(arm9DecompressRamAddress);
		}
		else {
			System.out.println("ARM9 bootstrap header not found!");
		}
		arm9editor.close();

		out.write(temp);
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

		// The ARM9 overlay table
		temp = y9bin.getBytes();
		header.arm9OverlayOffset = temp.length == 0 ? 0 : out.getPosition();
		header.arm9OverlaySize = temp.length;
		out.write(temp);
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

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
			out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);
		}

		// The ARM7
		header.arm7RomOffset = out.getPosition();
		temp = arm7bin.getBytes();
		header.arm7Size = temp.length;
		out.write(temp);
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

		// The ARM7 overlay table
		temp = y7bin.getBytes();
		header.arm7OverlayOffset = temp.length == 0 ? 0 : temp.length;
		header.arm7OverlaySize = temp.length;
		out.write(temp);
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

		// The ARM7 overlays
		int ovl7Base = header.arm9OverlaySize / OverlayTable.OverlayInfo.BYTES;
		for (int i = 0; i < header.arm7OverlaySize / OverlayTable.OverlayInfo.BYTES; i++) {
			FSFile ovl = overlays.get(i + ovl7Base);
			overlayStartOffsets.add(out.getPosition());
			overlayEndOffsets.add(out.getPosition() + ovl.length());
			out.write(ovl.getBytes());
			out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);
		}

		// The File Name Table
		header.fntOffset = out.getPosition();
		FNT.writeFNT(out, root);
		header.fntSize = out.getPosition() - header.fntOffset;
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

		// The File Allocation Table
		header.fatOffset = out.getPosition();
		header.fatSize = FAT.calculateFATSize(dataDir) + overlays.size() * 8;
		out.write(new byte[header.fatSize]); //allocate FAT
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

		// The banner
		header.iconOffset = out.getPosition();
		out.write(banner.getBytes());
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

		int fimgOffset = out.getPosition();

		out.seek(header.fatOffset);
		FAT.writeFAT(out, root, fimgOffset, overlayStartOffsets, overlayEndOffsets);

		// The actual files
		out.seek(fimgOffset);
		NitroDirectory.repackFileTree(out, fimgOffset, dataDir, root);
                
		// DSi-enhanced games: generate and write digest sector/block
		if(header.unitCode == 2) {
			out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);

			header.digestNtrRegionLength = out.getPosition() - header.digestNtrRegionOffset;
			header.digestSectorHashtableOffset = out.getPosition();

			// Build the TWL digest region from arm9i and arm7i
			MemoryStream regionTwl = new MemoryStream();
			regionTwl.write(arm9ibin.getBytes());
            regionTwl.pad(TWL_BINARY_ALIGNMENT, 0xFF);
			regionTwl.write(arm7ibin.getBytes());
            regionTwl.pad(0x400, 0xFF);

			// Read NTR digest region
			out.seek(header.digestNtrRegionOffset);
			byte[] regionNtr = out.readBytes(header.digestNtrRegionLength);

			// Build the digest region
			ByteArrayOutputStream digestRegion = new ByteArrayOutputStream();
			digestRegion.write(regionNtr);
			digestRegion.write(regionTwl.toByteArray());
                                               
			try {
				MemoryStream digestSector = new MemoryStream();
				digestSector.write(generateDigest(digestRegion.toByteArray(), header.digestSectorSize, true));
				digestSector.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0x00);

				byte[] digestBlock = generateDigest(digestSector.toByteArray(), header.digestBlockSectorCount * 20, false);

				// Write digest sector and block
				out.seek(header.digestSectorHashtableOffset);
				out.write(digestSector.toByteArray());
				out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0x00);
				header.digestSectorHashtableLength = out.getPosition() - header.digestSectorHashtableOffset;

				header.digestBlockHashtableOffset = out.getPosition();
				out.write(digestBlock);
				header.digestBlockHashtableLength = out.getPosition() - header.digestBlockHashtableOffset;
			} catch (GeneralSecurityException e) {
				throw new RuntimeException("Generation of DSi digest sectors failed", e);
			}
		}

		// Align NTR rom end to 0x200
		out.pad(CARTRIDGE_OPTIMAL_ALIGNMENT, 0xFF);
		header.usedRomSize = out.getPosition();
		
		// Align TWL region to 0x80000
		out.pad(1 << 0x13, 0xFF);
		int size = out.getPosition();
		header.ntrRomRegionEnd = size >> 0x13;
		header.twlRomRegionStart = size >> 0x13;

		// DSi-enhanced games: HMACs, Modcrypt, write TWL region to rom
		if(header.unitCode == 2) {
			try {
				Mac hmac = Mac.getInstance(TWL_HMAC_FUNCTION);
				SecretKeySpec secretKey = new SecretKeySpec(TWL_HMAC_KEY, TWL_HMAC_FUNCTION);
				hmac.init(secretKey);

				// arm9 with secure area
				out.seek(header.arm9RomOffset);
				hmac.update(out.readBytes(header.arm9Size));
				header.hmacArm9WithSecureArea = hmac.doFinal();

				// arm7
				out.seek(header.arm7RomOffset);
				hmac.update(out.readBytes(header.arm7Size));
				header.hmacArm7 = hmac.doFinal();

				// digest master
				out.seek(header.digestBlockHashtableOffset);
				hmac.update(out.readBytes(header.digestBlockHashtableLength));
				header.hmacDigestMaster = hmac.doFinal();

				// icon
				out.seek(header.iconOffset);
				hmac.update(out.readBytes(header.iconSize));
				header.hmacIconTitle = hmac.doFinal();

				// arm9i
				hmac.update(arm9ibin.getBytes());
				header.hmacArm9i = hmac.doFinal();

				// arm7i
				hmac.update(arm7ibin.getBytes());
				header.hmacArm7i = hmac.doFinal();

				// arm9 without secure area
				out.seek(header.arm9RomOffset + 0x4000);
				hmac.update(out.readBytes(header.arm9Size - 0x4000));
				header.hmacArm9WithoutSecureArea = hmac.doFinal();
			} catch (GeneralSecurityException e) {
				throw new RuntimeException("Generation of DSi HMAC entries failed", e);
			}

			// Modcrypt (area 1)

			byte[] arm9i = arm9ibin.getBytes();
            byte[] area1 = new byte[0x4000];
			System.arraycopy(arm9i, 0, area1, 0, 0x4000);

			Modcrypt modcrypt = new Modcrypt(header.gameCode, header.hmacArm9i, header.hmacArm9WithSecureArea);
            ByteArrayInputStream is = new ByteArrayInputStream(area1);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			
			try {
				modcrypt.transform(new DataInputStream(is), new DataOutputStream(os));
			} catch (GeneralSecurityException e) {
				throw new RuntimeException("Failed to encrypt DSi binaries", e);
			}
                        
			area1 = os.toByteArray();
			System.arraycopy(area1, 0, arm9i, 0, 0x4000);

			// Write TWL region to rom

			out.seek(header.twlRomRegionStart << 0x13);
			out.write(twlBlowfishTbl.getBytes());
			header.arm9iRomOffset = out.getPosition();
            header.digestTwlRegionOffset = header.arm9iRomOffset;
            header.modcryptArea1Offset = header.arm9iRomOffset;
			out.write(arm9i);
			out.pad(TWL_BINARY_ALIGNMENT, 0xFF);
			header.arm7iRomOffset = out.getPosition();
			out.write(arm7ibin.getBytes());
			out.pad(0x400, 0xFF);
			header.totalUsedRomSize = out.getPosition();
            header.digestTwlRegionLength = out.getPosition() - header.digestTwlRegionOffset;
			out.pad(TWL_BINARY_ALIGNMENT, 0xFF);
		}

		// Write updated header
		header.updateHeaderChecksum(out);
		out.seek(0);
		header.write(out);
		
		// Recalculate header signature
		if(header.unitCode == 2) {
            try {
                MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
                out.seek(0);
                byte[] headerBytes = out.readBytes(0xE00);
                sha1.update(headerBytes);
                byte[] sha1Digest = sha1.digest();

                // Header digest (PKCS#1 v1.5 without ASN.1 encoding)
                byte[] headerDigest = new byte[128];
                headerDigest[0] = 0x00;
                headerDigest[1] = 0x01;
                Arrays.fill(headerDigest, 2, 107, (byte)0xFF);
                headerDigest[107] = 0x00;
                System.arraycopy(sha1Digest, 0, headerDigest, 108, sha1Digest.length);

                // We don't know the retail private key, so we can't actually encrypt it.
                // Tinke DSi writes the plaintext PKCS#1 data here, so we can do the same.
                header.headerRsaSignature = headerDigest;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Failed to generate the DSi header signature", e);
            }

            // Write changes to rom
            out.seek(0);
		    header.write(out);
		}

		out.close();
	}

	private static boolean seekBootstrapHeader(DataIOStream stream) throws IOException {
		return IOUtils.searchForBytes(
			stream,
			0,
			-1,
			Integer.BYTES,
			new IOUtils.SearchPattern(
				new byte[]{(byte) 0x21, (byte) 0x06, (byte) 0xC0, (byte) 0xDE, (byte) 0xDE, (byte) 0xC0, (byte) 0x06, (byte) 0x21}
			)
		) != null;
	}

	private static byte[] generateDigest(byte[] data, int sectorSize, boolean truncate) throws IOException, GeneralSecurityException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();

		Mac mac = Mac.getInstance(TWL_HMAC_FUNCTION);
		SecretKeySpec secretKey = new SecretKeySpec(TWL_HMAC_KEY, TWL_HMAC_FUNCTION);
		mac.init(secretKey);

		int nSectors = truncate ? data.length / sectorSize : (data.length + sectorSize - 1) / sectorSize;

		for (int i = 0; i < nSectors; i++) {
				int start = i * sectorSize;
				int len = Math.min(data.length - start, sectorSize);
				mac.update(data, start, len);
				byte[] digest = mac.doFinal();

				out.write(digest);
		}

		return out.toByteArray();
	}
}
