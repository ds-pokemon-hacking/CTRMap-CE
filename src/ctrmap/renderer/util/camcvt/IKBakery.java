package ctrmap.renderer.util.camcvt;

import ctrmap.renderer.scene.animation.skeletal.InverseKinematics;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationFrame;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationTransformRequest;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.math.vec.Matrix4;

public class IKBakery {

	public static InverseKinematics.IKOutput calcIKMatrices(
		float frame, 
		Skeleton skeleton, 
		SkeletalMatrixBakery chainBakery, 
		SkeletalBoneTransform chainBT, 
		SkeletalBoneTransform jointBT, 
		SkeletalBoneTransform effectorBT
	) {
		SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(frame);

		InverseKinematics.IKInput in = new InverseKinematics.IKInput();
		in.chain = skeleton.getJoint(chainBT.name);
		in.joint = skeleton.getJoint(jointBT.name);
		in.effector = skeleton.getJoint(effectorBT.name);
		if (in.chain != null && in.joint != null && in.effector != null) {
			req.bindJoint = in.joint;
			in.localJointMatrix = jointBT.getTransformMatrix(req);
			in.localEffectorMatrix = new Matrix4();
			in.localEffectorMatrix.translation(in.effector.position); //local
			req.bindJoint = in.effector;
			SkeletalAnimationFrame f = effectorBT.getFrame(req);
			in.localEffectorMatrix.rotate(f.getRotation());
			in.localEffectorMatrix.scale(f.getScale());
			in.globalEffectorMatrix = new Matrix4();
			in.globalEffectorMatrix.translation(f.getTranslation()); //always global when animated
			in.globalChainMatrix = chainBakery.manualBake(frame);
			return InverseKinematics.transformIK(in);
		}
		return null;
	}
}
