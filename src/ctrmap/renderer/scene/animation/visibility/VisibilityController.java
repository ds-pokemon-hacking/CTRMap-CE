package ctrmap.renderer.scene.animation.visibility;

import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class VisibilityController extends AbstractAnimationController {

	public Map<String, Boolean> nodeVisibilities = new HashMap<>();

	public VisibilityController(VisibilityAnimation anm) {
		super(anm);
	}

	@Override
	public void advanceFrame(float globalStep, RenderSettings settings) {
		super.advanceFrame(globalStep, settings);

		nodeVisibilities.clear();
		if (anim != null) {
			for (VisibilityBoneTransform bt : ((VisibilityAnimation) anim).tracks) {
				nodeVisibilities.put(bt.name, bt.isVisible(frame));
			}
		}
	}

}
