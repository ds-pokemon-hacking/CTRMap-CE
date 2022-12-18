package ctrmap.renderer.scene.animation.visibility;

import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.AnimatedValue;
import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.util.ArraysEx;
import java.util.List;

/**
 *
 */
public class VisibilityBoneTransform extends AbstractBoneTransform {

	public Target target = Target.MESH;

	public KeyFrameList isVisible = new KeyFrameList();
	
	private final AnimatedValue tempAnimValue = new AnimatedValue();

	public boolean isVisible(float frame) {
		AnimatedValue val = getValueAt(isVisible, frame, true, tempAnimValue);
		if (val.exists) {
			return val.value != 0;
		}
		return true;
	}

	@Override
	public List<KeyFrameList> getAllKfLists() {
		return ArraysEx.asList(isVisible);
	}

	public static enum Target {
		MESH,
		JOINT,
		VISGROUP
	}
}
