package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.backends.RenderAllocator;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.model.Joint;
import xstandard.math.vec.Matrix4;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SkeletalController extends AbstractAnimationController {

	protected Map<Skeleton, Map<String, Matrix4>> animatedTransform = new HashMap<>();
	
	protected Skeleton skeleton;

	public SkeletalController(SkeletalAnimation anm) {
		this(anm, (Runnable) null);
	}

	public SkeletalController(SkeletalAnimation anm, Runnable callback) {
		this(anm, anm == null ? null : anm.skeleton, callback);
	}

	public SkeletalController(Collection<AbstractAnimation> queue, Skeleton s, Runnable callback) {
		super(queue);
		skeleton = s;
		this.callback = callback;
	}

	public SkeletalController(SkeletalAnimation anm, Skeleton s) {
		super(anm);
		skeleton = s;
	}

	public SkeletalController(SkeletalAnimation anm, Skeleton s, Runnable callback) {
		this(anm, s);
		this.callback = callback;
	}

	public void setSkeleton(Skeleton skl) {
		if (anim != null && ((SkeletalAnimation)anim).hasSkeleton() && skeleton == ((SkeletalAnimation) anim).skeleton) {
			return;
		}
		skeleton = skl;
	}

	public Skeleton getSkeleton() {
		return skeleton;
	}
	
	public Matrix4 getAnimatedTransform(Skeleton skl, String name) {
		Map<String, Matrix4> map = animatedTransform.get(skl);
		if (map != null) {
			return map.get(name);
		}
		return null;
	}

	@Override
	public void advanceFrame(float globalStep, RenderSettings settings) {
		advanceFrameImpl(globalStep, settings);
		animatedTransform.clear();
	}
	
	public void freeMatrices() {
		for (Map<String, Matrix4> map : animatedTransform.values()) {
			for (Matrix4 mat : map.values()) {
				RenderAllocator.freeMatrix(mat);
			}
		}
		animatedTransform.clear();
	}
	
	protected float advanceFrameImpl(float globalStep, RenderSettings settings){
		super.advanceFrame(globalStep, settings);
		
		if (anim != null) {
			Skeleton newSkl = ((SkeletalAnimation) anim).skeleton;
			if (newSkl != null) {
				skeleton = newSkl;
			}
		}

		float frameFinal = frame;
		if (settings.ANIMATION_USE_30FPS_SKL) {
			frameFinal = (float) Math.floor(frame);
		}
		return frameFinal;
	}

	public void makeAnimationMatrices(float frame, Skeleton skeleton, boolean manualAllocation) {
		if (this.skeleton != null) {
			skeleton = this.skeleton;
		}
		if (anim == null || skeleton == null) {
			return;
		}

		if (skeleton.bindTransforms.size() < skeleton.getJoints().size()) {
			skeleton.buildTransforms();
		}
		
		SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(frame, manualAllocation);
		
		Map<String, Matrix4> mtxMap = new HashMap<>();
		
		for (int i = 0; i < skeleton.getJoints().size(); i++) {
			Joint j = skeleton.getJoint(i);

			if (anim.getBoneTransform(j.name) != null){
				req.bindJoint = j;
				mtxMap.put(j.name, getJointMatrix(req, manualAllocation ? RenderAllocator.allocMatrix() : new Matrix4()));
			}
		}
		
		animatedTransform.put(skeleton, mtxMap);
	}

	protected Matrix4 getJointMatrix(SkeletalAnimationTransformRequest req, Matrix4 dest) {
		SkeletalBoneTransform bt = getBoneTransform(req.bindJoint.name);
		return bt.getTransformMatrix(req, dest);
	}
	
	public SkeletalBoneTransform getBoneTransform(String name){
		SkeletalBoneTransform bt = null;
		if (anim != null){
			bt = (SkeletalBoneTransform) anim.getBoneTransform(name);
		}
		if (bt == null){
			bt = new SkeletalBoneTransform();
			bt.name = name;
		}
		return bt;
	}
}
