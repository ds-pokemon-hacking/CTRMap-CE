package ctrmap.formats.ntr.rom.srl;

import xstandard.fs.FSFile;
import xstandard.io.base.iface.DataOutputEx;

import java.io.IOException;
import java.util.List;

/**
 * This class represents the File Allocation Table
 */
public class FAT {

	public static void writeFAT(DataOutputEx rom, NitroDirectory root, int ofsBase, List<Integer> overlayStartOffsets, List<Integer> overlayEndOffsets) throws IOException {
		if (root.id == 0xf000) {
			for (int i = 0; i < overlayStartOffsets.size(); i++) {
				rom.writeInt(overlayStartOffsets.get(i));
				rom.writeInt(overlayEndOffsets.get(i));
			}
			writeFAT(rom, root, ofsBase);
		} else {
			throw new IOException("This is not the root directory!");
		}
	}

	/**
	 * Recursively write the FAT section of the ROM
	 *
	 * @param rom ROM binary stream
	 * @param root Root nitro directory of the ROM
	 * @throws IOException
	 */
	private static void writeFAT(DataOutputEx rom, NitroDirectory root, int ofsBase) throws IOException {
		for (NitroFile f : root.fileList) {
			rom.writeInt(f.offset + ofsBase);
			rom.writeInt(f.offset + f.size + ofsBase);
		}
		for (NitroDirectory d : root.directoryList) {
			writeFAT(rom, d, ofsBase);
		}
	}

	/**
	 * Pre-calculate the size of the FAT section starting from a path (overlays are excluded from calculation) Please note that the overlays aren't counted
	 *
	 * @param path Path of the folder to calculate the FAT
	 * @return Size in bytes of the FAT section
	 */
	public static int calculateFATSize(FSFile path) {
		int size = 0;
		for (FSFile child : path.listFiles()) {
			if (child.isDirectory()) {
				size += calculateFATSize(child);
			} else {
				size += 8;
			}
		}
		return size;
	}
}
