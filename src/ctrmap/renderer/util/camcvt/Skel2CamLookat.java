package ctrmap.renderer.util.camcvt;

import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraLookAtBoneTransform;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationTransformRequest;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Model;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Skel2CamLookat {

	public static CameraAnimation convertSkeletalAnimation(Skel2CamLookatInput input) {
		CameraLookAtBoneTransform camBT = new CameraLookAtBoneTransform();
		camBT.name = input.cameraName;

		convVector(input, input.posSrc, camBT.tx, camBT.ty, camBT.tz);
		convVector(input, input.tgtSrc, camBT.targetTX, camBT.targetTY, camBT.targetTZ);

		KeyFrameList camRoll = new KeyFrameList();

		convScalar(input, input.rollSrc, camRoll);

		for (KeyFrame kf : camRoll) {
			KeyFrame kfX = new KeyFrame(kf);
			kfX.value = (float) Math.sin(kfX.value);
			camBT.upX.add(kfX);
			KeyFrame kfY = new KeyFrame(kf);
			kfY.value = (float) Math.cos(kfY.value);
			camBT.upY.add(kfY);
		}

		CameraAnimation anm = new CameraAnimation();
		anm.name = input.anm.name;
		anm.frameCount = input.anm.frameCount;
		anm.metaData.putValues(input.anm.metaData.getValues());
		anm.transforms.add(camBT);

		return anm;
	}

	private static void convScalar(Skel2CamLookatInput input, Skel2CamScalarSource src, KeyFrameList tgt) {
		SkeletalBoneTransform bt = (SkeletalBoneTransform) input.anm.getBoneTransform(src.jntName);

		KeyFrameList kfl = new KeyFrameList();

		if (!input.bakeModeGlobal) {
			KeyFrameList[][] KFL_LUT = new KeyFrameList[][]{
				{bt.tx, bt.ty, bt.tz},
				{bt.rx, bt.ry, bt.rz},
				{bt.sx, bt.sy, bt.sz}
			};

			kfl.copy(KFL_LUT[src.jntVec.ordinal()][src.comp.ordinal()]);
		} else {
			SkeletalVectorBakery bakery = new SkeletalVectorBakery(
				input.anm, 
				input.refModel.skeleton, 
				input.refModel.skeleton.getJoint(src.jntName), 
				src.jntVec
			);
			
			float frame;
			for (Vec3f vec : bakery) {
				frame = bakery.getCurrentFrame();
				
				switch (src.comp) {
					case X:
						kfl.add(new KeyFrame(frame, vec.x));
						break;
					case Y:
						kfl.add(new KeyFrame(frame, vec.y));
						break;
					case Z:
						kfl.add(new KeyFrame(frame, vec.z));
						break;
				}
			}
		}

		switch (src.op) {
			case ADD_HPI:
				for (KeyFrame kf : kfl) {
					kf.value += MathEx.HALF_PI;
				}
				break;
			case SUB_HPI:
				for (KeyFrame kf : kfl) {
					kf.value -= MathEx.HALF_PI;
				}
				break;
			case NEGATE:
				for (KeyFrame kf : kfl) {
					kf.value = -kf.value;
				}
				break;
		}

		tgt.set(kfl);
	}

	private static void convVector(Skel2CamLookatInput input, Skel2CamVectorSource src, KeyFrameList tgtX, KeyFrameList tgtY, KeyFrameList tgtZ) {
		SkeletalBoneTransform bt = (SkeletalBoneTransform) input.anm.getBoneTransform(src.jntName);
		if (bt != null) {
			KeyFrameList srcX = new KeyFrameList();
			KeyFrameList srcY = new KeyFrameList();
			KeyFrameList srcZ = new KeyFrameList();

			if (!input.bakeModeGlobal) {
				switch (src.jntVec) {
					case ROTATION:
						srcX.copy(bt.rx);
						srcY.copy(bt.ry);
						srcZ.copy(bt.rz);
						break;
					case TRANSLATION:
						srcX.copy(bt.tx);
						srcY.copy(bt.ty);
						srcZ.copy(bt.tz);
						break;
					case SCALE:
						srcX.copy(bt.sx);
						srcY.copy(bt.sy);
						srcZ.copy(bt.sz);
						break;
				}
			} else {
				SkeletalVectorBakery bakery = new SkeletalVectorBakery(
					input.anm, 
					input.refModel.skeleton, 
					input.refModel.skeleton.getJoint(src.jntName), 
					src.jntVec
				);

				for (Vec3f bakeVec : bakery) {
					float frame = bakery.getCurrentFrame();

					srcX.add(new KeyFrame(frame, bakeVec.x));
					srcY.add(new KeyFrame(frame, bakeVec.y));
					srcZ.add(new KeyFrame(frame, bakeVec.z));
				}

			}
			tgtX.set(srcX);

			switch (src.op) {
				case NONE:
					tgtY.set(srcY);
					tgtZ.set(srcZ);
					break;
				case ROTX_HPINEG:
					tgtY.set(srcZ);
					tgtZ.set(srcY);
					for (KeyFrame kf : tgtZ) {
						kf.value = -kf.value;
					}
					break;
				case ROTX_HPIPOS:
					tgtY.set(srcZ);
					tgtZ.set(srcY);
					for (KeyFrame kf : tgtY) {
						kf.value = -kf.value;
					}
					break;
			}
		}
	}

	public static class Skel2CamLookatInput {

		public SkeletalAnimation anm;

		public Skel2CamVectorSource posSrc;
		public Skel2CamVectorSource tgtSrc;
		public Skel2CamScalarSource rollSrc;

		public String cameraName;

		public boolean bakeModeGlobal;
		public Model refModel;
	}
}
