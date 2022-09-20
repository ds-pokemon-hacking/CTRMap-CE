package ctrmap.renderer.util.texture;

import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormat;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import xstandard.math.MathEx;
import java.util.Arrays;

public class TextureCodec {

	public static final int[] PICA_TILE_ORDER = {0, 1, 8, 9, 2, 3, 10, 11, 16, 17, 24, 25, 18, 19, 26, 27, 4, 5, 12, 13, 6, 7, 14, 15, 20, 21, 28, 29, 22, 23, 30, 31, 32, 33, 40, 41, 34, 35, 42, 43, 48, 49, 56, 57, 50, 51, 58, 59, 36, 37, 44, 45, 38, 39, 46, 47, 52, 53, 60, 61, 54, 55, 62, 63};

	public static byte[] trimPowerDim(byte[] data, int width, int height, TextureFormatHandler fmt) {
		if (fmt.originFormat == TextureFormat.ETC1 || fmt.originFormat == TextureFormat.ETC1A4) {
			return data;
		}

		int alignedWidth = nlpo2(width);
		int alignedHeight = nlpo2(height);
		if (alignedWidth == width && alignedHeight == height) {
			return data;
		} else {
			int fullStrideX = (alignedWidth * fmt.getOriginBitsPP()) >> 3;
			int actualStrideX = (width * fmt.getOriginBitsPP()) >> 3;
			byte[] out = new byte[(width * height * fmt.getOriginBitsPP()) >> 3];
			int outOff = 0;
			int inOff = 0;
			for (int y = 0; y < height; y++) {
				System.arraycopy(data, inOff, out, outOff, actualStrideX);
				inOff += fullStrideX;
				outOff += actualStrideX;
			}
			return out;
		}
	}

	public static byte[] unscramblePICA(byte[] data, int width, int height, TextureFormatHandler fmt) {
		if (fmt.originFormat == TextureFormat.ETC1 || fmt.originFormat == TextureFormat.ETC1A4) {
			return data;
		}

		//H3D RGBA8 is actually ABGR8, similar for RGBA4
		int bpp = fmt.getNativeBPP();
		switch (fmt) {
			case RGBA8: //ARGB-BE
				for (int offs = 0; offs < data.length; offs += bpp) {
					byte a = data[offs];
					byte b = data[offs + 1];
					byte g = data[offs + 2];
					byte r = data[offs + 3];
					data[offs] = r;
					data[offs + 1] = g;
					data[offs + 2] = b;
					data[offs + 3] = a;
				}
				break;
			case RGB8:
				//BGR to RGB
				for (int offs = 0; offs < data.length; offs += bpp) {
					byte r = data[offs];
					data[offs] = data[offs + 2];
					data[offs + 2] = r;
				}
				break;
			case L8A8:
				for (int offs = 0; offs < data.length; offs += bpp) {
					byte r = data[offs];
					data[offs] = data[offs + 1];
					data[offs + 1] = r;
				}
				break;
			default:
				break;
		}

		int dataOffset = 0;
		byte[] output = new byte[data.length];

		for (int tY = 0; tY < height / 8; tY++) {
			for (int tX = 0; tX < width / 8; tX++) {
				for (int pixel = 0; pixel < 64; pixel++) {
					int x = PICA_TILE_ORDER[pixel] % 8;
					int y = (PICA_TILE_ORDER[pixel] - x) / 8;

					int outputOffset = ((tX * 8) + x + ((tY * 8 + y) * width)) * bpp;

					//if ((dataOffset + bpp <= data.length) && (outputOffset + bpp <= output.length)) {
					System.arraycopy(data, dataOffset, output, outputOffset, bpp);
					//}

					dataOffset += bpp;
				}
			}
		}

		return output;
	}

	private static final float _1_255 = 1 / 255f;

	public static void texsRGBToRGB(byte[] texData) {
		for (int i = 0; i < texData.length; i++) {
			texData[i] = (byte) (255f * (srgbToRgbComp((texData[i] & 0xFF) * _1_255)));
		}
	}

	/*
	https://unlimited3d.wordpress.com/2020/01/08/srgb-color-space-in-opengl/
	 */
	private static float srgbToRgbComp(float thesRGBValue) {
		return thesRGBValue <= 0.04045f
			? thesRGBValue / 12.92f
			: (float) Math.pow((thesRGBValue + 0.055f) / 1.055f, 2.4f);
	}

	public static int nlpo2(int x) {
		//PK3DS/XLIMUtil.cs
		x--;
		x |= x >> 1;
		x |= x >> 2;
		x |= x >> 4;
		x |= x >> 8;
		x |= x >> 16;
		return x + 1;
	}

	public static byte[] fourToEightBits(byte[] texturedata) {
		return fourToEightBits(texturedata, false);
	}

	public static byte[] fourToEightBits(byte[] texturedata, boolean reverseBO) {
		byte[] ret = new byte[texturedata.length * 2];
		for (int i = 0; i < texturedata.length; i++) {
			int offs = i * 2;
			ret[offs + (reverseBO ? 0 : 1)] = (byte) (texturedata[i] & 0xF0 | ((texturedata[i] >> 4) & 0x0F));		//Left byte
			ret[offs + (reverseBO ? 1 : 0)] = (byte) ((texturedata[i] << 4) | (texturedata[i] & 0x0F));		//Right byte
		}
		return ret;
	}

	public static byte[] getRGBA(Texture tex, TextureFormatHandler format) {
		int inBPP = format.getNativeBPP();
		int outBPP = TextureFormatHandler.RGBA8.getNativeBPP();
		byte[] data = format.getOnTheFlyNativeConvTexData(tex);
		if (format.nativeFormat == TextureFormat.RGBA8) {
			return data;
		}
		byte[] out = new byte[tex.width * tex.height * outBPP];

		int inOffset = 0;
		int outOffset = 0;

		int inSize = tex.width * inBPP;
		int inStride = MathEx.padInteger(inSize, 4);

		for (int y = 0; y < tex.height; y++) {
			for (int x = 0; x < tex.width; x++) {
				switch (format.nativeFormat) {
					case NV_HILO8:
					case RGB8:
						out[outOffset + 3] = (byte) 0xFF;
					case RGBA8:
						System.arraycopy(data, inOffset, out, outOffset, inBPP);
						break;
					case RGBA4:
						byte[] src = fourToEightBits(Arrays.copyOfRange(data, inOffset, inOffset + inBPP));
						out[outOffset] = src[3];
						out[outOffset + 1] = src[2];
						out[outOffset + 2] = src[1];
						out[outOffset + 3] = src[0];
						break;
					case L4A4:
						setLuminanceAlpha(expand4(data[inOffset] & 0xf), expand4((data[inOffset] & 0xF0) >> 4), out, outOffset);
						break;
					case A8:
						setLuminanceAlpha(0xff, data[inOffset], out, outOffset);
						break;
					case L8:
						setLuminanceAlpha(data[inOffset], 0xff, out, outOffset);
						break;
					case L8A8:
						setLuminanceAlpha(data[inOffset], data[inOffset + 1], out, outOffset);
						break;
					case RGB565:
						decodeRGB565(out, outOffset, getShort(data, inOffset));
						break;
					case RGB5A1:
						decodeRGBA5551(out, outOffset, getShort(data, inOffset));
						break;
					case FLOAT32:
						setLuminanceAlpha((int) (255 * getFP32(data, inOffset)), 0xff, out, outOffset);
						break;
				}

				inOffset += inBPP;
				outOffset += outBPP;
			}
			//inOffset += (inStride - inSize);
		}
		return out;
	}

	private static int expand4(int bits4) {
		return (bits4 << 4) | bits4;
	}

	private static float getFP32(byte[] b, int offset) {
		return Float.intBitsToFloat((b[offset] & 0xFF) | ((b[offset + 1] & 0xFF) << 8) | ((b[offset + 2] & 0xFF) << 16) | ((b[offset + 3] & 0xFF) << 24));
	}

	private static void setLuminanceAlpha(int l, int a, byte[] out, int outOffset) {
		out[outOffset] = (byte) l;
		out[outOffset + 1] = (byte) l;
		out[outOffset + 2] = (byte) l;
		out[outOffset + 3] = (byte) a;
	}

	private static void decodeRGBA5551(byte[] out, int offset, short value) {
		int B = ((value >> 1) & 0x1f) << 3;
		int G = ((value >> 6) & 0x1f) << 3;
		int R = ((value >> 11) & 0x1f) << 3;

		out[offset] = (byte) (R | (R >>> 5));
		out[offset + 1] = (byte) (G | (G >>> 5));
		out[offset + 2] = (byte) (B | (B >>> 5));
		out[offset + 3] = (byte) ((value & 1) * 0xff);
	}

	private static void decodeRGB565(byte[] out, int offset, short value) {
		int B = (value & 0x1f) << 3;
		int G = ((value >> 5) & 0x3f) << 2;
		int R = ((value >> 11) & 0x1f) << 3;

		out[offset] = (byte) (R | (R >>> 5));
		out[offset + 1] = (byte) (G | (G >>> 6));
		out[offset + 2] = (byte) (B | (B >>> 5));
		out[offset + 3] = (byte) 0xFF;
	}

	private static short getShort(byte[] data, int offset) {
		return (short) (data[offset] & 0xFF
			| (data[offset + 1] << 8 & 0xFF00));
	}

	public static int clamp2047(int x) {
		if (x < 0) {
			return 0;
		}
		if (x > 2047) {
			return 2047;
		}
		return x;
	}

	public static int clamp1023_signed(int x) {
		if (x < - 1023) {
			return - 1023;
		}
		if (x > 1023) {
			return 1023;
		}
		return x;
	}

	public static int saturate(int value) {
		if (value > 0xff) {
			return 0xff;
		}
		if (value < 0) {
			return 0;
		}
		return value & 0xff;
	}

}
