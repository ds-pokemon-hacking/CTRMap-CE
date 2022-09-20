package ctrmap.formats.pokemon.containers.util;

public enum ContentType {
	UNKNOWN("bin"),
	SUB_CONTAINER,
	H3D_MODEL("bch"),
	H3D_TEXTURE_PACK("bch"),
	H3D_ANIM_S("bch"),
	H3D_ANIM_M("bch"),
	H3D_ANIM_V("bch"),
	GF_MOTION("gfmp"),
	CGFX("bcres"),
	H3D_OTHER("bch"),
	CAMERA_DATA("adcam"),
	CAMERA_DATA_MM_EXTRA("mmcam"),
	COLLISION("gfbcol"),
	TILEMAP("gfbmap"),
	PROP_DATA("pcfg"),
	MAPMATRIX("mm"),
	PROP_REGISTRY("preg"),
	ZONE_HEADER("zh"),
	ZONE_ENTITIES("ze"),
	ENC_DATA("enc"),
	POKE_ID_MATCONFIG("idmat"),
	SKY_COLLISION("scol");
	
	private final String extension;
	
	private ContentType(){
		this(null);
	}
	
	private ContentType(String extension){
		this.extension = extension;
	}
	
	public String getExtensionWithoutDot(){
		return extension;
	}
}
