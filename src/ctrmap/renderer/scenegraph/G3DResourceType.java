package ctrmap.renderer.scenegraph;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Texture;

public enum G3DResourceType {
	MODEL(Model.class),
	TEXTURE(Texture.class),
	CAMERA(Camera.class),
	ANIME_M(MaterialAnimation.class),
	ANIME_S(SkeletalAnimation.class),
	ANIME_V(VisibilityAnimation.class),
	ANIME_CAM(CameraAnimation.class),
	LIGHT(Light.class),
	SCENE_TEMPLATE(G3DSceneTemplate.class);

	public static final G3DResourceType[] VALUES = values();

	private final Class<? extends NamedResource> cls;

	private G3DResourceType(Class<? extends NamedResource> cls) {
		this.cls = cls;
	}

	public static G3DResourceType get(NamedResource res) {
		if (res == null) {
			return null;
		}
		Class resClass = res.getClass();
		for (G3DResourceType t : VALUES) {
			if (t.cls.isAssignableFrom(resClass)) {
				return t;
			}
		}
		return null;
	}
}
