package ctrmap.formats.ntr.common.compression;

import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class BLZ {

	public static BLZHeader getBLZHeader(FSFile fsf) {
		try {
			DataIOStream io = fsf.getDataIOStream();
			BLZHeader hdr = new BLZHeader(io);
			io.close();
			return hdr;
		} catch (IOException ex) {
			Logger.getLogger(BLZ.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}
	
	/*
	Enhanced version of:
	https://github.com/pedro-javierf/NTRGhidra/blob/master/src/main/java/ntrghidra/CRT0.java
	 */
	public static byte[] BLZ_Decompress(byte[] data) {
		BLZHeader hdr = new BLZHeader(data);
		int decLen = hdr.getDecLength();

		byte[] out = Arrays.copyOf(data, decLen);

		int maxLen = hdr.pakLen - hdr.encLen;
		int srcOff = hdr.pakLen - hdr.hdrLen;

		int dstOff = decLen;
		
		byte header, a, b;
		int offs, stopOff;
		
		while (true) {

			header = out[--srcOff];

			for (int i = 0; i < 8; i++) {
				if ((header & 0x80) == 0) {
					out[--dstOff] = out[--srcOff];
				} else {
					a = out[--srcOff];
					b = out[--srcOff];

					offs = (((a & 0xF) << 8) | (b & 0xFF)) + 2;
					
					stopOff = dstOff - ((a >> 4 & 0xF) + 3);

					for (; dstOff > stopOff; dstOff--) {
						out[dstOff - 1] = out[dstOff + offs];
					}
				}

				if (srcOff <= maxLen) {
					return out;
				}

				header <<= 1;
			}
		}
	}

	public static Integer get32(byte[] b, int off) {
		return (b[off] & 0xFF) | ((b[off + 1] & 0xFF) << 8) | ((b[off + 2] & 0xFF) << 16) | ((b[off + 3] & 0xFF) << 24);
	}

	public static class BLZHeader {

		public int pakLen;

		public int encLen;
		public int hdrLen;
		public int incLen;

		public BLZHeader(DataIOStream io) throws IOException {
			pakLen = io.getLength();
			io.seek(pakLen - 0x8);
			encLen = io.readUnsignedInt24();
			hdrLen = io.read();
			incLen = io.readInt();
		}

		public BLZHeader(byte[] data) {
			pakLen = data.length;
			encLen = get32(data, pakLen - 8);
			hdrLen = (encLen >>> 24);
			encLen &= 0xFFFFFF;
			incLen = get32(data, pakLen - 4);
		}

		public int getDecLength() {
			return pakLen + incLen;
		}

		public boolean valid() {
			return (hdrLen >= 0x8 && hdrLen <= 0xb) && pakLen > hdrLen && getDecLength() <= 0xFFFFFF;
		}
	}
}
