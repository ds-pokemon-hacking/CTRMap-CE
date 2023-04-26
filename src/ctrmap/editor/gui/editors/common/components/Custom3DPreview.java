package ctrmap.editor.gui.editors.common.components;

import com.jogamp.opengl.GLEventListener;
import ctrmap.renderer.backends.RenderSurface;
import ctrmap.util.gui.cameras.OrbitCameraInputManager;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.houston.gl2.HoustonGL2;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.util.G3DUtils;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;

/**
 * Used for showing single H3D models in a window
 */
public class Custom3DPreview extends RenderSurface {

	private ModelInstance model = new ModelInstance();

	private Scene previewScene = new Scene("PreviewScene");

	private OrbitCameraInputManager camera;

	private Light[] lights = new Light[4];

	public Custom3DPreview() {
		super(new DefaultPreviewRenderSettings());
		setSyncAspectRatio(true);
		model.setPersistent(true);
		setScene(previewScene);
		previewScene.addModel(model);
		camera = new OrbitCameraInputManager(this);
		camera.addToScene(previewScene);

		Light lightBase = new Light("Custom3DPreview");
		lightBase.setDirectionByOriginIllumPosition(new Vec3f(50f, 150f, 100f));
		lightBase.ambientColor = new RGBA(220, 220, 220, 255);
		lightBase.specular1Color = new RGBA(150, 150, 150, 255);
		for (int i = 0; i < 4; i++) {
			Light l = new Light(lightBase);
			l.name += "_set" + i;
			l.setIndex = i;
			previewScene.instantiateLight(l);
			lights[i] = l;
		}
	}

	public void setLight(Light l) {
		if (l.setIndex < 4) {
			previewScene.deinstantiateLight(lights[l.setIndex]);
			previewScene.instantiateLight(l);
			lights[l.setIndex] = l;
		}
	}

	public Light getLight(int setIndex) {
		if (setIndex < 0 || setIndex >= lights.length) {
			return null;
		}
		return lights[setIndex];
	}

	@Override
	public Scene getScene() {
		return previewScene;
	}

	protected G3DResourceInstance getModel() {
		return model;
	}

	public void clear() {
		clearTextures();
		loadResource(new G3DResource());
	}

	public void loadResource(G3DResource res) {
		clearAnime();
		if (res == null) {
			res = new G3DResource();
		}
		model.setResource(res);
		setCamera();
	}

	public void loadAnime(String anm) {
		G3DResourceInstance m = getModel();
		if (m != null) {
			m.stopAllAnimations();
			m.bindAnimationAutodetect(anm);
		}
	}

	public AbstractAnimationController loadAnime(AbstractAnimation anm) {
		clearAnime();
		G3DResourceInstance m = getModel();
		if (m != null) {
			return m.bindAnimationAutodetect(anm);
		}
		return null;
	}

	public void playRscAnime() {
		G3DResourceInstance m = getModel();
		if (m != null) {
			m.playResourceAnimations();
		}
	}

	public void playFirstSkeletalAnime() {
		G3DResourceInstance m = getModel();
		if (m != null && !m.resource.skeletalAnimations.isEmpty()) {
			m.playAnimation(m.resource.skeletalAnimations.get(0));
		}
	}

	public void clearAnime() {
		G3DResourceInstance m = getModel();
		if (m != null) {
			m.stopAllAnimations();
		}
	}

	public void clearTextures() {
		getScene().resource.textures.clear();
	}

	public void mergeSceneResource(G3DResource res) {
		if (res != null) {
			previewScene.merge(res);
		}
	}

	protected void setCamera(G3DResource resource) {
		if (resource != null) {
			float maxTransl0 = Math.max(resource.boundingBox.min.getHighestAbsComponent(), resource.boundingBox.max.getHighestAbsComponent());
			camera.getTranslationCamera().FOV = 45f;
			camera.getRotationCamera().FOV = 45f;
			camera.setTX((resource.boundingBox.max.x + resource.boundingBox.min.x) / 2f);
			camera.setTY(resource.boundingBox.max.y / 2f);
			camera.setTZ(maxTransl0 * 2.25f + 5f);

			float near = G3DUtils.getIdealNearClipDistance(resource, 0.1f);
			float far = G3DUtils.getIdealFarClipDistance(resource, 300f);
			camera.getRotationCamera().zNear = near;
			camera.getTranslationCamera().zNear = near;
			camera.getRotationCamera().zFar = far;
			camera.getTranslationCamera().zFar = far;
		}
	}

	protected void setCamera() {
		setCamera(model.resource);
	}

	private static class DefaultPreviewRenderSettings extends RenderSettings {

		public DefaultPreviewRenderSettings() {
			super();
			FRAMERATE_CAP = 60;
			BACKFACE_CULLING = true;
		}
	}
}
