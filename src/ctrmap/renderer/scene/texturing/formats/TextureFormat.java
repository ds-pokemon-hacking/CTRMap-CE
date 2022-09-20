package ctrmap.renderer.scene.texturing.formats;

public enum TextureFormat {
	RGBA8,
	RGB8,
	RGB5A1,
	RGB565,
	RGBA4,
	L8A8,
	NV_HILO8,
	L8,
	A8,
	L4A4,
	L4,
	A4,
	//Compressed formats
	ETC1,
	ETC1A4,
	//Extension formats
	FLOAT32;
	
	private static TextureFormat[] values = values();
	
	public static TextureFormat valueOfSafe(String str){
		for (TextureFormat f : values){
			if (f.name().equals(str)){
				return f;
			}
		}
		return null;
	}
	
	public static TextureFormat valueOfSafe(int ord){
		if (ord >= 0 && ord < values.length){
			return values[ord];
		}
		return null;
	}
}
