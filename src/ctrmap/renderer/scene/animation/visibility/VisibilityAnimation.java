
package ctrmap.renderer.scene.animation.visibility;

import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import xstandard.util.ListenableList;

/**
 *
 */
public class VisibilityAnimation extends AbstractAnimation {

	public ListenableList<VisibilityBoneTransform> tracks = new ListenableList<>();
	
	@Override
	public ListenableList<? extends AbstractBoneTransform> getBones() {
		return tracks;
	}

	@Override
	protected void callOptimize() {
		
	}

}
