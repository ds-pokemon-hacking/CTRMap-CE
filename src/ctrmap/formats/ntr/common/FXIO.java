package ctrmap.formats.ntr.common;

import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class FXIO {

	private static final float FX_DEFAULT_SCALE = 4096f;
	private static final float FX_DEFAULT_SCALE_INV = 1f / FX_DEFAULT_SCALE;

	private static final float FX_ANGLE_FULLRES = 360f / 65536f;
	private static final float FX_ANGLE_FULLRES_INV = 1f / FX_ANGLE_FULLRES;

	public static Vec3f readVecFX16(DataInput in) throws IOException {
		return readVecFX16(in, new Vec3f());
	}

	public static Vec3f readVecFX32(DataInput in) throws IOException {
		return readVecFX32(in, new Vec3f());
	}

	public static Vec3f readVecFX16(DataInput in, Vec3f dest) throws IOException {
		dest.x = readFX16(in);
		dest.y = readFX16(in);
		dest.z = readFX16(in);
		return dest;
	}

	public static Vec3f readVecFX32(DataInput in, Vec3f dest) throws IOException {
		dest.x = readFX32(in);
		dest.y = readFX32(in);
		dest.z = readFX32(in);
		return dest;
	}

	public static void writeVecFX16(DataOutput out, Vec3f vec) throws IOException {
		writeFX16(out, vec.x, vec.y, vec.z);
	}

	public static void writeVecFX32(DataOutput out, Vec3f vec) throws IOException {
		writeFX32(out, vec.x, vec.y, vec.z);
	}

	public static float readFX32(DataInput in) throws IOException {
		return FX.unfx32(in.readInt());
	}

	public static float readFX16(DataInput in) throws IOException {
		return FX.unfx16(in.readShort());
	}

	public static void writeFX16(DataOutput out, float FX16) throws IOException {
		out.writeShort(FX.fx16(FX16));
	}

	public static void writeFX16Round(DataOutput out, float FX16) throws IOException {
		out.writeShort((short) Math.round(FX16 * FX_DEFAULT_SCALE));
	}

	public static void writeFX32(DataOutput out, float FX32) throws IOException {
		out.writeInt(FX.fx32(FX32));
	}

	public static void writeFX16(DataOutput out, float... FX16) throws IOException {
		for (float flt : FX16) {
			out.writeShort(FX.fx16(flt));
		}
	}

	public static void writeFX32(DataOutput out, float... FX32) throws IOException {
		for (float flt : FX32) {
			out.writeInt(FX.fx32(flt));
		}
	}

	public static float readAngleDeg16(DataInput in) throws IOException {
		return in.readShort() * FX_ANGLE_FULLRES;
	}

	public static float readAngleDeg16Unsigned(DataInput in) throws IOException {
		return in.readUnsignedShort() * FX_ANGLE_FULLRES;
	}

	public static void writeAngleDeg16(DataOutput out, float angle) throws IOException {
		int bits = (int) (angle * FX_ANGLE_FULLRES_INV);
		bits &= 0xFFFF; //%65536
		out.writeShort(bits);
	}

	public static float readAngleDeg32(DataInput in) throws IOException {
		return in.readInt() * FX_ANGLE_FULLRES;
	}

	public static void writeAngleDeg32(DataOutput out, float angle) throws IOException {
		out.writeInt((int) (angle * FX_ANGLE_FULLRES_INV));
	}
}
