package ctrmap.renderer.util;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.animation.*;
import ctrmap.renderer.scene.animation.camera.*;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.*;
import ctrmap.renderer.scene.model.*;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scenegraph.G3DResourceState;
import ctrmap.renderer.util.camcvt.SkeletalMatrixBakery;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

public class AnimeProcessor {
	
	private static final float _1_30 = 1f / 30f;
	
	public static float convFpsFrom30(float value, float destFps) {
		return value * _1_30 * destFps;
	}

	public static int getWholeFPS(AbstractAnimation animation) {
		int maxlevel = 3;
		boolean[] levelsDone = new boolean[3];

		Outer:
		for (AbstractBoneTransform bt : animation.getBones()) {
			for (KeyFrameList kfl : bt.getAllKfLists()) {
				for (KeyFrame kf : kfl) {
					float step = 0.25f;
					float modBase = 0.5f;
					
					for (int lv = 0; lv < maxlevel; lv++) {
						if (levelsDone[lv]) {
							break;
						}

						if (MathEx.impreciseFloatEquals(kf.frame % modBase, step, 0.001f)) {
							levelsDone[lv] = true;
						}
						else {
							step *= 2f;
							modBase *= 2f;
						}
					}
				}
			}
		}
		
		int fps = 15;
		for (int i = 0; i < maxlevel; i++) {
			if (levelsDone[i]) {
				fps *= 1 << (maxlevel - i);
				break;
			}
		}
		return fps;
	}

	public static void changeFPS(float sourceFPS, float targetFPS, KeyFrameList... kfls) {
		float frameMul = targetFPS / sourceFPS;
		for (KeyFrameList kfl : kfls) {
			applyTempoScale(kfl, frameMul);
		}
	}

	public static boolean checkConstant(List<KeyFrame>... kfls) {
		for (List<KeyFrame> kfl : kfls) {
			if (!kfl.isEmpty()) {
				KeyFrame _1st = kfl.get(0);
				for (KeyFrame kf : kfl) {
					if (kf.value != _1st.value) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static CameraAnimation skeletalToCamera(SkeletalAnimation anime, String transformJointName, String camTgtName, Skeleton refSkeleton, boolean localToGlobal, float xRotAdd) {
		CameraAnimation camAnm = new CameraAnimation();
		camAnm.frameCount = anime.frameCount;
		camAnm.name = anime.name;

		CameraViewpointBoneTransform out = new CameraViewpointBoneTransform();
		out.name = camTgtName;

		skeletalToCamera(anime, transformJointName, refSkeleton, localToGlobal, xRotAdd, out);

		camAnm.transforms.add(out);

		return camAnm;
	}

	public static void skeletalToCamera(SkeletalAnimation anime, String transformJointName, Skeleton refSkeleton, boolean localToGlobal, float xRotAdd, CameraViewpointBoneTransform out) {
		SkeletalBoneTransform bt = (SkeletalBoneTransform) anime.getBoneTransform(transformJointName);

		if (localToGlobal && refSkeleton != null) {
			List<SkeletalBoneTransform> transformList = new Stack<>();
			List<Joint> jointList = new Stack<>();

			Joint jnt = refSkeleton.getJoint(transformJointName);
			if (jnt.parentName != null) {
				//Do not apply on non-root bones
				xRotAdd = 0f;
			}
			while (jnt != null) {
				SkeletalBoneTransform jbt = (SkeletalBoneTransform) anime.getBoneTransform(jnt.name);
				if (jbt == null) {
					jbt = new SkeletalBoneTransform();
				}
				transformList.add(jbt);
				jointList.add(jnt);

				jnt = refSkeleton.getJoint(jnt.parentName);
			}

			Stack<Matrix4> mtxStack = new Stack<>();
			Vec3f t = new Vec3f();
			Vec3f r = new Vec3f();

			SkeletalAnimationTransformRequest transformReq = new SkeletalAnimationTransformRequest(0f);
			transformReq.bindJoint = jnt;

			for (int i = 0; i < anime.frameCount; i++) {
				for (int j = 0; j < transformList.size(); j++) {
					transformReq.bindJoint = jointList.get(j);
					transformReq.frame = i;
					mtxStack.push(transformList.get(j).getTransformMatrix(transformReq));
				}
				Matrix4 mtx = new Matrix4();
				while (!mtxStack.empty()) {
					mtx.mul(mtxStack.pop());
				}

				mtx.getTranslation(t);
				mtx.getRotationTo(r);

				out.rx.add(new KeyFrame(i, r.x + xRotAdd));
				out.ry.add(new KeyFrame(i, r.y));
				out.rz.add(new KeyFrame(i, r.z));
				out.tx.add(new KeyFrame(i, t.x));
				out.ty.add(new KeyFrame(i, t.y));
				out.tz.add(new KeyFrame(i, t.z));
			}
		} else {
			out.rx = new KeyFrameList(bt.rx, true);
			if (xRotAdd != 0f) {
				for (KeyFrame kf : out.rx) {
					kf.value += xRotAdd;
				}
			}

			out.ry = new KeyFrameList(bt.ry, true);
			out.rz = new KeyFrameList(bt.rz, true);

			out.tx = new KeyFrameList(bt.tx, true);
			out.ty = new KeyFrameList(bt.ty, true);
			out.tz = new KeyFrameList(bt.tz, true);
		}
		kflRadToDeg(out.rx);
		kflRadToDeg(out.ry);
		kflRadToDeg(out.rz);
	}

	private static void kflRadToDeg(List<KeyFrame> kfl) {
		for (KeyFrame kf : kfl) {
			kf.value = (float) Math.toDegrees(kf.value);
		}
	}

	public static void stripIKs(SkeletalAnimation anm) {
		if (anm.skeleton != null) {
			//Old SPICA method

			for (Joint chain : anm.skeleton) {
				if (chain.kinematicsRole == Skeleton.KinematicsRole.CHAIN) {
					Joint joint = chain.getChildByType(Skeleton.KinematicsRole.JOINT);
					if (joint != null) {
						Joint effector = joint.getChildByType(Skeleton.KinematicsRole.EFFECTOR);

						if (effector != null) {
							SkeletalBoneTransform btJ = (SkeletalBoneTransform) anm.getBoneTransform(joint.name);
							SkeletalBoneTransform btE = (SkeletalBoneTransform) anm.getBoneTransform(effector.name);

							if (btJ != null && btE != null) {
								Vec3f[] localT = new Vec3f[(int) anm.frameCount + 1];
								Vec3f[] jointR = new Vec3f[(int) anm.frameCount + 1];

								Quaternion tempRotQuat = new Quaternion();

								SkeletalMatrixBakery jointBakery = new SkeletalMatrixBakery(anm, anm.skeleton, joint);

								SkeletalAnimationTransformRequest reqEff = new SkeletalAnimationTransformRequest(0f);
								reqEff.bindJoint = effector;
								reqEff.rotation = false;
								reqEff.scale = false;

								SkeletalAnimationTransformRequest reqJnt = new SkeletalAnimationTransformRequest(0f);
								reqJnt.bindJoint = joint;
								reqJnt.rotation = false;
								reqJnt.scale = false;

								for (int frame = 0; frame <= anm.frameCount; frame++) {
									Matrix4 jointMtxG = jointBakery.manualBake(frame).invert(); //inverse parent matrix
									reqEff.frame = frame;
									reqJnt.frame = frame;
									Vec3f effTransGlobal = btE.getFrame(reqEff).getTranslation();
									effTransGlobal.mulPosition(jointMtxG);
									Vec3f jntTransLocal = btJ.getFrame(reqJnt).getTranslation();
									tempRotQuat.rotationTo(jntTransLocal, effTransGlobal);
									jointR[frame] = tempRotQuat.getEulerRotation();
									effTransGlobal.rotate(tempRotQuat.invert());
									localT[frame] = effTransGlobal;
								}

								joint.kinematicsRole = Skeleton.KinematicsRole.NONE;
								effector.kinematicsRole = Skeleton.KinematicsRole.NONE;

								btE.tx.clear();
								btE.ty.clear();
								btE.tz.clear();
								btJ.rx.clear();
								btJ.ry.clear();
								btJ.rz.clear();

								for (int i = 0; i <= anm.frameCount; i++) {
									btJ.addVector(i, jointR[i], btJ.rx, btJ.ry, btJ.rz);
									btE.addVector(i, localT[i], btE.tx, btE.ty, btE.tz);
								}
							}
						}
					}
				}
			}
		}
	}

	public static void transformToGlobal(SkeletalBoneTransform bt, SkeletalAnimation anime) {
		if (bt == null) {
			return;
		}
		SkeletalController dummyCtrl = new SkeletalController(anime);
		G3DResourceInstance dummyG3DInstance = new G3DResourceInstance();
		Model dummyModel = new Model();
		dummyModel.skeleton = anime.skeleton;
		dummyG3DInstance.resource.addModel(dummyModel);

		List<KeyFrameTuple> tuples = createTuples(bt.tx, bt.ty, bt.tz);

		if (tuples.isEmpty()) {
			Joint bindJoint = anime.skeleton.getJoint(bt.name);
			bt.tx.add(new KeyFrame(0, bindJoint.position.x));
			bt.ty.add(new KeyFrame(0, bindJoint.position.y));
			bt.tz.add(new KeyFrame(0, bindJoint.position.z));
			tuples = createTuples(bt.tx, bt.ty, bt.tz);
		}

		for (KeyFrameTuple t : tuples) {
			dummyCtrl.frame = t.frame;
			dummyCtrl.makeAnimationMatrices(dummyCtrl.frame, anime.skeleton);
			G3DResourceState dummyState = new G3DResourceState(dummyG3DInstance);
			Matrix4 mtx = dummyState.getAnimatedJointMatrix(dummyModel.skeleton.getJoint(bt.name), dummyState.getFastSkeleton(dummyModel.skeleton));
			if (mtx != null) {
				Vec3f globalTrans = mtx.getTranslation();
				if (t.x != null) {
					t.x.value = globalTrans.x;
				}
				if (t.y != null) {
					t.y.value = globalTrans.y;
				}
				if (t.z != null) {
					t.z.value = globalTrans.z;
				}
			}
		}
	}

	public static List<KeyFrameTuple> createTuples(List<KeyFrame> x, List<KeyFrame> y, List<KeyFrame> z) {
		List<KeyFrame> allList = new ArrayList<>();
		List<KeyFrameTuple> tuples = new ArrayList<>();
		allList.addAll(x);
		allList.addAll(y);
		allList.addAll(z);
		for (int i = 0; i < allList.size(); i++) {
			KeyFrame kf = allList.get(i);
			float f = kf.frame;
			KeyFrame xf = getKeyFrameAt(x, f);
			KeyFrame yf = getKeyFrameAt(y, f);
			KeyFrame zf = getKeyFrameAt(z, f);
			if (xf != null && yf != null && zf != null) {
				KeyFrameTuple tuple = new KeyFrameTuple();
				tuple.frame = f;
				tuple.x = xf;
				tuple.y = yf;
				tuple.z = zf;
				tuples.add(tuple);
				removeIfNotEqual(xf, kf, allList);
				removeIfNotEqual(yf, kf, allList);
				removeIfNotEqual(zf, kf, allList);
				i = allList.indexOf(kf) - 1;
				allList.remove(kf);
			}
		}
		return tuples;
	}

	private static void removeIfNotEqual(Object o, Object ref, List list) {
		if (o != ref) {
			list.remove(o);
		}
	}

	public static KeyFrame getKeyFrameAt(List<KeyFrame> kfl, float frame) {
		for (KeyFrame frm : kfl) {
			if (frm.frame == frame) {
				return frm;
			}
		}
		return null;
	}

	public static class KeyFrameTuple {

		public float frame;
		public KeyFrame x;
		public KeyFrame y;
		public KeyFrame z;

		public boolean isFull() {
			return x != null && y != null && z != null;
		}
	}

	public static void applyTempoScale(KeyFrameList kfl, float scale) {
		float scaleInv = 1f / scale;
		for (KeyFrame kf : kfl) {
			kf.frame *= scale;
			//The slopes have to be scaled with the inverse value because the FrameDiff in the weird method of hermite that the games use gets scaled up
			if (kf.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
				kf.inSlope *= scaleInv;
				kf.outSlope *= scaleInv;
			}
		}
	}

	//Tempo change
	public static void applyTempoScale(AbstractAnimation a, float scale) {
		a.frameCount *= scale;

		for (AbstractBoneTransform bt : a.getBones()) {
			for (KeyFrameList kfg : bt.getAllKfLists()) {
				applyTempoScale(kfg, scale);
			}
		}
	}

	//Translation scale
	public static void scaleSklAnimeTra(SkeletalAnimation a, float scale) {
		for (SkeletalBoneTransform bt : a.bones) {
			scaleTraKFL(bt.tx, bt.sx, scale);
			scaleTraKFL(bt.ty, bt.sy, scale);
			scaleTraKFL(bt.tz, bt.sz, scale);
		}
		if (a.skeleton != null) {
			for (Joint motionBone : a.skeleton) {
				motionBone.position.x *= scale * motionBone.scale.x;
				motionBone.position.y *= scale * motionBone.scale.y;
				motionBone.position.z *= scale * motionBone.scale.z;
			}
		}
	}

	private static void scaleTraKFL(KeyFrameList traKFL, KeyFrameList scaKFL, float scale) {
		for (KeyFrame kf : traKFL) {
			AnimatedValue internalScale = AbstractBoneTransform.getValueAt(scaKFL, kf.frame);
			float realScale = scale * (internalScale.exists ? internalScale.value : 1f);
			kf.value *= realScale;
		}
	}

	//AnimeOptimizer
	public static final float FULL_CIRCLE = (float) Math.PI * 2f;//2 Pi
	public static final float HALF_CIRCLE = (float) Math.PI;//Pi
	public static final float FRAME_TRIGGER_THRESHOLD = 5f;

	public static void makeAnimeWholeFrames(AbstractAnimation a) {
		for (AbstractBoneTransform bt : a.getBones()) {
			for (List<KeyFrame> kfl : bt.getAllKfLists()) {
				makeKFLWholeFrames(kfl);
			}
		}
	}

	public static int makeKFLWholeFrames(List<KeyFrame> kfl) {
		float roundFrame, weightSum, weight, valueSum;
		int roundFrameI;
		int count = 0;

		List<KeyFrame> ignoredFrames = new ArrayList<>();

		for (int i = 0; i < kfl.size(); i++) {
			KeyFrame frm = kfl.get(i);
			if (ignoredFrames.contains(frm)) {
				continue;
			}
			roundFrameI = Math.round(frm.frame);
			roundFrame = roundFrameI;
			List<KeyFrame> allFramesOfFrameAverage = collectKFsForFrame(kfl, i, roundFrameI);
			if (allFramesOfFrameAverage.size() > 1) {
				weightSum = 0f;
				for (KeyFrame f : allFramesOfFrameAverage) {
					weight = Math.abs(f.frame - roundFrame);
					weightSum += weight;
				}
				valueSum = 0f;
				for (KeyFrame f : allFramesOfFrameAverage) {
					weight = Math.abs(f.frame - roundFrame);
					valueSum += f.value * (weight / weightSum);
				}
				frm.value = valueSum;
				frm.frame = roundFrame;
				allFramesOfFrameAverage.remove(frm);
				count += allFramesOfFrameAverage.size();
				ignoredFrames.addAll(allFramesOfFrameAverage);
			}
		}
		kfl.removeAll(ignoredFrames);
		/*List<Float> occupiedFrames = new ArrayList<>();
		for (KeyFrame kf : kfl) {
			if (!occupiedFrames.contains(kf.frame)){
				occupiedFrames.add(kf.frame);
			}
			else {
				System.err.println("Frame not removed ! ! (should not happen!?)");
			}
		}*/
		return count;
	}

	private static List<KeyFrame> collectKFsForFrame(List<KeyFrame> kfl, int kflSearchStartIndex, int frame) {
		List<KeyFrame> r = new ArrayList<>();
		for (int i = kflSearchStartIndex; i < kfl.size(); i++) {
			KeyFrame kf = kfl.get(i);
			if (Math.round(kf.frame) == frame) {
				r.add(kf);
			} else {
				return r;
			}
		}
		return r;
	}

	public static void optimizeSkeletalAnimation(SkeletalAnimation anime) {
		optimizeSkeletalAnimation(anime, false);
	}

	public static void optimizeSkeletalAnimation(SkeletalAnimation anime, boolean notForRuntime) {
		for (SkeletalBoneTransform bt : anime.bones) {
			if (notForRuntime) {
				optimizeRotKFL(bt.rx);
				optimizeRotKFL(bt.ry);
				optimizeRotKFL(bt.rz); //no longer needed thanks to quaternion slerping
			}
			createStepTranslations(bt.tx);
			createStepTranslations(bt.ty);
			createStepTranslations(bt.tz);
			createStepRotations(bt.rx);
			createStepRotations(bt.ry);
			createStepRotations(bt.rz);
			createStepScales(bt.sx);
			createStepScales(bt.sy);
			createStepScales(bt.sz);
		}
	}

	public static void optimizeCameraPosTrack(KeyFrameList kfl, float frameCount) {
		if (kfl.size() >= 3) {
			float lastValue = Float.MAX_VALUE;

			float lastDiff = 0f;

			KeyFrame lastKF = null;

			for (float f = 0; f < frameCount; f++) {
				float val = 0f;
				KeyFrame kf = kfl.getByFrameRounddown(f);
				if (kf != null) {
					val = kf.value;
				} else {
					val = AbstractBoneTransform.getValueAt(kfl, f).value;
				}
				if (lastValue != Float.MAX_VALUE) {
					float diff = val - lastValue;

					if (diff > 100f * lastDiff) {
						if (lastKF != null) {
							lastKF.interpolation = KeyFrame.InterpolationMethod.STEP;
						}
					} else {
						lastDiff = diff;
					}
				}
				lastKF = kf;
			}
		}
	}

	public static void optimizeCameraRotTrack(KeyFrameList kfl, boolean radian) {
		float threshold = 20f;
		if (radian) {
			threshold *= MathEx.DEGREES_TO_RADIANS;
		}
		for (int i = 0; i < kfl.size() - 1; i++) {
			KeyFrame kf = kfl.get(i);
			KeyFrame next = kfl.get(i + 1);
			if (next.frame - kf.frame < 2f && Math.abs(next.value - kf.value) > threshold) {
				kf.interpolation = KeyFrame.InterpolationMethod.STEP;
			}
		}
	}

	public static void syncSteps(KeyFrameList... lists) {
		HashSet<Float> stepFrames = new HashSet<>();
		for (KeyFrameList l : lists) {
			for (KeyFrame f : l) {
				if (f.interpolation == KeyFrame.InterpolationMethod.STEP) {
					stepFrames.add(f.frame);
				}
			}
		}

		for (KeyFrameList l : lists) {
			for (KeyFrame f : l) {
				if (stepFrames.contains(f.frame)) {
					f.interpolation = KeyFrame.InterpolationMethod.STEP;
				}
			}
		}
	}

	public static void optimizeCameraAnimation(CameraAnimation anime) {
		for (CameraBoneTransform bt : anime.transforms) {
			/*List<KeyFrameTuple> tuplesR = null;
			if (bt instanceof CameraViewpointBoneTransform) {
				CameraViewpointBoneTransform vp = (CameraViewpointBoneTransform) bt;
				tuplesR = createTuples(vp.rx, vp.ry, vp.rz);
			}*/

 /*optimizeCameraPosTuples(bt.tx, bt.ty, bt.tz, tuplesR);*/
			optimizeCameraPosTrack(bt.tx, anime.frameCount);
			optimizeCameraPosTrack(bt.ty, anime.frameCount);
			optimizeCameraPosTrack(bt.tz, anime.frameCount);

			if (bt instanceof CameraLookAtBoneTransform) {
				CameraLookAtBoneTransform la = (CameraLookAtBoneTransform) bt;
				//optimizeCameraPosTuples(la.targetTX, la.targetTY, la.targetTZ, tuplesR);
			} else if (bt instanceof CameraViewpointBoneTransform) {
				CameraViewpointBoneTransform vp = (CameraViewpointBoneTransform) bt;
				/*optimizeCameraRotTrack(vp.rx, vp.isRadians);
				optimizeCameraRotTrack(vp.ry, vp.isRadians);
				optimizeCameraRotTrack(vp.rz, vp.isRadians);*/

				Quaternion q = new Quaternion();
				Quaternion qp = new Quaternion();

				List<KeyFrameTupleHandle> thl = makeBakedTuples(vp.rx, vp.ry, vp.rz, anime.frameCount);

				for (int i = 1; i < thl.size(); i++) {
					KeyFrameTupleHandle t = thl.get(i);
					KeyFrameTupleHandle prev = thl.get(i - 1);

					q.identity();
					qp.identity();
					if (vp.isRadians) {
						q.rotateZYX(t.z, t.y, t.x);
						qp.rotateZYX(prev.z, prev.y, prev.x);
					} else {
						q.rotateZYXDeg(t.z, t.y, t.x);
						qp.rotateZYXDeg(prev.z, prev.y, prev.x);
					}

					q.difference(qp);

					float ang = q.angle();
					if (ang > MathEx.PI) {
						ang = MathEx.TWO_PI - ang;
					}
					/*System.out.println("last " + prev.x + " / " + prev.y + " / " + prev.z);
						System.out.println("now " + t.x + " / " + t.y + " / " + t.z);
						System.out.println(ang);*/
					if (ang > 0.5f) {
						/*System.out.println("last " + prev.x + " / " + prev.y + " / " + prev.z);
						System.out.println("now " + t.x + " / " + t.y + " / " + t.z);*/
						prev.setStepInterp();
					}
				}

				syncSteps(bt.tx, bt.ty, bt.tz, vp.rx, vp.ry, vp.rz);
			}
		}
	}

	public static List<KeyFrameTupleHandle> makeBakedTuples(KeyFrameList x, KeyFrameList y, KeyFrameList z, float frameCount) {
		List<KeyFrameTupleHandle> r = new ArrayList<>();
		for (float f = 0; f < frameCount; f++) {
			r.add(new KeyFrameTupleHandle(f, x.getByFrameRounddown(f), y.getByFrameRounddown(f), z.getByFrameRounddown(f), x, y, z));
		}
		return r;
	}

	public static class KeyFrameTupleHandle {

		public float x;
		public float y;
		public float z;

		private KeyFrame kfX;
		private KeyFrame kfY;
		private KeyFrame kfZ;

		public KeyFrameTupleHandle(float frame, KeyFrame kfX, KeyFrame kfY, KeyFrame kfZ, KeyFrameList lX, KeyFrameList lY, KeyFrameList lZ) {
			x = kfX != null ? kfX.value : AbstractBoneTransform.getValueAt(lX, frame).value;
			y = kfY != null ? kfY.value : AbstractBoneTransform.getValueAt(lY, frame).value;
			z = kfZ != null ? kfZ.value : AbstractBoneTransform.getValueAt(lZ, frame).value;
			this.kfX = kfX;
			this.kfY = kfY;
			this.kfZ = kfZ;
		}

		public void setStepInterp() {
			if (kfX != null) {
				kfX.interpolation = KeyFrame.InterpolationMethod.STEP;
			}
			if (kfY != null) {
				kfY.interpolation = KeyFrame.InterpolationMethod.STEP;
			}
			if (kfZ != null) {
				kfZ.interpolation = KeyFrame.InterpolationMethod.STEP;
			}
		}
	}

	private static void optimizeCameraPosTuples(List<KeyFrame> x, List<KeyFrame> y, List<KeyFrame> z, List<KeyFrameTuple> tuplesR) {
		if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
			return;
		}
		List<KeyFrameTuple> tuplesT = createTuples(x, y, z);

		float traThreshold = getKFLBBoxDim(x, y, z) / 50f;

		for (int i = 1; i < tuplesT.size(); i++) {
			KeyFrameTuple t = tuplesT.get(i);
			if (t.isFull()) {
				KeyFrameTuple prev = tuplesT.get(i - 1);

				if (t.frame - prev.frame < 1.5f) {
					if (new Vec3f(prev.x.value, prev.y.value, prev.z.value).distance(t.x.value, t.y.value, t.z.value) > traThreshold) {
						prev.x.interpolation = KeyFrame.InterpolationMethod.STEP;
						prev.y.interpolation = KeyFrame.InterpolationMethod.STEP;
						prev.z.interpolation = KeyFrame.InterpolationMethod.STEP;

						if (tuplesR != null) {
							for (KeyFrameTuple r : tuplesR) {
								if (r.frame == prev.frame) {
									if (r.x != null) {
										r.x.interpolation = KeyFrame.InterpolationMethod.STEP;
									}
									if (r.y != null) {
										r.y.interpolation = KeyFrame.InterpolationMethod.STEP;
									}
									if (r.z != null) {
										r.z.interpolation = KeyFrame.InterpolationMethod.STEP;
									}
									break;
								}
							}
						}
					}
				}
			}
		}
	}

	public static float getKFLBBoxDim(List<KeyFrame>... kfls) {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (List<KeyFrame> l : kfls) {
			for (KeyFrame kf : l) {
				min = Math.min(min, kf.value);
				max = Math.max(max, kf.value);
			}
		}
		return max - min;
	}

	public static void optimizeMaterialAnimation(MaterialAnimation anime) {
		for (MatAnimBoneTransform bt : anime.bones) {
			for (int c = 0; c < 3; c++) {
				createStepTranslationsMta(bt.mtx[c]);
				createStepTranslationsMta(bt.mty[c]);
			}
		}
	}

	public static void rebaseRotKFL(List<KeyFrame> l) {
		for (KeyFrame kf : l) {
			kf.value %= FULL_CIRCLE;
			if (kf.value > HALF_CIRCLE) {
				kf.value = -FULL_CIRCLE - kf.value;
			}
			if (kf.value < -HALF_CIRCLE) {
				kf.value = FULL_CIRCLE + kf.value;
			}
		}
	}

	public static void optimizeRotKFL(List<KeyFrame> l) {
		optimizeRotKFL(l, FRAME_TRIGGER_THRESHOLD);
	}

	public static void optimizeRotKFL(List<KeyFrame> l, float threshold) {
		for (int i = 0; i < l.size() - 1; i++) {
			KeyFrame thisKF = l.get(i);
			KeyFrame nextKF = l.get(i + 1);
			if (nextKF.frame - thisKF.frame < threshold) {
				float absoluteDiff = Math.abs(nextKF.value - thisKF.value);
				if (absoluteDiff > HALF_CIRCLE) {
					float thisSign = Math.signum(thisKF.value);
					float nkfv = nextKF.value % FULL_CIRCLE;
					if ((thisSign * nkfv) < 0) {
						nkfv = thisSign * FULL_CIRCLE + nkfv;
					}
					float baseDiff = nkfv - thisKF.value % FULL_CIRCLE;
					float newValue = thisKF.value + baseDiff;
					nextKF.value = newValue;
				}
			}
		}
	}

	public static void createStepRotations(List<KeyFrame> l) {
		for (int i = 0; i < l.size() - 1; i++) {
			KeyFrame thisKF = l.get(i);
			KeyFrame nextKF = l.get(i + 1);

			if (nextKF.frame - thisKF.frame <= 1.01f) {
				if (Math.abs(nextKF.value - thisKF.value) >= HALF_CIRCLE * 0.2f) {
					thisKF.interpolation = KeyFrame.InterpolationMethod.STEP;
				}
			}
		}
	}

	public static void createStepTranslations(List<KeyFrame> l) {
		float min = Float.MAX_VALUE;
		float max = Float.MIN_VALUE;
		for (KeyFrame kf : l) {
			if (kf.value < min) {
				min = kf.value;
			}
			if (kf.value > max) {
				max = kf.value;
			}
		}
		float maxShift = (float) Math.pow(Math.abs(max - min), 0.7f);
		for (int i = 0; i < l.size() - 1; i++) {
			KeyFrame thisKF = l.get(i);
			KeyFrame nextKF = l.get(i + 1);

			if (nextKF.frame - thisKF.frame <= 1.01f && Math.abs(nextKF.value - thisKF.value) > maxShift) {
				thisKF.interpolation = KeyFrame.InterpolationMethod.STEP;
			}
		}
	}

	public static void createStepScales(List<KeyFrame> l) {
		for (int i = 0; i < l.size() - 1; i++) {
			KeyFrame thisKF = l.get(i);
			KeyFrame nextKF = l.get(i + 1);

			if (nextKF.frame - thisKF.frame == 1f) {
				if (Math.abs(nextKF.value - thisKF.value) == 1f) {
					thisKF.interpolation = KeyFrame.InterpolationMethod.STEP;
				}
			}
		}
	}

	public static void createStepTranslationsMta(List<KeyFrame> l) {
		for (int i = 0; i < l.size() - 1; i++) {
			KeyFrame thisKF = l.get(i);
			KeyFrame nextKF = l.get(i + 1);

			if (nextKF.frame - thisKF.frame < 1.05f) {
				/*float divided25 = Math.abs(nextKF.value - thisKF.value) / 0.25f;
				float divided10 = Math.abs(nextKF.value - thisKF.value) / 0.10f;
				boolean isCloseTo25Mult = divided25 != 0 && Math.abs(divided25 - Math.round(divided25)) < (0.01 * divided25);
				boolean isCloseTo10Mult = divided10 != 0 && Math.abs(divided10 - Math.round(divided10)) < (0.01 * divided10);
				if (isCloseTo25Mult || isCloseTo10Mult) {
					thisKF.interpolation = KeyFrame.InterpolationMethod.STEP;
				}*/
				if (Math.abs(nextKF.value - thisKF.value) > 0.11f) {
					thisKF.interpolation = KeyFrame.InterpolationMethod.STEP;
				}

				if (thisKF.interpolation == KeyFrame.InterpolationMethod.HERMITE && nextKF.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
					thisKF.interpolation = KeyFrame.InterpolationMethod.LINEAR;
					nextKF.interpolation = KeyFrame.InterpolationMethod.LINEAR;
				}
			}
		}
	}

	public static final float SKA_KF_THRESHOLD_TRA = 0.00001f;
	public static final float SKA_KF_THRESHOLD_ROT = 0.0001f;
	public static final float SKA_KF_THRESHOLD_SCA = 0.01f;

	public static int optimizeSkaKeyframes(SkeletalAnimation anime) {
		int count = 0;
		for (SkeletalBoneTransform bt : anime.bones) {
			count += createKeyframes(bt.tx, SKA_KF_THRESHOLD_TRA);
			count += createKeyframes(bt.ty, SKA_KF_THRESHOLD_TRA);
			count += createKeyframes(bt.tz, SKA_KF_THRESHOLD_TRA);
			count += createKeyframes(bt.rx, SKA_KF_THRESHOLD_ROT);
			count += createKeyframes(bt.ry, SKA_KF_THRESHOLD_ROT);
			count += createKeyframes(bt.rz, SKA_KF_THRESHOLD_ROT);
			count += createKeyframes(bt.sx, SKA_KF_THRESHOLD_SCA);
			count += createKeyframes(bt.sy, SKA_KF_THRESHOLD_SCA);
			count += createKeyframes(bt.sz, SKA_KF_THRESHOLD_SCA);
		}

		return count;
	}

	public static final float CMA_KF_THRESHOLD = 0.00001f;

	public static int optimizeCmaKeyframes(CameraAnimation anime) {
		int count = 0;
		for (CameraBoneTransform bt : anime.transforms) {
			for (List<KeyFrame> kfl : bt.getAllKfLists()) {
				count += createKeyframes(kfl, SKA_KF_THRESHOLD_TRA);
			}
		}
		return count;
	}

	public static int createKeyframes(List<KeyFrame> l, float threshold) {
		int count = 0;
		for (int i = 0; i < l.size() - 2; i++) {
			KeyFrame thisKF = l.get(i);
			KeyFrame nextKF = l.get(i + 1);
			KeyFrame afterKF = l.get(i + 2);
			AnimatedValue interpValue = AbstractBoneTransform.getValueInterpolated(thisKF, afterKF, nextKF.frame);
			if (interpValue.exists) { //it always should, but why not check it
				if (MathEx.impreciseFloatEquals(interpValue.value, nextKF.value, threshold)) {
					l.remove(i + 1);
					count++;
					i--;
				}
			}
		}

		if (l.size() == 2) {
			if (l.get(0).value == l.get(1).value) {
				l.remove(1);
			}
		}

		return count;
	}
}
