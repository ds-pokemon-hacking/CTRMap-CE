
package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.util.AnimeProcessor;
import xstandard.util.ListenableList;

public class CameraAnimation extends AbstractAnimation {

	public ListenableList<CameraBoneTransform> transforms = new ListenableList<>();
	
	@Override
	public ListenableList<? extends AbstractBoneTransform> getBones() {
		return transforms;
	}

	@Override
	protected void callOptimize() {
		AnimeProcessor.optimizeCameraAnimation(this);
	}
}
