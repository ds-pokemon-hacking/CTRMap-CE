package ctrmap.missioncontrol_base.util;

import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.util.generators.PlaneGenerator;
import ctrmap.renderer.util.texture.TextureProcessor;
import java.nio.ByteBuffer;
import javax.swing.SwingUtilities;

public class GameCapture extends Scene {

	private static final String GAMECAPTURE_TEXTURE_NAME = "GameCaptureTexture";
	private static final String GAMECAPTURE_MATERIAL_NAME = "GameCaptureMaterial";

	private Texture captureTexture;

	private GameCapture() {
		super("GameCapture");
	}

	private void initG3D() {
		Model model = new Model();
		model.addMaterial(createGameCaptureMaterial());

		Mesh mesh = PlaneGenerator.generateQuadPlaneMesh(1f, 1f, 0f, false, true);
		mesh.name = "GameCaptureMesh";
		mesh.materialName = GAMECAPTURE_MATERIAL_NAME;
		model.addMesh(mesh);

		ModelInstance instance = new ModelInstance();

		instance.resource.addModel(model);
		instance.resource.addTexture(captureTexture);

		addModel(instance);
	}

	private static Material createGameCaptureMaterial() {
		Material mat = new Material();
		mat.name = GAMECAPTURE_MATERIAL_NAME;
		mat.fshType = MaterialParams.FragmentShaderType.USER_SHADER;
		mat.vertexShaderName = "FillScreenQuad.vsh";
		mat.fragmentShaderName = "GameCapture.fsh";
		mat.depthColorMask.enabled = false;
		mat.textures.add(new TextureMapper(GAMECAPTURE_TEXTURE_NAME));
		return mat;
	}

	public static GameCapture createCapture(AbstractBackend videoCore) {
		GameCapture cap = new GameCapture();
		ViewportInfo info = videoCore.getViewportInfo();

		int w = info.surfaceDimensions.width;
		int h = info.surfaceDimensions.height;

		cap.captureTexture = new Texture(w, h, TextureFormatHandler.RGBA8);
		cap.captureTexture.name = GAMECAPTURE_TEXTURE_NAME;
		ByteBuffer buf = ByteBuffer.wrap(cap.captureTexture.data);
		SwingUtilities.invokeLater((() -> { //should be done by the time the display loop starts
			videoCore.readPixels(0, 0, w, h, RenderConstants.RENDERTARGET_SURFACE_MAIN, buf);
			cap.captureTexture.data = TextureProcessor.flipImageData(w, h, cap.captureTexture.data, TextureFormatHandler.RGBA8);
		}));
		
		cap.initG3D();

		return cap;
	}
}
