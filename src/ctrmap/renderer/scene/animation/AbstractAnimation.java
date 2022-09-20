package ctrmap.renderer.scene.animation;

import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public abstract class AbstractAnimation implements NamedResource {

	public float frameCount;

	public boolean isLooped = false;

	public abstract ListenableList<? extends AbstractBoneTransform> getBones();

	public String name;
	
	public MetaData metaData = new MetaData();

	public float getFrameCountMaxTime() {
		float retval = 0;
		for (AbstractBoneTransform bt : getBones()) {
			for (KeyFrameList kfl : bt.getAllKfLists()) {
				for (KeyFrame kf : kfl) {
					if (kf.frame > retval) {
						retval = kf.frame;
					}
				}
			}
		}
		if (frameCount - retval > 2) {
			retval = frameCount;
		}
		return retval;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public AbstractBoneTransform getBoneTransform(String name) {
		for (AbstractBoneTransform t : getBones()) {
			if (t.name.equals(name)) {
				return t;
			}
		}
		return null;
	}
	
	public boolean hasBoneTransform(String name){
		for (AbstractBoneTransform t : getBones()) {
			if (t.name.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private boolean optimized = false;

	protected abstract void callOptimize();

	public void optimize() {
		if (!optimized) {
			callOptimize();
			optimized = true;
		}
	}
	
	public void forceSetAsOptimize(){
		optimized = true;
	}
}
