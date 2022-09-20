package ctrmap.renderer.scene.animation.material;

import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.util.AnimeProcessor;
import xstandard.util.ListenableList;

public class MaterialAnimation extends AbstractAnimation{
	public ListenableList<MatAnimBoneTransform> bones = new ListenableList<>();
	
	@Override
	public ListenableList<? extends AbstractBoneTransform> getBones() {
		return bones;
	}
		
	@Override
	public void callOptimize(){
		AnimeProcessor.optimizeMaterialAnimation(this);
	}
}
