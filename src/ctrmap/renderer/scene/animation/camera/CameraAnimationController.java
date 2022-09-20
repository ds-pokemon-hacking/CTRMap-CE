package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import java.util.HashMap;
import java.util.Map;

public class CameraAnimationController extends AbstractAnimationController {

	public Map<String, CameraAnimationFrame> transforms = new HashMap<>();

	public CameraAnimationController(AbstractAnimation anm) {
		super(anm);
	}

	@Override
	public void advanceFrame(float globalStep, RenderSettings settings) {
		super.advanceFrame(globalStep, settings);

		transforms.clear();
		if (anim != null) {
			for (AbstractBoneTransform bt : anim.getBones()) {
				CameraBoneTransform cbt = (CameraBoneTransform)bt; //( ͡° ͜ʖ ͡°)
				
				transforms.put(cbt.name, cbt.getFrame(frame, settings.ANIMATION_DO_NOT_INTERPOLATE));
			}
		}
	}
}
