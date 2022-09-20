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

import xstandard.crypto.CRC16;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.base.impl.ext.data.DataIOStream;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class handles the cartdridge header.
 */
public class NitroHeader {
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

    /**
     * Read the header
     *
     * @param rom The stream where to read the information
     * @return A header
     * @throws IOException If something goes wrong
     */
    public static NitroHeader readHeader(DataInputEx rom) throws IOException {
        NitroHeader header = new NitroHeader();
        header.gameTitle = rom.readPaddedString(12);
        header.gameCode = rom.readPaddedString(4);
        header.makerCode = rom.readPaddedString(2);
        header.unitCode = rom.readByte();
        header.encryptionSeedSelect = rom.readByte();
        header.deviceCapacity = rom.readByte();
        header.reserved1 = rom.readBytes(7);
        header.dsiFlags = rom.readByte();
        header.ndsRegion = rom.readByte();
        header.romVersion = rom.readByte();
        header.autoStart = rom.readByte();

        header.arm9RomOffset = rom.readInt();
        header.arm9EntryAddress = rom.readInt();
        header.arm9RamAddress = rom.readInt();
        header.arm9Size = rom.readInt();

        header.arm7RomOffset = rom.readInt();
        header.arm7EntryAddress = rom.readInt();
        header.arm7RamAddress = rom.readInt();
        header.arm7Size = rom.readInt();

        header.fntOffset = rom.readInt();
        header.fntSize = rom.readInt();
        header.fatOffset = rom.readInt();
        header.fatSize = rom.readInt();

        header.arm9OverlayOffset = rom.readInt();
        header.arm9OverlaySize = rom.readInt();

        header.arm7OverlayOffset = rom.readInt();
        header.arm7OverlaySize = rom.readInt();

        header.port40001A4hNormalCommand = rom.readInt();
        header.port40001A4hKey1Command = rom.readInt();

        header.iconOffset = rom.readInt();
        header.secureAreaChecksum = rom.readShort();
        header.secureAreaDelay = rom.readShort();
        header.arm9AutoLoad = rom.readInt();
        header.arm7AutoLoad = rom.readInt();
        header.secureAreaDisable = rom.readLong();
        header.usedRomSize = rom.readInt();
        header.headerSize = rom.readInt();
        header.reserved2 = rom.readBytes(0x28);
        header.reserved3 = rom.readBytes(0x10);

        header.logo = rom.readBytes(0x9c);
        header.logoChecksum = rom.readShort();
        header.headerChecksum = rom.readShort();
        header.debugRomOffset = rom.readInt();
        header.debugSize = rom.readInt();
        header.debugRamAddress = rom.readInt();
        header.reserved4 = rom.readInt();
        header.reserved5 = rom.readBytes(0x90);
        return header;
    }

    /**
     * Write the header
     *
     * @param header The header to write
     * @param rom    The stream where to write the information
     */
    public static void writeHeader(NitroHeader header, DataOutputEx rom) throws IOException {
        rom.writePaddedString(header.gameTitle, 12);
        rom.writePaddedString(header.gameCode, 4);
        rom.writePaddedString(header.makerCode, 2);
        rom.writeByte(header.unitCode);
        rom.writeByte(header.encryptionSeedSelect);
        rom.writeByte(header.deviceCapacity);
        rom.write(header.reserved1);
        rom.writeByte(header.dsiFlags);
        rom.writeByte(header.ndsRegion);
        rom.writeByte(header.romVersion);
        rom.writeByte(header.autoStart);

        rom.writeInt(header.arm9RomOffset);
        rom.writeInt(header.arm9EntryAddress);
        rom.writeInt(header.arm9RamAddress);
        rom.writeInt(header.arm9Size);

        rom.writeInt(header.arm7RomOffset);
        rom.writeInt(header.arm7EntryAddress);
        rom.writeInt(header.arm7RamAddress);
        rom.writeInt(header.arm7Size);

        rom.writeInt(header.fntOffset);
        rom.writeInt(header.fntSize);
        rom.writeInt(header.fatOffset);
        rom.writeInt(header.fatSize);

        rom.writeInt(header.arm9OverlayOffset);
        rom.writeInt(header.arm9OverlaySize);

        rom.writeInt(header.arm7OverlayOffset);
        rom.writeInt(header.arm7OverlaySize);

        rom.writeInt(header.port40001A4hNormalCommand);
        rom.writeInt(header.port40001A4hKey1Command);

        rom.writeInt(header.iconOffset);
        rom.writeShort(header.secureAreaChecksum);
        rom.writeShort(header.secureAreaDelay);
        rom.writeInt(header.arm9AutoLoad);
        rom.writeInt(header.arm7AutoLoad);
        rom.writeLong(header.secureAreaDisable);
        rom.writeInt(header.usedRomSize);
        rom.writeInt(header.headerSize);
        rom.write(header.reserved2);
        rom.write(header.reserved3);

        rom.write(header.logo);
        rom.writeShort(header.logoChecksum);
        rom.writeShort(header.headerChecksum);
        rom.writeInt(header.debugRomOffset);
        rom.writeInt(header.debugSize);
        rom.writeInt(header.debugRamAddress);
        rom.writeInt(header.reserved4);
        rom.write(header.reserved5);
    }

    public static void updateHeaderChecksum(NitroHeader header, DataIOStream rom) {
		try {
			header.logoChecksum = CRC16.CRC16(header.logo, 0, header.logo.length);
			DataIOStream buf = new DataIOStream(new byte[0x8000]);
			writeHeader(header, buf);
			
			header.headerChecksum = CRC16.CRC16(buf, 0x00, 0x15E);
			header.secureAreaChecksum = CRC16.CRC16(rom, header.arm9RomOffset, 0x8000 - header.arm9RomOffset);
		} catch (IOException ex) {
			Logger.getLogger(NitroHeader.class.getName()).log(Level.SEVERE, null, ex);
		}
    }
}
