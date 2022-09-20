package ctrmap.formats.ntr.rom;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OverlayCrypto {

	public static void main(String[] args) {
		String ovlFilePath = "D:\\_REWorkspace\\pokescript_genv\\_CUTSCENE_IDB\\overlay\\overlay_0337 - kopie.bin";
		int ovlBaseAddr = 0x217F640;

		int[] CRYPTO_ENTRY_PTRS = new int[]{
			0x2182400,
			0x2180F64,
			0x217FD6C,
			0x21802AC,
			0x2180C50,
		};

		try (DataIOStream io = new DataIOStream(new File(ovlFilePath))) {
			io.setBase(ovlBaseAddr);

			int end = io.getLength();
			
			int totalDecrypted = 0;

			for (int crypto : CRYPTO_ENTRY_PTRS) {
				System.out.println("---------Reading crypto table " + Integer.toHexString(crypto));
				io.seek(crypto);

				while (true) {
					CryptoEntry e = new CryptoEntry();
					io.checkpoint();
					e.start = io.readInt();
					if (e.start != 0) {
						e.end = io.readInt();
						System.out.println("Entry data " + Integer.toHexString(e.start) + " | " + Integer.toHexString(e.end));

						int actualStart = e.start - 0x2200;
						System.out.println("start " + Integer.toHexString(actualStart));
						int actualEnd = actualStart + (e.end - 0x2200 - 0xC - end);
						System.out.println("actual end " + Integer.toHexString(actualEnd));
						int wordCount = (actualEnd - actualStart) >> 2;
						System.out.println("decrypting " + wordCount + " words ... at " + Integer.toHexString(actualStart));

						io.seek(actualStart);
						int decKey = 0xA471ABB;
						int dec;
						for (int i = 0; i < wordCount; i++) {
							dec = io.readInt() ^ decKey;
							io.skipBytes(-4);
							io.writeInt(dec);
							decKey ^= dec - (dec >>> 8);
						}
						totalDecrypted += wordCount;
						
						io.seek(actualStart);
						int checksum = 0;
						for (int i = wordCount; i >= 1; i--) {
							checksum ^= Integer.rotateRight(io.readInt(), i);
						}
						System.out.println("Checksum: " + Integer.toHexString(checksum));
						
						io.resetCheckpoint();
						io.writeInt(0);
						io.writeInt(0);
					} else {
						break;
					}
				}
			}
			
			System.out.println("Done. Decrypted 0x" + Integer.toHexString(totalDecrypted << 2) + " bytes.");
		} catch (IOException ex) {
			Logger.getLogger(OverlayCrypto.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static class CryptoEntry {

		public int start;
		public int end;
	}
}
