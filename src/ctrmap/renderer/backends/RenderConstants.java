package ctrmap.renderer.backends;

import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.Texture;

/**
 *
 */
public class RenderConstants {
	
	public static final boolean RENDER_DEBUG = false;
	
	public static final String RENDERTARGET_SURFACE_MAIN = "RT_SURFACE_MAIN";
	
	public static final String BACKEND_OGL2_HOUSTON = "Houston";
	public static final String BACKEND_OGL2_HOUSTON_UBER = "HoustonUber";
	public static final String BACKEND_OGL4_HOUSTON = "Houston4";

	public static final int LAYER_MAX = 8;
	public static final int TEXTURE_MAX = 4;
	public static final int LUT_MAX = MaterialParams.LUTTarget.values().length;
	public static final int LIGHT_MAX = 8;
	public static final int SHADING_STAGE_MAX = TexEnvConfig.STAGE_COUNT;
	
	public static final Texture EMPTY_TEXTURE = new Texture(4, 4, new byte[64]);

	public static int getTextureMax(Material mat) {
		if (mat != null) {
			return Math.min(TEXTURE_MAX, mat.textures.size());
		}
		return 0;
	}
}
