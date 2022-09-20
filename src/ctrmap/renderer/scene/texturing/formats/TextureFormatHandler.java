package ctrmap.renderer.scene.texturing.formats;

import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.util.texture.TextureCodec;

public enum TextureFormatHandler {
	RGBA8(TextureFormat.RGBA8),
	RGB8(TextureFormat.RGB8),
	RGBA4(TextureFormat.RGBA4),
	RGB565(TextureFormat.RGB565),
	RGB5A1(TextureFormat.RGB5A1),
	
	A4(TextureFormat.A4, TextureFormat.A8),
	L4(TextureFormat.L4, TextureFormat.L8),
	A8(TextureFormat.A8),
	L8(TextureFormat.L8),
	L4A4(TextureFormat.L4A4, TextureFormat.L8A8),
	L8A8(TextureFormat.L8A8),
	
	ETC1(TextureFormat.ETC1, TextureFormat.RGB8),
	ETC1A4(TextureFormat.ETC1A4, TextureFormat.RGBA8),
	
	HILO8(TextureFormat.NV_HILO8),
	
	LUMINANCE_F32(TextureFormat.FLOAT32);
	
	public final TextureFormat originFormat;
	public final TextureFormat nativeFormat;
	
	private TextureFormatHandler(TextureFormat fmt){
		this(fmt, fmt);
	}
	
	private TextureFormatHandler(TextureFormat fmt, TextureFormat nativeFormat){
		originFormat = fmt;
		this.nativeFormat = nativeFormat;
	}
	
	public static TextureFormatHandler getHandlerForFormat(TextureFormat fmt){
		switch(fmt){
			case RGBA8:
				return RGBA8;
			case A4:
				return A4;
			case A8:
				return A8;
			case ETC1:
				return ETC1;
			case ETC1A4:
				return ETC1A4;
			case L4:
				return L4;
			case L8:
				return L8;
			case L4A4:
				return L4A4;
			case L8A8:
				return L8A8;
			case NV_HILO8:
				return HILO8;
			case RGB565:
				return RGB565;
			case RGB5A1:
				return RGB5A1;
			case RGB8:
				return RGB8;
			case RGBA4:
				return RGBA4;
			case FLOAT32:
				return LUMINANCE_F32;
		}
		return RGBA8;
	}
	
	public TextureFormatHandler getHandlerForNative(){
		return getHandlerForFormat(nativeFormat);
	}
	
	public boolean getNeedsByteExpand(){
		switch (originFormat){
			case A4:
			case L4:
			case L4A4:
				return true;
		}
		return false;
	}
	
	public boolean getNeedsFullDecode(){
		switch (originFormat){
			case ETC1:
			case ETC1A4:
				return true;
		}
		return false;
	}
	
	public static int getPixelByteOffset(int x, int y, int width, int Bpp) {
		return (y * width + x) * Bpp;
	}
	
	public int getPixelByteOffset(int x, int y, int width) {
		return getPixelByteOffset(x, y, width, getNativeBPP());
	}
	
	public int getNativeBPP(){
		return getBytesPerPixelForNativeFormat(nativeFormat);
	}
	
	public int getOriginBitsPP(){
		return getBitsPerPixelForFormat(originFormat);
	}
	
	public int getDataSizeForDimensions(int w, int h){
		return (w * h * getOriginBitsPP()) >> 3;
	}
	
	public Texture createTexture(int w, int h){
		return new Texture(w, h, this);
	}
	
	public byte[] getRGBA(Texture tex){
		return TextureCodec.getRGBA(tex, this);
	}
	
	public boolean hasAlpha(){
		switch(nativeFormat){
			case A4:
			case A8:
			case ETC1A4:
			case L4A4:
			case L8A8:
			case RGB5A1:
			case RGBA4:
			case RGBA8:
				return true;
		}
		return false;
	}
	
	public Texture createFullyNativeTexture(int width, int height, byte[] data){
		Texture tex = new Texture(width, height, getHandlerForNative(), data);
		
		tex.data = getOnTheFlyNativeConvTexData(tex);
		
		return tex;
	}
	
	public byte[] getOnTheFlyNativeConvTexData(Texture tex){
		if (getNeedsByteExpand()){
			return TextureCodec.fourToEightBits(tex.data);
		}
		
		if (getNeedsFullDecode()){
			return ctrmap.renderer.util.texture.compressors.ETC1.etc1DecodeNew(tex.data, tex.width, tex.height, tex.format);
		}
		
		return tex.data;
	}
	
	public static int getBitsPerPixelForFormat(TextureFormat f){
		switch (f){
			case A4:
			case L4:
			case ETC1:
				return 4;
			case A8:
			case L8:
			case L4A4:
			case ETC1A4:
				return 8;
			case L8A8:
			case NV_HILO8:
			case RGB565:
			case RGB5A1:
			case RGBA4:
				return 16;
			case RGB8:
				return 24;
			case RGBA8:
			case FLOAT32:
				return 32;
		}
		return 32;
	}
	
	public static int getBytesPerPixelForNativeFormat(TextureFormat f){
		switch (f){
			case A8:
			case L8:
			case L4A4:
				return 1;
			case L8A8:
			case NV_HILO8:
			case RGB565:
			case RGB5A1:
			case RGBA4:
				return 2;
			case RGB8:
				return 3;
			case FLOAT32:
			case RGBA8:
				return 4;
			default:
				throw new IllegalArgumentException("Format is not a valid native format.");
		}
	}
}
