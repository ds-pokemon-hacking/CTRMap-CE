package ctrmap.creativestudio.ngcs;

import ctrmap.CTRMapResources;
import ctrmap.renderer.backends.RenderSurface;
import xstandard.math.vec.RGBA;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.util.generators.GridGenerator;
import xstandard.res.ResourceAccess;

public class CSG3DSurface extends RenderSurface {

	public static final String CS_SHADER_EXTENSIONS_PATH = "cs/shader";
	public static final String CS_COMMON_SHADER_EXT = "CreativeStudioCommon.vsh_ext";

	public static final G3DResource CS_GRID = GridGenerator.generateGrid(5f, 0f, 15, 1, RGBA.WHITE, true);

	public Scene scene;

	private String currentAttrShader = null;

	public CSG3DSurface() {
		super(new CSG3DRenderSettings());

		CTRMapResources.load();

		getProgramManager().getUserShManager().addIncludeDirectory(ResourceAccess.getResourceFile(CS_SHADER_EXTENSIONS_PATH));
		getProgramManager().loadExtension(CS_COMMON_SHADER_EXT);

		G3DResourceInstance grid = CS_GRID.createInstance();
		grid.setPersistent(true);
		scene = getScene();

		scene.addChild(grid);
	}

	public void setAttrShader(String name) {
		name = name == null ? null : "frag_attribute_shader/" + name;
		if (currentAttrShader != null) {
			getProgramManager().unloadExtension(currentAttrShader);
		}
		if (name != null) {
			getProgramManager().loadExtension(name);
			currentAttrShader = name;
		}
	}

	private static class CSG3DRenderSettings extends RenderSettings {

		public CSG3DRenderSettings() {
			super();
			Z_NEAR = 10f;
			Z_FAR = 3000;
			FRAMERATE_CAP = 240;
			ANIMATION_USE_30FPS_SKL = false;
			CLEAR_COLOR = new RGBA(65, 65, 65, 255);
			BACKFACE_CULLING = true;
		}
	}
}
