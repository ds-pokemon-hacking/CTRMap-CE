package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.texturing.formats.TextureFormat;

public enum CMIFTextureFormat {
	COMPRESSED,
	FULL_COLOR,
	REDUCED_COLOR,
	
	AUTO;
	
	public static final CMIFTextureFormat[] VALUES = values();
		
	public static CMIFTextureFormat fromPICA(TextureFormat format){
		switch (format){
			case RGB565:
			case RGB5A1:
				return REDUCED_COLOR;
			case RGB8:
			case RGBA4:
			case RGBA8:
				return FULL_COLOR;
		}
		return AUTO;
	}
}
