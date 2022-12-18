package ctrmap.formats.ntr.common.gfx.texture;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import java.io.IOException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.BitConverter;
import xstandard.io.util.IOUtils;
import xstandard.math.BitMath;

public class GETextureDecoder {

	public static byte[] decodeCompressed(int width, int height, byte[] data, byte[] indexData, short[] palette) {
		DataIOStream indexStream = new DataIOStream(indexData);

		byte[] data5a1 = new byte[width * height * 2];

		try {
			short[] blockColors = new short[4];
			int inOffs = 0;
			for (int y = 0; y < height; y += 4) {
				for (int x = 0; x < width; x += 4) {
					int blockData = BitConverter.toInt32LE(data, inOffs);
					int indexBitfield = indexStream.readUnsignedShort();
					int baseIndex = (indexBitfield & 0x3FFF) << 1;
					int mode = (indexBitfield >> 14) & 3;

					blockColors[0] = readPaletteColor(palette, baseIndex);
					blockColors[1] = readPaletteColor(palette, baseIndex + 1);
					switch (mode) {
						case 0:
							blockColors[2] = readPaletteColor(palette, baseIndex + 2);
							blockColors[3] = 0x7FFF;
							break;
						case 1:
							blockColors[2] = getColorAvg(blockColors[0], blockColors[1]);
							blockColors[3] = 0x7FFF;
							break;
						case 2:
							blockColors[2] = readPaletteColor(palette, baseIndex + 2);
							blockColors[3] = readPaletteColor(palette, baseIndex + 3);
							break;
						case 3:
							blockColors[2] = getColor53(blockColors[0], blockColors[1]);
							blockColors[3] = getColor53(blockColors[1], blockColors[0]);
							break;
					}

					for (int y2 = 0; y2 < 4; y2++) {
						for (int x2 = 0; x2 < 4; x2++) {
							BitConverter.fromInt16LE(
								col1555To5551(blockColors[BitMath.getIntegerBits(blockData, ((y2 * 4) + x2) * 2, 2)]),
								data5a1,
								TextureFormatHandler.getPixelByteOffset(x + x2, y + y2, width, 2)
							);
						}
					}
					inOffs += 4;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return data5a1;
	}

	public static byte[] decodeIndexed(int width, int height, byte[] data, GETextureFormat format, short[] palette, boolean firstIndexAlpha) {
		short col;
		int inOffs = 0;
		int outOffs = 0;
		int pixelsConverted = 0;
		DataIOStream input = new DataIOStream(data);
		byte[] data5a1 = new byte[width * height * 2];
		short[] paletteConv = new short[palette.length];
		for (int i = 0; i < palette.length; i++) {
			paletteConv[i] = col1555To5551((short) (palette[i] | 0x8000));
		}
		if (firstIndexAlpha) {
			paletteConv[0] = col1555To5551((short) 0x7FFF);
		}
		palette = paletteConv;
		try {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width;) {
					pixelsConverted = 0;
					switch (format) {
						case IDX8:
							col = input.readByte();
							BitConverter.fromInt16LE(palette[col & 0xFF], data5a1, outOffs);
							pixelsConverted = 1;
							break;
						case IDX4:
							col = input.readByte();
							BitConverter.fromInt16LE(palette[col & 0x0F], data5a1, outOffs);
							BitConverter.fromInt16LE(palette[(col & 0xF0) >> 4], data5a1, outOffs + 2);
							pixelsConverted = 2;
							break;
						case IDX2:
							col = input.readByte();
							BitConverter.fromInt16LE(palette[col & 0x3], data5a1, outOffs);
							BitConverter.fromInt16LE(palette[(col & 0xC) >> 2], data5a1, outOffs + 2);
							BitConverter.fromInt16LE(palette[(col & 0x30) >> 4], data5a1, outOffs + 4);
							BitConverter.fromInt16LE(palette[(col & 0xC0) >> 6], data5a1, outOffs + 6);
							pixelsConverted = 4;
							break;
					}

					outOffs += 2 * pixelsConverted;
					inOffs += (format.bpp * pixelsConverted) >> 3;
					x += pixelsConverted;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return data5a1;
	}

	public static byte[] decodeRGB5A1(int width, int height, byte[] data) {
		short col;
		int outOffs = 0;
		DataIOStream input = new DataIOStream(data);
		byte[] data5a1 = new byte[width * height * 2];
		try {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					col = input.readShort();
					BitConverter.fromInt16LE((((col & 0x1F) << 11) | ((col >> 9) & 0x3E) | ((col & 0x3E0) << 1) | 1), data5a1, outOffs);

					outOffs += 2;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return data5a1;
	}

	public static byte[] decodeAlpha(int width, int height, GETextureFormat format, byte[] data, short[] palette) {
		byte col;
		int outOffs = 0;
		DataIOStream input = new DataIOStream(data);
		byte[] dataRgba8 = new byte[width * height * 4];
		int[] paletteRGBA = new int[palette.length];
		for (int i = 0; i < palette.length; i++) {
			paletteRGBA[i] = GXColorToFRGB(palette[i]);
		}
		try {
			int index = 0;
			int alpha = 0;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					col = input.readByte();

					switch (format) {
						case A3I5:
							index = col & 31;
							alpha = bit3To8((col >> 5) & 7);
							break;
						case A5I3:
							index = col & 7;
							alpha = GXColor.bit5to8((col >> 3) & 31);
							break;
					}

					BitConverter.fromInt32LE((paletteRGBA[index] & 0xFFFFFF) | (alpha << 24), dataRgba8, outOffs);

					outOffs += 4;
				}
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return dataRgba8;
	}

	public static int GXColorToFRGB(short value) {
		return GXColor.bit5to8((value >> 0) & 0x1F) //B
			| (GXColor.bit5to8((value >> 5) & 0x1F) << 8) //G
			| (GXColor.bit5to8((value >> 10) & 0x1F) << 16) //R
			| (0xFF000000); //A - always 255
	}

	private static int bit3To8(int value) {
		return value << 5 | value << 2 | value >>> 1;
	}

	private static short readPaletteColor(short[] palette, int index) throws IOException {
		return (short) (palette[index] | 0x8000);
	}

	private static short getColorAvg(short col1, short col2) {
		return (short) ((((col1 & 0x1F) + (col2 & 0x1F)) >> 1)
			| ((((col1 & 0x3E0) + (col2 & 0x3E0)) >> 1) & 0x3E0)
			| ((((col1 & 0x7C00) + (col2 & 0x7C00)) >> 1) & 0x7C00)
			| 0x8000);
	}

	private static short getColor53(short col1, short col2) {
		return (short) (((((col1 & 0x1F) * 5) + ((col2 & 0x1F) * 3)) >> 3)
			| (((((col1 & 0x3E0) * 5) + ((col2 & 0x3E0) * 3)) >> 3) & 0x3E0)
			| (((((col1 & 0x7C00) * 5) + ((col2 & 0x7C00) * 3)) >> 3) & 0x7C00)
			| 0x8000);
	}

	private static short col1555To5551(short col) {
		return (short) (((col & 0x1F) << 11) | ((col >> 9) & 0x3E) | ((col & 0x3E0) << 1) | ((col >> 15) & 1));
	}
}
