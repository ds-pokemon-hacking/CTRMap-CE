package ctrmap.renderer.util.texture.compressors;

import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.util.texture.TextureCodec;
import xstandard.io.util.IOUtils;
import xstandard.math.MathEx;

/**
 *
 */
public class ETC1 {

	private static final int[][] ETC1_LUT = {
		{2, 8, -2, -8},
		{5, 17, -5, -17},
		{9, 29, -9, -29},
		{13, 42, -13, -42},
		{18, 60, -18, -60},
		{24, 80, -24, -80},
		{33, 106, -33, -106},
		{47, 183, -47, -183}
	};

	public static byte[] etc1DecodeNew(byte[] data, int width, int height, TextureFormatHandler format) {
		/*
		https://github.com/Gericom/EveryFileExplorer/blob/master/3DS/GPU/Textures.cs
		 */
		int bpp = format.getNativeBPP();
		boolean hasAlpha = format == TextureFormatHandler.ETC1A4;
		byte[] output = new byte[bpp * width * height];
		int offs = 0;
		int outOffs = 0;
		int realWidth = width;
		int realHeight = height;
		int stride = bpp * width;
		int alignedWidth = TextureCodec.nlpo2(width);
		int alignedHeight = TextureCodec.nlpo2(height);
		for (int y = 0; y < alignedHeight; y += 8) {
			for (int x = 0; x < alignedWidth; x += 8) {
				for (int y2 = 0; y2 < 8; y2 += 4) {
					for (int x2 = 0; x2 < 8; x2 += 4) {
						long alpha = -1L;
						if (hasAlpha) {
							alpha = IOUtils.byteArrayToLongLE(data, offs);
							offs += 8;
						}
						long block = IOUtils.byteArrayToLongLE(data, offs);
						boolean diffbit = ((block >> 33) & 1) == 1;
						boolean flipbit = ((block >> 32) & 1) == 1; //0: |||, 1: |-|
						int r1;
						int r2;
						int g1;
						int g2;
						int b1;
						int b2;
						if (diffbit) {
							int r = (int) ((block >>> 59) & 31);
							int g = (int) ((block >> 51) & 31);
							int b = (int) ((block >> 43) & 31);
							r1 = (r << 3) | ((r & 28) >> 2);
							g1 = (g << 3) | ((g & 28) >> 2);
							b1 = (b << 3) | ((b & 28) >> 2);
							r += (int) ((block >>> 56) & 7) << 29 >> 29;
							g += (int) ((block >>> 48) & 7) << 29 >> 29;
							b += (int) ((block >>> 40) & 7) << 29 >> 29;
							r2 = (r << 3) | ((r & 28) >> 2);
							g2 = (g << 3) | ((g & 28) >> 2);
							b2 = (b << 3) | ((b & 28) >> 2);
						} else //'individual' mode
						{
							r1 = (int) ((block >>> 60) & 15) * 17;
							g1 = (int) ((block >>> 52) & 15) * 17;
							b1 = (int) ((block >>> 44) & 15) * 17;
							r2 = (int) ((block >>> 56) & 15) * 17;
							g2 = (int) ((block >>> 48) & 15) * 17;
							b2 = (int) ((block >>> 40) & 15) * 17;
						}
						int table1 = (int) ((block >>> 37) & 7);
						int table2 = (int) ((block >>> 34) & 7);
						for (int y3 = 0; y3 < 4; y3++) {
							for (int x3 = 0; x3 < 4; x3++) {
								if (x + x2 + x3 >= realWidth) {
									continue;
								}
								if (y + y2 + y3 >= realHeight) {
									continue;
								}
								int val = (int) ((block >>> (x3 * 4 + y3)) & 1);
								int neg = (int) ((block >>> (x3 * 4 + y3 + 16)) & 1) * 2;
								byte r;
								byte g;
								byte b;
								byte a = (byte) (((alpha >>> ((x3 * 4 + y3) * 4)) & 15) * 17);
								if ((flipbit && y3 < 2) || (!flipbit && x3 < 2)) {
									int add = ETC1_LUT[table1][val + neg];
									r = (byte) TextureCodec.saturate(r1 + add);
									g = (byte) TextureCodec.saturate(g1 + add);
									b = (byte) TextureCodec.saturate(b1 + add);
								} else {
									int add = ETC1_LUT[table2][val + neg];
									r = (byte) TextureCodec.saturate(r2 + add);
									g = (byte) TextureCodec.saturate(g2 + add);
									b = (byte) TextureCodec.saturate(b2 + add);
								}
								int outPos = outOffs + (y2 + y3) * stride + (x + x2 + x3) * bpp;
								output[outPos] = r;
								output[outPos + 1] = g;
								output[outPos + 2] = b;
								if (hasAlpha) {
									output[outPos + 3] = a;
								}
							}
						}
						offs += 8;
					}
				}
			}
			outOffs += stride * 8;
		}
		return output;
	}

}
