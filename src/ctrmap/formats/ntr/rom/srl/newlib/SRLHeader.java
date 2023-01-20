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

	public int usedRomSize;
	public int headerSize;

	byte[] unknownDSi;

	/**
	 * These two fields are a doozy. Pokemon White 2 refuses to read files past this point, so they both have
	 * to be adjusted properly. (see 0x207215C in W2U ARM9, took me ages to debug all the way there). However,
	 * our implementation doesn't care about the DSi parts, so they'll just be set to the end of the ROM.
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
		unknownDSi = rom.readBytes(0x8);
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
		reserved5 = rom.readBytes(0x90);
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
		rom.write(unknownDSi);
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
