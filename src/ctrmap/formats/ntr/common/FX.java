package ctrmap.formats.ntr.common;

import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Matrix3f;
import org.joml.Matrix4x3f;

public class FX {

	public static final float FX_DEFAULT_PRECISION = 4096f;

	public static final float FX_MIN = 1 / FX_DEFAULT_PRECISION;
	public static final float FX16_MAX = Short.MAX_VALUE / FX_DEFAULT_PRECISION;
	public static final float FX32_MAX = Integer.MAX_VALUE / FX_DEFAULT_PRECISION;

	public static final FX.VecFX32 ONE_VEC3 = new VecFX32(1.0F, 1.0F, 1.0F);
	public static final FX.VecFX32 ZERO_VEC3 = new VecFX32(0.0F, 0.0F, 0.0F);

	public static final FX.Vec2FX32 ONE_VEC2 = new Vec2FX32(1.0F, 1.0F);
	public static final FX.Vec2FX32 ZERO_VEC2 = new Vec2FX32(0.0F, 0.0F);

	private static int roundToZero(float f) {
		if (f < 0) {
			return (int) Math.ceil(f);
		} else {
			return (int) Math.floor(f);
		}
	}

	public static int fx32(float f) {
		return roundToZero(f * FX_DEFAULT_PRECISION);
	}

	public static short fx16(float f) {
		return (short) roundToZero(f * FX_DEFAULT_PRECISION);
	}

	public static int fx(float f, int fracBits) {
		return roundToZero(f * (1 << fracBits));
	}

	public static int fx(float f, int intBits, int fracBits) {
		return roundToZero(f * (1 << fracBits)) & ((1 << (fracBits + intBits)) - 1);
	}

	public static float unfx16(int val) {
		return val * FX_MIN;
	}
	
	public static float unfx32(int val) {
		return val * FX_MIN;
	}
	
	public static float unfx10(int val, int fracBits) {
		return (val << 22 >> 22) / (float) (1 << fracBits);
	}

	public static float unfx(int val, int intBits, int fracBits) {
		int shift = Integer.SIZE - (intBits + fracBits);
		return (val << shift >> shift) / (float) (1 << fracBits);
	}

	public static boolean fx_ImpreciseEquals(int a, int b, int precision) {
		return Math.abs(a - b) <= precision;
	}

	public static boolean allowFX16(float value) {
		return Math.abs(value) < FX16_MAX;
	}

	public static class VecFX16 {

		public short x;
		public short y;
		public short z;

		public VecFX16(Vec3f vec) {
			this(vec, 1f);
		}

		public VecFX16(Vec3f vec, float scale) {
			float mul = FX_DEFAULT_PRECISION * scale;
			x = (short) (vec.x * mul);
			y = (short) (vec.y * mul);
			z = (short) (vec.z * mul);
		}

		public void write(DataOutput out) throws IOException {
			out.writeShort(x);
			out.writeShort(y);
			out.writeShort(z);
		}
	}

	public static class VecFX32 {

		public int x;
		public int y;
		public int z;

		public VecFX32(Vec3f vec) {
			this(vec, 1f);
		}

		public VecFX32(float x, float y, float z) {
			this.x = fx32(x);
			this.y = fx32(y);
			this.z = fx32(z);
		}

		public VecFX32(Vec3f vec, float scale) {
			float mul = FX_DEFAULT_PRECISION * scale;
			x = (int) (vec.x * mul);
			y = (int) (vec.y * mul);
			z = (int) (vec.z * mul);
		}

		public void write(DataOutput out) throws IOException {
			out.writeInt(x);
			out.writeInt(y);
			out.writeInt(z);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof VecFX32) {
				if (obj == this) {
					return true;
				}
				VecFX32 vec = (VecFX32) obj;
				return vec.x == x && vec.y == y && vec.z == z;
			}
			return false;
		}
	}

	public static class Vec2FX32 {

		public int x;
		public int y;

		public Vec2FX32(Vec2f vec) {
			this(vec, 1f);
		}

		public Vec2FX32(float x, float y) {
			this.x = fx32(x);
			this.y = fx32(y);
		}

		public Vec2FX32(Vec2f vec, float scale) {
			float mul = FX_DEFAULT_PRECISION * scale;
			x = (int) (vec.x * mul);
			y = (int) (vec.y * mul);
		}

		public void write(DataOutput out) throws IOException {
			out.writeInt(x);
			out.writeInt(y);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof Vec2FX32) {
				if (obj == this) {
					return true;
				}
				Vec2FX32 vec = (Vec2FX32) obj;
				return vec.x == x && vec.y == y;
			}
			return false;
		}
	}

	private static class MatFX16 {

		private short[] matrix;

		private MatFX16(float[] floats) {
			matrix = new short[floats.length];
			for (int i = 0; i < floats.length; i++) {
				matrix[i] = fx16(floats[i]);
			}
		}

		public void write(DataOutput out) throws IOException {
			for (short elem : matrix) {
				out.writeShort(elem);
			}
		}

		public short get(int pos) {
			return matrix[pos];
		}
	}

	private static class MatFX32 {

		private int[] matrix;

		private MatFX32(float[] floats) {
			matrix = new int[floats.length];
			for (int i = 0; i < floats.length; i++) {
				matrix[i] = fx32(floats[i]);
			}
		}

		public void write(DataOutput out) throws IOException {
			for (int elem : matrix) {
				out.writeInt(elem);
			}
		}

		public int get(int pos) {
			return matrix[pos];
		}
	}

	public static class Mat3x3FX16 extends MatFX16 {

		public Mat3x3FX16(Matrix3f mat) {
			super(mat.get(new float[9]));
		}
	}

	public static class Mat3x3FX32 extends MatFX32 {

		public Mat3x3FX32(Matrix3f mat) {
			super(mat.get(new float[9]));
		}
	}

	public static class Mat4x3FX32 extends MatFX32 {

		public Mat4x3FX32(Matrix4x3f mat) {
			super(mat.get(new float[12]));
		}
	}
}
