package ctrmap.renderer.util;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.AnimatedValue;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.camera.CameraLookAtBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraLookAtFrame;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraViewpointFrame;
import xstandard.math.vec.Vec3f;
import java.util.ArrayList;
import java.util.List;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;

public class CameraFrameProcessor {

	public static int optimizeLookatTarget(CameraLookAtBoneTransform bt, float frameCount) {
		int iFrameCount = (int)Math.ceil(frameCount);
		int count = 0;
		if (KeyFrameList.existAll(bt.tx, bt.targetTX, bt.ty, bt.targetTY, bt.tz, bt.targetTZ)) {
			Vec3f lastTgt = new Vec3f();
			Vec3f tgt = new Vec3f();
			Vec3f lastPos = new Vec3f();
			Vec3f pos = new Vec3f();

			CameraLookAtFrame lastF = null;
			
			Vec3f currentTargetPoint = new Vec3f();
			Vec3f destDistance = new Vec3f();

			List<Vec3f> positions = new ArrayList<>(iFrameCount);
			List<Vec3f> targets = new ArrayList<>(iFrameCount);
			
			Vec3f rslTgt = new Vec3f();
			
			for (int i = 0; i < iFrameCount; i++) {
				CameraLookAtFrame f = bt.getFrame(i, false);
				pos.set(f.tx.value, f.ty.value, f.tz.value);
				tgt.set(f.targetX.value - f.tx.value, f.targetY.value - f.ty.value, f.targetZ.value - f.tz.value);
				tgt.mul(1000f);
				tgt.add(f.tx.value, f.ty.value, f.tz.value);
				rslTgt.set(tgt);
				if (lastF != null) {
					Vec3f.findShortestDistance(pos, tgt, lastPos, lastTgt, currentTargetPoint, destDistance);
					
					if (destDistance.length() < 1f) {
						rslTgt.set(currentTargetPoint);
						count++;
					}
					else {
						System.out.println(destDistance + ", len " + destDistance.length());
					}
				}
				lastF = f;
				lastTgt.set(f.targetX.value, f.targetY.value, f.targetZ.value);
				lastPos.set(pos);
				
				positions.add(pos.clone());
				targets.add(rslTgt.clone());
				System.out.println("tgt " + tgt + " of frame " + i);
			}
			
			bt.tx.clear();
			bt.ty.clear();
			bt.tz.clear();
			bt.targetTX.clear();
			bt.targetTY.clear();
			bt.targetTZ.clear();
			
			for (int i = 0; i < iFrameCount; i++) {
				Vec3f p = positions.get(i);
				Vec3f t = targets.get(i);
				bt.tx.add(new KeyFrame(i, p.x));
				bt.ty.add(new KeyFrame(i, p.y));
				bt.tz.add(new KeyFrame(i, p.z));
				bt.targetTX.add(new KeyFrame(i, t.x));
				bt.targetTY.add(new KeyFrame(i, t.y));
				bt.targetTZ.add(new KeyFrame(i, t.z));
			}
		}
		return count;
	}
	
	public static CameraViewpointBoneTransform lookatToViewpoint(CameraLookAtBoneTransform la, Camera cam, float frameCount) {
		CameraViewpointBoneTransform bt = new CameraViewpointBoneTransform();
		bt.name = la.name;
		bt.isRadians = true;
		copyKeyFrames(la.fov, bt.fov, la.isRadians ? 1f : MathEx.DEGREES_TO_RADIANS);
		copyKeyFrames(la.tx, bt.tx);
		copyKeyFrames(la.ty, bt.ty);
		copyKeyFrames(la.tz, bt.tz);
		
		cam = new Camera(cam);
		Vec3f rotation = new Vec3f();
		
		for (float frame = 0f; frame <= frameCount; frame = KeyFrameList.nextFrame(frame, la.targetTX, la.targetTY, la.targetTZ, la.upX, la.upY, la.upZ)) {
			CameraLookAtFrame f = la.getFrame(frame, false);
			f.applyToCamera(cam);
			cam.getTransformMatrix(false).getRotationTo(rotation);
			bt.addVector(frame, rotation, bt.rx, bt.ry, bt.rz);
		}
		
		return bt;
	}

	public static CameraLookAtBoneTransform viewpointToLookat(CameraViewpointBoneTransform vp, Camera cam, float frameCount) {
		CameraLookAtBoneTransform lookat = new CameraLookAtBoneTransform();
		lookat.name = vp.name;
		copyKeyFrames(vp.fov, lookat.fov);
		copyKeyFrames(vp.tx, lookat.tx);
		copyKeyFrames(vp.ty, lookat.ty);
		copyKeyFrames(vp.tz, lookat.tz);

		Vec3f rotVec_temp = new Vec3f();
		cam = new Camera(cam);

		for (float frame = 0f; frame <= frameCount; frame = KeyFrameList.nextFrame(frame, vp.rx, vp.ry, vp.rz)) {
			CameraViewpointFrame f = vp.getFrame(frame, false);
			f.applyToCamera(cam);

			rotVec_temp.set(cam.rotation);
			rotVec_temp.getDirFromEulersDegZYX(rotVec_temp);
			addValue(lookat.targetTX, frame, f.tx, rotVec_temp.x);
			addValue(lookat.targetTY, frame, f.ty, rotVec_temp.y);
			addValue(lookat.targetTZ, frame, f.tz, rotVec_temp.z);
			rotVec_temp.set(f.rx.value, f.ry.value, f.rz.value);
			rotVec_temp.getUpVecFromEulersDegZYX(rotVec_temp);
			addValue(lookat.upX, frame, rotVec_temp.x);
			addValue(lookat.upY, frame, rotVec_temp.y);
			addValue(lookat.upZ, frame, rotVec_temp.z);
		}

		return lookat;
	}

	private static void copyKeyFrames(KeyFrameList l1, KeyFrameList l2) {
		copyKeyFrames(l1, l2, 1f);
	}
	
	private static void copyKeyFrames(KeyFrameList l1, KeyFrameList l2, float mul) {
		for (KeyFrame kf : l1) {
			KeyFrame kf2 = new KeyFrame(kf);
			kf2.value *= mul;
			l2.add(kf2);
		}
	}

	private static void addValue(KeyFrameList kfl, float frame, float val) {
		kfl.add(new KeyFrame(frame, val));
	}

	private static void addValue(KeyFrameList kfl, float frame, AnimatedValue val, float off) {
		if (val.exists) {
			kfl.add(new KeyFrame(frame, val.value + off));
		}
	}
}
