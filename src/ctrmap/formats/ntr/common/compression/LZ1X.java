package ctrmap.formats.ntr.common.compression;

import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;

/**
 *
 */
public class LZ1X {

	public static byte[] decompress(ReadableStream in) throws IOException {
		DataInStream dis = new DataInStream(in);
		int streamHeader = dis.readInt();
		ReadableStream data = in;
		int uncompSize = streamHeader >> 8;
		boolean allowExDisp = (streamHeader & 0xF) != 0;
		boolean breakLoop;
		int copySizeBase;
		byte[] out = new byte[uncompSize];
		int outIndex = 0;
		while (uncompSize > 0) {
			int header = in.read();
			int bitIndex = 8;
			int byte2;
			while (true) {
				breakLoop = bitIndex-- < 1;
				if (breakLoop) {
					break;
				}
				if ((header & 0x80) != 0) {
					int byte1 = data.read();
					if (allowExDisp) {
						if ((byte1 & 0xE0) != 0) {
							copySizeBase = 1;
						} else {
							int disp16bit = (byte1 & 0xF) << 4;     // (byte1 & 0xF) << 4
							if ((byte1 & 0x10) != 0) {
								byte2 = data.read();
								disp16bit = ((byte1 & 0xF) << 12) + 16 * byte2 + 256;// 256 + (byte2 << 4) + ((byte1 & 0xF) << 12) //unpack to 16 bits of displacement
							}
							copySizeBase = disp16bit + 17;
							byte1 = data.read();
						}
					} else {
						copySizeBase = 3;
					}
					int bufCopySize = copySizeBase + (byte1 >> 4);
					int lastByte = data.read();
					int displacement = (lastByte | ((byte1 & 0xF) << 8)) + 1;
					uncompSize -= bufCopySize;
					int o = outIndex;
					for (int i = 0; i <= bufCopySize / displacement; i++, o += displacement) {
						System.arraycopy(out, outIndex - displacement, out, o, Math.min(displacement, bufCopySize - i * displacement));
					}
					outIndex += bufCopySize;
					/*do {
						out[outIndex] = out[outIndex - displacement];
						outIndex++;
						breakLoop = bufCopySize-- <= 1;
					} while (!breakLoop);*/
				} else {
					out[outIndex] = (byte) data.read();
					outIndex++;
					--uncompSize;
				}
				if (uncompSize <= 0) {
					break;
				}
				header <<= 1;
			}                                           // header <<= 1
		}
		return out;
	}
}
