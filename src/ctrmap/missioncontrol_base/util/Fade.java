package ctrmap.missioncontrol_base.util;

import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.util.generators.PlaneGenerator;
import xstandard.math.Easings;
import xstandard.math.vec.RGBA;

public class Fade extends Scene {

	private static final String FADE_MATERIAL_NAME = "FadeMaterial";

	private RGBA beginColor = new RGBA();
	private RGBA endColor = new RGBA();

	private RGBA constColorRef = new RGBA();

	private float position = 0f;
	private float duration;

	private boolean isEnd = false;

	public Fade() {
		super("Fade");

		initG3D();

		addSceneAnimationCallback(((frameAdvance) -> {
			isEnd = position >= duration;
			position += frameAdvance;
			float weight = Easings.easeInOutCosine_GF(duration == 0f ? 1f : Math.min(1f, position / duration));
			constColorRef.set(beginColor);
			constColorRef.lerp(endColor, weight);
		}));
		
		setVisible(false);
	}
	
	public void set(Fade other) {
		beginColor.set(other.beginColor);
		endColor.set(other.endColor);
		position = other.position;
		duration = other.duration;
		isEnd = other.isEnd;
		visible = other.visible;
	}

	public void set(RGBA begin, RGBA end, float durationFrames) {
		this.beginColor.set(begin);
		this.endColor.set(end);
		this.duration = durationFrames;
		position = 0f;
	}
	
	public RGBA getEndColor() {
		return endColor;
	}

	public boolean isEnd() {
		return isEnd;
	}

	private void initG3D() {
		Model model = new Model();
		model.name = "Fade";
		Material mat = createFadeConstMaterial();
		constColorRef = mat.constantColors[0];
		model.addMaterial(mat);

		Mesh mesh = PlaneGenerator.generateQuadPlaneMesh(1f, 1f, 0f, false, true);
		mesh.name = "FadeMesh";
		mesh.renderLayer = 7;
		mesh.materialName = FADE_MATERIAL_NAME;
		model.addMesh(mesh);

		ModelInstance instance = new ModelInstance();

		instance.resource.addModel(model);

		addModel(instance);
	}

	private static Material createFadeConstMaterial() {
		Material mat = new Material();
		mat.name = FADE_MATERIAL_NAME;
		mat.fshType = MaterialParams.FragmentShaderType.CTR_COMBINER;
		mat.vertexShaderName = "FillScreenQuad.vsh";
		mat.depthColorMask.depthFunction = MaterialParams.TestFunction.ALWAYS;
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.CCOL);
		MaterialProcessor.setAlphaBlend(mat);
		return mat;
	}
}
