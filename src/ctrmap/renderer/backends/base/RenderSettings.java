package ctrmap.renderer.backends.base;

import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.houston.gl2.HoustonGL2;
import ctrmap.renderer.backends.houston.gl2.uber.HoustonGL2Uber;
import ctrmap.renderer.backends.houston.gl4.HoustonGL4;
import xstandard.math.vec.RGBA;
import ctrmap.util.CMPrefs;
import java.util.prefs.Preferences;

public class RenderSettings {

	public static final RenderSettings DEFAULT_SETTINGS = new RenderSettings();

	public String BACKEND_DEFAULT = RenderConstants.BACKEND_OGL2_HOUSTON;
	public boolean USE_NATIVE_TEXTURE_FORMATS;
	public boolean ENABLE_SHADER_GC;
	
	public float FOV;
	public float Z_NEAR;
	public float Z_FAR;
	public int FRAMERATE_CAP;
	public float FRAME_SCALE;
	public boolean ANIMATION_DO_NOT_INTERPOLATE;
	public boolean ANIMATION_USE_30FPS_SKL;
	public boolean ANIMATION_USE_30FPS_MAT;
	public boolean ANIMATION_OPTIMIZE;
	
	public boolean BACKFACE_CULLING;

	public RGBA CLEAR_COLOR;

	protected RenderSettings() {
		loadDefaults();
	}
	
	public static RenderSettings getDefaultSettings(){
		return new RenderSettings();
	}
	
	public static AbstractBackend createDefaultRenderer(){
		return createDefaultRenderer(null);
	}
	
	public static AbstractBackend createDefaultRenderer(RenderCapabilities caps){
		return getDefaultSettings().createRenderer(caps);
	}
	
	public AbstractBackend createRenderer(){
		return createRenderer(null);
	}
	
	public AbstractBackend createRenderer(RenderCapabilities caps){
		switch (BACKEND_DEFAULT){
			case RenderConstants.BACKEND_OGL2_HOUSTON:
				return new HoustonGL2(this, caps);
			case RenderConstants.BACKEND_OGL2_HOUSTON_UBER:
				return new HoustonGL2Uber(this, caps);
			case RenderConstants.BACKEND_OGL4_HOUSTON:
				return new HoustonGL4(this, caps);
		}
		return new HoustonGL2();
	}

	public final void loadDefaults() {
		BACKEND_DEFAULT = Defaults.BACKEND_DEFAULT;
		USE_NATIVE_TEXTURE_FORMATS = Defaults.USE_NATIVE_TEXTURE_FORMATS;
		ENABLE_SHADER_GC = Defaults.ENABLE_SHADER_GC;
		FOV = Defaults.FOV;
		Z_FAR = Defaults.Z_FAR;
		Z_NEAR = Defaults.Z_NEAR;
		FRAMERATE_CAP = Defaults.FRAMERATE_CAP;
		FRAME_SCALE = Defaults.FRAME_SCALE;
		ANIMATION_USE_30FPS_MAT = Defaults.ANIMATION_USE_30FPS_MAT;
		ANIMATION_USE_30FPS_SKL = Defaults.ANIMATION_USE_30FPS_SKL;
		ANIMATION_OPTIMIZE = Defaults.ANIMATION_OPTIMIZE;
		BACKFACE_CULLING = Defaults.BACKFACE_CULLING;
		CLEAR_COLOR = Defaults.CLEAR_COLOR;
	}

	public static class Defaults {

		private static final Preferences prefs = CMPrefs.node(Defaults.class.getName());

		private static final String DEFAULT_BACKEND = "defaultBackend";
		private static final String BACKEND_USE_NATIVE_FORMATS = "useNativeTextureFormats";
		private static final String BACKEND_ENABLE_SHADER_GC = "enableShaderGarbageCollection";

		private static final String RENDERER_FOV = "gfxFOV";
		private static final String RENDERER_NEAR = "gfxNear";
		private static final String RENDERER_FAR = "gfxFar";
		private static final String RENDERER_FPS_CAP = "gfxFramelimit";
		
		private static final String RENDERER_BFC = "gfxBackFaceCulling";

		private static final String DEBUG_ANIME_SPEED_MUL = "dbgFrameLengthInternal";
		private static final String DEBUG_ANIME_NO_INTERPOLATE_S = "dbgUse30FpsAnimSkl";
		private static final String DEBUG_ANIME_NO_INTERPOLATE_M = "dbgUse30FpsAnimMat";
		private static final String DEBUG_ANIME_OPTIMIZE = "dbgPreprocessAnime";

		public static String BACKEND_DEFAULT;
		public static boolean USE_NATIVE_TEXTURE_FORMATS;
		public static boolean ENABLE_SHADER_GC;
		public static float FOV;
		public static float Z_NEAR;
		public static float Z_FAR;
		public static int FRAMERATE_CAP;
		public static float FRAME_SCALE;
		public static boolean ANIMATION_USE_30FPS_SKL;
		public static boolean ANIMATION_USE_30FPS_MAT;
		public static boolean ANIMATION_OPTIMIZE;
		public static boolean BACKFACE_CULLING;
		public static RGBA CLEAR_COLOR = new RGBA(0, 0, 0, 255);

		static {
			BACKEND_DEFAULT = prefs.get(DEFAULT_BACKEND, RenderConstants.BACKEND_OGL2_HOUSTON);
			USE_NATIVE_TEXTURE_FORMATS = prefs.getBoolean(BACKEND_USE_NATIVE_FORMATS, false);
			ENABLE_SHADER_GC = prefs.getBoolean(BACKEND_ENABLE_SHADER_GC, true);
			FOV = prefs.getFloat(RENDERER_FOV, 45.0f);
			Z_FAR = prefs.getFloat(RENDERER_FAR, 15000f);
			Z_NEAR = prefs.getFloat(RENDERER_NEAR, 50f);
			FRAMERATE_CAP = prefs.getInt(RENDERER_FPS_CAP, 60);
			FRAME_SCALE = prefs.getFloat(DEBUG_ANIME_SPEED_MUL, 1f);
			ANIMATION_USE_30FPS_MAT = prefs.getBoolean(DEBUG_ANIME_NO_INTERPOLATE_M, false);
			ANIMATION_USE_30FPS_SKL = prefs.getBoolean(DEBUG_ANIME_NO_INTERPOLATE_S, false);
			ANIMATION_OPTIMIZE = prefs.getBoolean(DEBUG_ANIME_OPTIMIZE, true);
			BACKFACE_CULLING = prefs.getBoolean(RENDERER_BFC, false);
		}

		public static Preferences getRenderSettingsDefaultPreferenceManager() {
			return prefs;
		}

		public static void saveDefaults() {
			prefsPutNonNull(DEFAULT_BACKEND, BACKEND_DEFAULT, prefs);
			prefs.putBoolean(BACKEND_USE_NATIVE_FORMATS, USE_NATIVE_TEXTURE_FORMATS);
			prefs.putBoolean(BACKEND_ENABLE_SHADER_GC, ENABLE_SHADER_GC);
			prefs.putBoolean(DEBUG_ANIME_NO_INTERPOLATE_M, ANIMATION_USE_30FPS_MAT);
			prefs.putBoolean(DEBUG_ANIME_NO_INTERPOLATE_S, ANIMATION_USE_30FPS_SKL);
			prefs.putBoolean(DEBUG_ANIME_OPTIMIZE, ANIMATION_OPTIMIZE);
			prefs.putFloat(RENDERER_FOV, FOV);
			prefs.putFloat(RENDERER_NEAR, Z_NEAR);
			prefs.putFloat(RENDERER_FAR, Z_FAR);
			prefs.putInt(RENDERER_FPS_CAP, FRAMERATE_CAP);
			prefs.putBoolean(RENDERER_BFC, BACKFACE_CULLING);
			prefs.putFloat(DEBUG_ANIME_SPEED_MUL, FRAME_SCALE);
		}

		private static void prefsPutNonNull(String key, String value, Preferences prefs) {
			if (value != null && key != null) {
				prefs.put(key, value);
			}
		}
	}
}
