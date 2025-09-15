package ctrmap.formats.ntr.rom.srl.newlib;

import xstandard.crypto.CRC16;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SRLHeader {

	public String gameTitle;
	public String gameCode;
	public String makerCode;

	public int unitCode;
	public int encryptionSeedSelect;
	public int deviceCapacity;
	public byte[] reserved1;
	public int dsiFlags;
	public int ndsRegion;
	public int romVersion;
	public int autoStart;

	public int arm9RomOffset;
	public int arm9EntryAddress;
	public int arm9RamAddress;
	public int arm9Size;

	public int arm7RomOffset;
	public int arm7EntryAddress;
	public int arm7RamAddress;
	public int arm7Size;

	public int fntOffset;
	public int fntSize;

	public int fatOffset;
	public int fatSize;

	public int arm9OverlayOffset;
	public int arm9OverlaySize;

	public int arm7OverlayOffset;
	public int arm7OverlaySize;

	public int port40001A4hNormalCommand;
	public int port40001A4hKey1Command;

	public int iconOffset;

	public int secureAreaChecksum;
	public int secureAreaDelay;

	public int arm9AutoLoad;

	public int arm7AutoLoad;

	public long secureAreaDisable;

	public int usedRomSize; // NTR only, excluding TWL ROM data
	public int headerSize;

	public int arm9ParamTableOffset;
	public int arm7ParamTableOffset;

	/**
	 * These two fields are a doozy. Pokemon White 2 refuses to read files past this point, so they both have
	 * to be adjusted properly. (see 0x207215C in W2U ARM9, took me ages to debug all the way there).
	 */
	public int ntrRomRegionEnd;
	public int twlRomRegionStart;

	public byte[] reserved2;
	public byte[] reserved3;

	public byte[] logo;
	public int logoChecksum;
	public int headerChecksum;

	public int debugRomOffset;
	public int debugSize;
	public int debugRamAddress;

	public int reserved4;
	public byte[] reserved5;

	// TWL extended header fields
	
	public byte[] globalWramSlotSettings;
	public byte[] localWramAreasArm9;
	public byte[] localWramAreasArm7;
	public byte[] globalWramWriteProtect;
	public byte globalWramCnt;
	
	public int regionFlags;
	public int accessControl;
	public int arm7scfgExt7Settings;
	
	public byte[] reserved6;
	
	public byte twlApplicationFlags;
	
	public int arm9iRomOffset;
	public int reserved7;
	public int arm9iRamLoadAddress;
	public int arm9iSize;
	
	public int arm7iRomOffset;        
	public int deviceListArm7RamAddress;
	public int arm7iRamLoadAddress;
	public int arm7iSize;
	
	public int digestNtrRegionOffset;
	public int digestNtrRegionLength;
	public int digestTwlRegionOffset;
	public int digestTwlRegionLength;
	public int digestSectorHashtableOffset;
	public int digestSectorHashtableLength;
	public int digestBlockHashtableOffset;
	public int digestBlockHashtableLength;
	public int digestSectorSize;
	public int digestBlockSectorCount;
	
	public int iconSize;
	
	public byte shared2File0000Size;
	public byte shared2File0001Size;
	
	public byte eulaVersion;
	public byte useRatings;
	
	public int totalUsedRomSize;    // Including TWL region
	
	public byte shared2File0002Size;
	public byte shared2File0003Size;
	public byte shared2File0004Size;
	public byte shared2File0005Size;
	
	public int arm9iParamTableOffset;
	public int arm7iParamTableOffset;
	
	public int modcryptArea1Offset;
	public int modcryptArea1Size;
	public int modcryptArea2Offset;
	public int modcryptArea2Size;
	
	public String titleIdEmagcode;
	public byte titleIdFileType;
	public byte titleIdFixedZero1;
	public byte titleIdFixedThree;
	public byte titleIdFixedZero2;
	
	public int dsiwarePublicFilesize;
	public int dsiwarePrivateFilesize;
	
	public byte[] reserved8;
	
	public byte parentalControlAgeRatingCero;
	public byte parentalControlAgeRatingEsrb;
	public byte parentalControlAgeRatingReserved1;
	public byte parentalControlAgeRatingUsk;
	public byte parentalControlAgeRatingPegi;
	public byte parentalControlAgeRatingReserved2;
	public byte parentalControlAgeRatingPegiPortugal;
	public byte parentalControlAgeRatingPegiBbfcUk;
	public byte parentalControlAgeRatingAgcb;
	public byte parentalControlAgeRatingGrb;
	public byte[] parentalControlAgeRatingReserved3;
	
	// TWL SHA-1 HMAC table
	
	public byte[] hmacArm9WithSecureArea;
	public byte[] hmacArm7;
	public byte[] hmacDigestMaster;
	public byte[] hmacIconTitle;    // Also in non-whitelisted NDS titles
	public byte[] hmacArm9i;
	public byte[] hmacArm7i;
	
	public byte[] hmacNdsReserved1; // HMAC for 0x160-byte header + ARM9 + ARM7 in non-whitelisted NDS titles
	public byte[] hmacNdsReserved2; // HMAC for ARM9 overlay table + NitroFAT in non-whitelisted NDS titles
	
	public byte[] hmacArm9WithoutSecureArea;
	
	// Empty reserved area (zero-filled)

	public byte[] reserved9;
	
	// Reserved for passing arguments in debug roms. Empty in retail
	
	public byte[] reserved10;
	
	// Extended header RSA signature
	
	public byte[] headerRsaSignature;   // Commits to [0x00 - 0xDFF]
	
	/**
	 * Beyond this point there is a region [0x1000 - 0x3FFF] which is not loaded by BIOS.
	 * ndstool -se will place some test patterns here, and also some Blowfish tables
	 * which are derived from the game code. This data is also found in debug .SRL files.
	 * In dumps of retail cartridges this is zero-filled, so we can just ignore it.
	 */

	public SRLHeader(FSFile fsf) throws IOException {
		DataInputEx in = fsf.getDataInputStream();
		read(in);
		in.close();
	}

	public SRLHeader(DataInputEx rom) throws IOException {
		read(rom);
	}

	private void read(DataInputEx rom) throws IOException {
		gameTitle = rom.readPaddedString(12);
		gameCode = rom.readPaddedString(4);
		makerCode = rom.readPaddedString(2);
		unitCode = rom.readByte();
		encryptionSeedSelect = rom.readByte();
		deviceCapacity = rom.readByte();
		reserved1 = rom.readBytes(7);
		dsiFlags = rom.readByte();
		ndsRegion = rom.readByte();
		romVersion = rom.readByte();
		autoStart = rom.readByte();

		arm9RomOffset = rom.readInt();
		arm9EntryAddress = rom.readInt();
		arm9RamAddress = rom.readInt();
		arm9Size = rom.readInt();

		arm7RomOffset = rom.readInt();
		arm7EntryAddress = rom.readInt();
		arm7RamAddress = rom.readInt();
		arm7Size = rom.readInt();

		fntOffset = rom.readInt();
		fntSize = rom.readInt();
		fatOffset = rom.readInt();
		fatSize = rom.readInt();

		arm9OverlayOffset = rom.readInt();
		arm9OverlaySize = rom.readInt();

		arm7OverlayOffset = rom.readInt();
		arm7OverlaySize = rom.readInt();

		port40001A4hNormalCommand = rom.readInt();
		port40001A4hKey1Command = rom.readInt();

		iconOffset = rom.readInt();
		secureAreaChecksum = rom.readUnsignedShort();
		secureAreaDelay = rom.readUnsignedShort();
		arm9AutoLoad = rom.readInt();
		arm7AutoLoad = rom.readInt();
		secureAreaDisable = rom.readLong();
		usedRomSize = rom.readInt();
		headerSize = rom.readInt();
		arm9ParamTableOffset = rom.readInt();
		arm7ParamTableOffset = rom.readInt();
		ntrRomRegionEnd = rom.readUnsignedShort();
		twlRomRegionStart = rom.readUnsignedShort();
		reserved2 = rom.readBytes(0x1C);
		reserved3 = rom.readBytes(0x10);
		logo = rom.readBytes(0x9c);
		logoChecksum = rom.readUnsignedShort();
		headerChecksum = rom.readUnsignedShort();
		debugRomOffset = rom.readInt();
		debugSize = rom.readInt();
		debugRamAddress = rom.readInt();
		reserved4 = rom.readInt();
		reserved5 = rom.readBytes(0x10);

		// TWL extended header
		
		globalWramSlotSettings = rom.readBytes(20);
		localWramAreasArm9 = rom.readBytes(12);
		localWramAreasArm7 = rom.readBytes(12);
		globalWramWriteProtect = rom.readBytes(3);
		globalWramCnt = rom.readByte();
		
		regionFlags = rom.readInt();
		accessControl = rom.readInt();
		arm7scfgExt7Settings = rom.readInt();
		
		reserved6 = rom.readBytes(3);
		
		twlApplicationFlags = rom.readByte();
		
		arm9iRomOffset = rom.readInt();
		reserved7 = rom.readInt();
		arm9iRamLoadAddress = rom.readInt();
		arm9iSize = rom.readInt();
		
		arm7iRomOffset = rom.readInt();
		deviceListArm7RamAddress = rom.readInt();
		arm7iRamLoadAddress = rom.readInt();
		arm7iSize = rom.readInt();
		
		digestNtrRegionOffset = rom.readInt();
		digestNtrRegionLength = rom.readInt();
		digestTwlRegionOffset = rom.readInt();
		digestTwlRegionLength = rom.readInt();
		digestSectorHashtableOffset = rom.readInt();
		digestSectorHashtableLength = rom.readInt();
		digestBlockHashtableOffset = rom.readInt();
		digestBlockHashtableLength = rom.readInt();
		digestSectorSize = rom.readInt();
		digestBlockSectorCount = rom.readInt();

		iconSize = rom.readInt();

		shared2File0000Size = rom.readByte();
		shared2File0001Size = rom.readByte();

		eulaVersion = rom.readByte();
		useRatings = rom.readByte();

		totalUsedRomSize = rom.readInt();

		shared2File0002Size = rom.readByte();
		shared2File0003Size = rom.readByte();
		shared2File0004Size = rom.readByte();
		shared2File0005Size = rom.readByte();

		arm9iParamTableOffset = rom.readInt();
		arm7iParamTableOffset = rom.readInt();

		modcryptArea1Offset = rom.readInt();
		modcryptArea1Size = rom.readInt();
		modcryptArea2Offset = rom.readInt();
		modcryptArea2Size = rom.readInt();

		titleIdEmagcode = rom.readPaddedString(4);
		titleIdFileType = rom.readByte();
		titleIdFixedZero1 = rom.readByte();
		titleIdFixedThree = rom.readByte();
		titleIdFixedZero2 = rom.readByte();

		dsiwarePublicFilesize = rom.readInt();
		dsiwarePrivateFilesize = rom.readInt();

		reserved8 = rom.readBytes(176);

		parentalControlAgeRatingCero = rom.readByte();
		parentalControlAgeRatingEsrb = rom.readByte();
		parentalControlAgeRatingReserved1 = rom.readByte();
		parentalControlAgeRatingUsk = rom.readByte();
		parentalControlAgeRatingPegi = rom.readByte();
		parentalControlAgeRatingReserved2 = rom.readByte();
		parentalControlAgeRatingPegiPortugal = rom.readByte();
		parentalControlAgeRatingPegiBbfcUk = rom.readByte();
		parentalControlAgeRatingAgcb = rom.readByte();
		parentalControlAgeRatingGrb = rom.readByte();
		parentalControlAgeRatingReserved3 = rom.readBytes(6);

		hmacArm9WithSecureArea = rom.readBytes(20);
		hmacArm7 = rom.readBytes(20);
		hmacDigestMaster = rom.readBytes(20);
		hmacIconTitle = rom.readBytes(20);
		hmacArm9i = rom.readBytes(20);
		hmacArm7i = rom.readBytes(20);
		hmacNdsReserved1 = rom.readBytes(20);
		hmacNdsReserved2 = rom.readBytes(20);
		hmacArm9WithoutSecureArea = rom.readBytes(20);

		reserved9 = rom.readBytes(2636);
		reserved10 = rom.readBytes(0x180);

		headerRsaSignature = rom.readBytes(0x80);
	}

	public void write(DataOutputEx rom) throws IOException {
		rom.writePaddedString(gameTitle, 12);
		rom.writePaddedString(gameCode, 4);
		rom.writePaddedString(makerCode, 2);
		rom.writeByte(unitCode);
		rom.writeByte(encryptionSeedSelect);
		rom.writeByte(deviceCapacity);
		rom.write(reserved1);
		rom.writeByte(dsiFlags);
		rom.writeByte(ndsRegion);
		rom.writeByte(romVersion);
		rom.writeByte(autoStart);

		rom.writeInt(arm9RomOffset);
		rom.writeInt(arm9EntryAddress);
		rom.writeInt(arm9RamAddress);
		rom.writeInt(arm9Size);

		rom.writeInt(arm7RomOffset);
		rom.writeInt(arm7EntryAddress);
		rom.writeInt(arm7RamAddress);
		rom.writeInt(arm7Size);

		rom.writeInt(fntOffset);
		rom.writeInt(fntSize);
		rom.writeInt(fatOffset);
		rom.writeInt(fatSize);

		rom.writeInt(arm9OverlayOffset);
		rom.writeInt(arm9OverlaySize);

		rom.writeInt(arm7OverlayOffset);
		rom.writeInt(arm7OverlaySize);

		rom.writeInt(port40001A4hNormalCommand);
		rom.writeInt(port40001A4hKey1Command);

		rom.writeInt(iconOffset);
		rom.writeShort(secureAreaChecksum);
		rom.writeShort(secureAreaDelay);
		rom.writeInt(arm9AutoLoad);
		rom.writeInt(arm7AutoLoad);
		rom.writeLong(secureAreaDisable);
		rom.writeInt(usedRomSize);
		rom.writeInt(headerSize);
		rom.writeInt(arm9ParamTableOffset);
		rom.writeInt(arm7ParamTableOffset);
		rom.writeShort(ntrRomRegionEnd);
		rom.writeShort(twlRomRegionStart);
		rom.write(reserved2);
		rom.write(reserved3);

		rom.write(logo);
		rom.writeShort(logoChecksum);
		rom.writeShort(headerChecksum);
		rom.writeInt(debugRomOffset);
		rom.writeInt(debugSize);
		rom.writeInt(debugRamAddress);
		rom.writeInt(reserved4);
		rom.write(reserved5);

		// TWL extended header

		rom.write(globalWramSlotSettings);
		rom.write(localWramAreasArm9);
		rom.write(localWramAreasArm7);
		rom.write(globalWramWriteProtect);
		rom.writeByte(globalWramCnt);

		rom.writeInt(regionFlags);
		rom.writeInt(accessControl);
		rom.writeInt(arm7scfgExt7Settings);

		rom.write(reserved6);

		rom.writeByte(twlApplicationFlags);

		rom.writeInt(arm9iRomOffset);
		rom.writeInt(reserved7);
		rom.writeInt(arm9iRamLoadAddress);
		rom.writeInt(arm9iSize);

		rom.writeInt(arm7iRomOffset);
		rom.writeInt(deviceListArm7RamAddress);
		rom.writeInt(arm7iRamLoadAddress);
		rom.writeInt(arm7iSize);

		rom.writeInt(digestNtrRegionOffset);
		rom.writeInt(digestNtrRegionLength);
		rom.writeInt(digestTwlRegionOffset);
		rom.writeInt(digestTwlRegionLength);
		rom.writeInt(digestSectorHashtableOffset);
		rom.writeInt(digestSectorHashtableLength);
		rom.writeInt(digestBlockHashtableOffset);
		rom.writeInt(digestBlockHashtableLength);
		rom.writeInt(digestSectorSize);
		rom.writeInt(digestBlockSectorCount);

		rom.writeInt(iconSize);

		rom.writeByte(shared2File0000Size);
		rom.writeByte(shared2File0001Size);

		rom.writeByte(eulaVersion);
		rom.writeByte(useRatings);

		rom.writeInt(totalUsedRomSize);

		rom.writeByte(shared2File0002Size);
		rom.writeByte(shared2File0003Size);
		rom.writeByte(shared2File0004Size);
		rom.writeByte(shared2File0005Size);

		rom.writeInt(arm9iParamTableOffset);
		rom.writeInt(arm7iParamTableOffset);

		rom.writeInt(modcryptArea1Offset);
		rom.writeInt(modcryptArea1Size);
		rom.writeInt(modcryptArea2Offset);
		rom.writeInt(modcryptArea2Size);

		rom.writePaddedString(titleIdEmagcode, 4);
		rom.writeByte(titleIdFileType);
		rom.writeByte(titleIdFixedZero1);
		rom.writeByte(titleIdFixedThree);
		rom.writeByte(titleIdFixedZero2);

		rom.writeInt(dsiwarePublicFilesize);
		rom.writeInt(dsiwarePrivateFilesize);

		rom.write(reserved8);

		rom.writeByte(parentalControlAgeRatingCero);
		rom.writeByte(parentalControlAgeRatingEsrb);
		rom.writeByte(parentalControlAgeRatingReserved1);
		rom.writeByte(parentalControlAgeRatingUsk);
		rom.writeByte(parentalControlAgeRatingPegi);
		rom.writeByte(parentalControlAgeRatingReserved2);
		rom.writeByte(parentalControlAgeRatingPegiPortugal);
		rom.writeByte(parentalControlAgeRatingPegiBbfcUk);
		rom.writeByte(parentalControlAgeRatingAgcb);
		rom.writeByte(parentalControlAgeRatingGrb);
		rom.write(parentalControlAgeRatingReserved3);

		rom.write(hmacArm9WithSecureArea);
		rom.write(hmacArm7);
		rom.write(hmacDigestMaster);
		rom.write(hmacIconTitle);
		rom.write(hmacArm9i);
		rom.write(hmacArm7i);
		rom.write(hmacNdsReserved1);
		rom.write(hmacNdsReserved2);
		rom.write(hmacArm9WithoutSecureArea);

		rom.write(reserved9);

		rom.write(reserved10);

		rom.write(headerRsaSignature);
	}

	public void updateHeaderChecksum(DataIOStream rom) {
		try {
			logoChecksum = CRC16.CRC16(logo, 0, logo.length);
			secureAreaChecksum = CRC16.CRC16(rom, arm9RomOffset, 0x8000 - arm9RomOffset);
			DataIOStream buf = new DataIOStream(new byte[0x8000]);
			write(buf);

			headerChecksum = CRC16.CRC16(buf, 0x00, 0x15E);
		} catch (IOException ex) {
			Logger.getLogger(SRLHeader.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
