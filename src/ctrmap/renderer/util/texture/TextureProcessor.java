package ctrmap.renderer.util.texture;

import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import xstandard.io.util.IOUtils;
import xstandard.math.vec.RGBA;
import xstandard.util.collections.IntList;

public class TextureProcessor {

	public static boolean isGrayScale(byte[] rgba){
		for (int i = 0; i < rgba.length; i += 4){
			if (rgba[i] != rgba[i + 1] || rgba[i] != rgba[i + 2]){
				return false;
			}
		}
		return true;
	}
	
	public static boolean hasVaryingAlpha(byte[] rgba){
		return getConstantAlphaValue(rgba) == -1;
	}
	
	public static short[] getAlphaChannel(byte[] rgba){
		return getChannel(rgba, 3);
	}
	
	public static short[] getChannel(byte[] rgba, int num){
		short[] ch = new short[rgba.length / 4];
		for (int i = num, j = 0; i < rgba.length; i += 4, j++){
			ch[j] = (short)(rgba[i] & 0xFF);
		}
		return ch;
	}
	
	public static int[] getUniqueAlphaValues(short[] alphaChannel) {
		IntList alphas = new IntList();
		int a;
		for (int i = 0; i < alphaChannel.length; i++) {
			a = alphaChannel[i];
			if (!alphas.contains(a)) {
				alphas.add(a);
			}
		}
		return alphas.toArray();
	}
	
	public static int[] getUniqueAlphaValues(byte[] rgba){
		IntList alphas = new IntList();
		
		int a;
		for (int i = 3; i < rgba.length; i += 4){
			a = rgba[i] & 0xFF;
			if (!alphas.contains(a)){
				alphas.add(a);
			}
		}
		
		return alphas.toArray();
	}
	
	public static int getConstantAlphaValue(int[] alphaValues) {
		if (alphaValues.length > 0){
			int a = alphaValues[0];
			for (int i = 1; i < alphaValues.length; i++){
				if (alphaValues[i] != a){
					return -1;
				}
			}
			return a;
		}
		return -1;
	}
	
	public static int getConstantAlphaValue(byte[] rgba) {
		int startA = rgba[3];
		for (int i = 7; i < rgba.length; i += 4) {
			if (rgba[i] != startA) {
				return -1;
			}
		}
		return startA & 0xFF;
	}
	
	public static RGBA[] getUniqueColors(byte[] rgba, boolean alpha) {
		IntList rgbs = new IntList();
		alpha = !alpha;
		
		int rgb;
		for (int i = 0; i < rgba.length; i += 4){
			rgb = IOUtils.byteArrayToIntegerLE(rgba, i);
			if (alpha){
				rgb &= 0xFFFFFF;
			}
			if (!rgbs.contains(rgb)){
				rgbs.add(rgb);
			}
		}
		
		RGBA[] colors = new RGBA[rgbs.size()];
		for (int i = 0; i < colors.length; i++){
			colors[i] = new RGBA(rgbs.get(i), false);
		}
		return colors;
	}

	public static byte[] crop(byte[] data, int sw, int sh, int dw, int dh, TextureFormatHandler format) {
		int bitpp = format.getOriginBitsPP();
		byte[] outData = new byte[(dw * dh * bitpp) >> 3];
		int dataPos = 0;
		int outPos = 0;
		int scanSize = (dw * bitpp) >> 3;
		int discardSize = (sw * bitpp) >> 3;
		for (int line = 0; line < dh; line++) {
			System.arraycopy(data, dataPos, outData, outPos, scanSize);
			dataPos += discardSize;
			outPos += scanSize;
		}
		return outData;
	}

	public static byte[] flipImageData(int width, int height, byte[] nativedata, TextureFormatHandler format) {
		int bpp = format.getNativeBPP();
		byte[] flip = new byte[width * height * bpp]; //flip for OpenGL and convert to RGBA
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = (y * width + x) * bpp;
				int inOffset = ((height - y - 1) * width + x) * bpp;
				smallArraycopy(nativedata, inOffset, flip, offset, bpp);
			}
		}
		return flip;
	}
	
	private static void smallArraycopy(byte[] source, int srcPos, byte[] dest, int destPos, int count) {
		for (int i = 0; i < count; i++) {
			dest[destPos++] = source[srcPos++];
		}
	}
}
