package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.scene.model.Joint;
import xstandard.math.vec.Vec3f;
import xstandard.math.MathEx;
import xstandard.math.MatrixUtil;
import xstandard.math.vec.Matrix4;
import org.joml.Matrix3f;

public class InverseKinematics {

	public static IKOutput transformIK(IKInput in) {
		IKOutput out = new IKOutput();

		out.chainMatrix = in.globalChainMatrix.clone();

		Vec3f effectorTrans = in.globalEffectorMatrix.getTranslation(); //absolute position of the effector
		Vec3f loc = adjustGlobalToMatrix(effectorTrans, in.globalChainMatrix); //position of the effector as seen from the chain

		float distChainToEff = loc.length();

		//angle of ground-chain-effector - setup chain to face the ground
		float chainRotate = (float) Math.asin(-loc.z / distChainToEff);
		out.chainMatrix.rotate(chainRotate, 0, 1, 0);

		float distChainToJoint = in.localJointMatrix.getTranslation().length();
		float distJointToEff = in.localEffectorMatrix.getTranslation().length();

		if (distChainToEff < distJointToEff + distChainToJoint) {
			float angleCJE = MathEx.getAngleByCosineLaw(distJointToEff, distChainToJoint, distChainToEff);
			float angleECJ = MathEx.getAngleBySineLaw(distChainToEff, angleCJE, distJointToEff); //law of sines should be slightly faster now that we have CJE
			
			out.chainMatrix.rotate(-angleECJ, 0, 1, 0); //ECJ in the correct direction
			out.jointMatrix = out.chainMatrix.clone();
			out.jointMatrix.rotate((float)Math.PI - angleCJE, 0, 1, 0); //outer angle of CJE (we are rotating from chain matrix)
		} else {
			out.jointMatrix = out.chainMatrix.clone();
		}

		Vec3f jointPos = new Vec3f(distChainToJoint, 0, 0);
		jointPos.mulPosition(out.chainMatrix);
		
		out.jointMatrix.setTranslation(jointPos);
				 
		out.effectorMatrix = out.jointMatrix.clone();
		out.effectorMatrix.mul(in.localEffectorMatrix);

		return out;
	}

	public static Vec3f adjustGlobalToMatrix(Vec3f globalPosition, Matrix4 matrix) {
		Vec3f trans = matrix.getTranslation();
		trans.invert();
		Vec3f g = new Vec3f(globalPosition);
		g.add(trans);
		xstandard.math.vec.Quaternion rot = new xstandard.math.vec.Quaternion();
		Matrix3f norm = new Matrix3f();
		matrix.normalize3x3(norm);
		norm.getNormalizedRotation(rot);
		rot.invert();
		g.rotate(rot);
		return g;
	}

	public static class IKInput {
		
		public Joint chain;
		public Joint joint;
		public Joint effector;

		public Matrix4 globalChainMatrix;
		public Matrix4 localJointMatrix;
		public Matrix4 localEffectorMatrix;

		public Matrix4 globalEffectorMatrix;
	}

	public static class IKOutput {

		public Matrix4 chainMatrix = new Matrix4();
		public Matrix4 jointMatrix = new Matrix4();
		public Matrix4 effectorMatrix = new Matrix4();
	}
}
