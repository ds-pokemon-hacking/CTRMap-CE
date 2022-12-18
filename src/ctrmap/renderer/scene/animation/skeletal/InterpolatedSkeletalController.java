package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.backends.RenderAllocator;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.math.InterpolationTimer;
import xstandard.math.vec.Matrix4;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InterpolatedSkeletalController extends SkeletalController {

	private InterpolationTimer timer;

	private SkeletalController leftController;
	private SkeletalController rightController;

	private List<InterpolatedBoneTransform> transforms = new ArrayList<>();

	public InterpolatedSkeletalController(SkeletalController ctrl1, SkeletalController ctrl2, Skeleton skeleton, float interpDuration) {
		super(null);
		timer = new InterpolationTimer(interpDuration);
		anim = createDummyAnime(ctrl1, ctrl2, skeleton);
		setSkeleton(skeleton);
		leftController = ctrl1;
		rightController = ctrl2;
	}

	@Override
	public void register(RenderSettings settings) {
		super.register(settings);
		leftController.register(settings);
		rightController.register(settings);
	}

	@Override
	public void advanceFrame(float globalStep, RenderSettings settings) {
		frame += globalStep;
		timer.setTime(frame);
		leftController.advanceFrameImpl(globalStep, settings);
		rightController.advanceFrameImpl(globalStep, settings);
		animatedTransform.clear();
	}
	
	@Override
	public void pauseAnimation(){
		leftController.pauseAnimation();
		rightController.pauseAnimation();
	}
	
	@Override
	public void stopAnimation(){
		leftController.stopAnimation();
		rightController.stopAnimation();
	}
	
	@Override
	public void resumeAnimation(){
		leftController.resumeAnimation();
		rightController.resumeAnimation();
	}
	
	@Override
	public void restartAnimation(){
		leftController.restartAnimation();
		rightController.restartAnimation();
	}
	
	@Override
	public void pauseOrUnpauseAnimation(){
		leftController.pauseOrUnpauseAnimation();
		rightController.pauseOrUnpauseAnimation();
	}

	private SkeletalAnimation createDummyAnime(SkeletalController c1, SkeletalController c2, Skeleton skl) {
		if (c1.anim == null) {
			c1.forceNextAnime();
		}
		if (c2.anim == null) {
			c2.forceNextAnime();
		}
		SkeletalAnimation anm = new SkeletalAnimation();
		anm.frameCount = Math.max(c1.anim.frameCount, c2.anim.frameCount);
		anm.skeleton = skl;
		anm.name = c1.getName() + "_to_" + c2.getName();

		for (SkeletalBoneTransform btL : ((SkeletalAnimation) c1.anim).bones) {
			SkeletalBoneTransform btR = c2.getBoneTransform(btL.name);
			anm.bones.add(new InterpolatedBoneTransform(btL, btR, c1, c2, timer));
		}
		for (SkeletalBoneTransform btR : ((SkeletalAnimation) c2.anim).bones) {
			if (Scene.getNamedObject(btR.name, anm.bones) == null) {
				anm.bones.add(new InterpolatedBoneTransform(c1.getBoneTransform(btR.name), btR, c1, c2, timer));
			}
		}

		return anm;
	}
	
	@Override
	public void makeAnimationMatrices(float frame, Skeleton skeleton, boolean manualAllocation) {
		if (this.skeleton != null) {
			skeleton = this.skeleton;
		}
		if (skeleton == null) {
			return;
		}

		if (skeleton.bindTransforms.size() < skeleton.getJoints().size()) {
			skeleton.buildTransforms();
		}

		SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(frame, manualAllocation);
		
		Map<String, Matrix4> mtxMap = new HashMap<>();
		
		for (int i = 0; i < skeleton.getJoints().size(); i++) {
			Joint j = skeleton.getJoint(i);
			req.bindJoint = j;
			mtxMap.put(j.name, getJointMatrix(req, manualAllocation ? RenderAllocator.allocMatrix() : new Matrix4()));
		}
		
		animatedTransform.put(skeleton, mtxMap);
	}
}
