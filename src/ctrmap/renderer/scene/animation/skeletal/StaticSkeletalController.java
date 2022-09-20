package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.scene.model.Joint;

public class StaticSkeletalController extends SkeletalController {

	public StaticSkeletalController(SkeletalController frozen) {
		super(createAnimation(frozen));
	}

	private static SkeletalAnimation createAnimation(SkeletalController ctrl) {
		SkeletalAnimation anime = new SkeletalAnimation();
		anime.skeleton = ctrl.skeleton;
		anime.frameCount = ctrl.anim.frameCount;
		anime.name = ctrl.getName() + "_frame" + (int)ctrl.frame;
		for (Joint j : anime.skeleton) {
			SkeletalAnimationFrame frame = ctrl.getBoneTransform(j.name).getFrame(ctrl.frame, j);
			SkeletalBoneTransform bt = new SkeletalBoneTransform();
			bt.name = j.name;
			bt.pushFullBakedFrame(0, frame);
			anime.bones.add(bt);
		}
		return anime;
	}
}
