
package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.scene.model.Joint;

public class SkeletalAnimationTransformRequest {
	public float frame;
	public Joint bindJoint;
	
	public boolean disableInterpolation;
	
	public boolean translation = true;
	public boolean rotation = true;
	public boolean scale = true;
	
	public SkeletalAnimationTransformRequest(float frame){
		this.frame = frame;
	}
	
	public SkeletalAnimationTransformRequest(SkeletalAnimationTransformRequest req){
		frame = req.frame;
		bindJoint = req.bindJoint;
		disableInterpolation = req.disableInterpolation;
		translation = req.translation;
		rotation = req.rotation;
		scale = req.scale;
	}
}
