package ctrmap.renderer.scene.metadata;

import ctrmap.formats.generic.interchange.CMIFTextureFormat;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.texturing.Texture;

public class ReservedMetaData {

	public static final String LINE_WIDTH = "LineWidth";
	public static final String DESIRED_TEX_FORMAT = "DesiredTextureFormat";
	public static final String IDX_FORMAT_PALETTE_NAME = "IdxFormatPaletteName";
	public static final String RAW_TEX_DATA = "RawTextureData";
	public static final String RAW_TEX_FMT = "RawTextureFormat";
	public static final String TEX_AS_LUT = "CreativeStudio_TexAsLUT";
	public static final String CAM_LOOKAT_AS_AIM = "CreativeStudio_LookAtAsAim";

	public static String getIdxTexPalName(Texture tex) {
		if (tex.metaData.hasValue(ReservedMetaData.IDX_FORMAT_PALETTE_NAME)) {
			return tex.metaData.getValue(ReservedMetaData.IDX_FORMAT_PALETTE_NAME).stringValue();
		}
		return tex.name;
	}
	
	public static final float getLineWidth(MetaData metaData, float defaultValue) {
		if (metaData.hasValue(ReservedMetaData.LINE_WIDTH)) {
			return metaData.getValue(ReservedMetaData.LINE_WIDTH).floatValue();
		} else {
			return defaultValue;
		}
	}
	
	public static final CMIFTextureFormat getDesiredTextureFormat(MetaData metaData, CMIFTextureFormat defaultValue) {
		if (metaData.hasValue(ReservedMetaData.DESIRED_TEX_FORMAT)) {
			return CMIFTextureFormat.VALUES[metaData.getValue(ReservedMetaData.DESIRED_TEX_FORMAT).intValue()];
		} else {
			return defaultValue;
		}
	}

	public static final boolean isLUT(Texture t) {
		return checkMetavalueBool(t.metaData, TEX_AS_LUT);
	}

	public static final boolean isLookAtAsAim(Camera cam) {
		return checkMetavalueBool(cam.metaData, CAM_LOOKAT_AS_AIM);
	}

	public static final boolean isLookAtAsAim(CameraAnimation camAnm) {
		return checkMetavalueBool(camAnm.metaData, CAM_LOOKAT_AS_AIM);
	}

	private static boolean checkMetavalueBool(MetaData md, String name) {
		MetaDataValue v = md.getValue(name);
		if (v != null) {
			return v.intValue() > 0;
		}
		return false;
	}
}
