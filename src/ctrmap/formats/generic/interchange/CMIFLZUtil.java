package ctrmap.formats.generic.interchange;

//import ctrmap.formats.pokemon.gen6.garc.lz.LZ11;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class CMIFLZUtil {

	public static void writeLZ(byte[] data, boolean compressLZSS, DataOutput out) throws IOException {
		compressLZSS = false;
		if (compressLZSS) {
			//data = LZ11.compress(data);
		}
		out.writeInt(data.length | ((compressLZSS ? 1 : 0) << 31));
		out.write(data);
	}

	public static byte[] readLZ(DataInput in, int fileVersion) throws IOException {
		int len = in.readInt();
		boolean decompressLZSS = (fileVersion >= Revisions.REV_BUFFER_COMP_LZ && ((len >>> 31 & 1) > 0));
		len &= 0x7FFFFFFF;
		byte[] data = new byte[len];
		in.readFully(data);
		if (decompressLZSS) {
			//data = LZ11.decompress(data);
			throw new IOException("LZ compression no longer supported!");
		}
		return data;
	}
}
