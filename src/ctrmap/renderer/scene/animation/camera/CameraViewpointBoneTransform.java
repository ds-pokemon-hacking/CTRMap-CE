package ctrmap.renderer.scene.animation.camera;

import static ctrmap.renderer.scene.animation.AbstractBoneTransform.getNearKeyFrame;
import ctrmap.renderer.scene.animation.AnimatedValue;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.math.MathEx;
import xstandard.math.vec.Quaternion;
import xstandard.math.vec.Vec3f;
import xstandard.util.ArraysEx;
import java.util.List;

public class CameraViewpointBoneTransform extends CameraBoneTransform {

	public KeyFrameList rx = new KeyFrameList();
	public KeyFrameList ry = new KeyFrameList();
	public KeyFrameList rz = new KeyFrameList();

	@Override
	public List<KeyFrameList> getAllKfLists() {
		return ArraysEx.asList(fov, tx, ty, tz, rx, ry, rz);
	}
	
	public boolean hasRotation() {
		return KeyFrameList.existAny(rx, ry, rz);
	}

	private float searchExistingRotFrame(float frame, boolean backwards) {
		KeyFrame x = getNearKeyFrame(rx, frame, backwards);
		KeyFrame y = getNearKeyFrame(ry, frame, backwards);
		KeyFrame z = getNearKeyFrame(rz, frame, backwards);
		if (backwards) {
			if (x != null && (x.frame <= frame) && (y == null || x.frame > y.frame) && (z == null || x.frame > z.frame)) {
				return x.frame;
			} else if (y != null && (y.frame <= frame) && (z == null || y.frame > z.frame)) {
				return y.frame;
			}
			return (z != null && z.frame <= frame) ? z.frame : -1f;
		} else {
			if (x != null && (x.frame >= frame) && (y == null || x.frame < y.frame) && (z == null || x.frame < z.frame)) {
				return x.frame;
			} else if (y != null && (y.frame >= frame) && (z == null || y.frame < z.frame)) {
				return y.frame;
			}
			return (z != null && z.frame >= frame) ? z.frame : -1f;
		}
	}

	private boolean getIsRotationNonHermite() {
		KeyFrameList[] kfls = new KeyFrameList[]{rx, ry, rz};
		for (KeyFrameList l : kfls) {
			for (KeyFrame kf : l) {
				if (kf.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
					return false;
				}
			}
		}
		return true;
	}

	private CameraViewpointFrame getRotationExact(float frame) {
		CameraViewpointFrame frm = new CameraViewpointFrame();
		frm.rx = getValueAt(rx, frame, false);
		frm.ry = getValueAt(ry, frame, false);
		frm.rz = getValueAt(rz, frame, false);
		if (isRadians) {
			frm.rx.value *= MathEx.RADIANS_TO_DEGREES;
			frm.ry.value *= MathEx.RADIANS_TO_DEGREES;
			frm.rz.value *= MathEx.RADIANS_TO_DEGREES;
		}
		return frm;
	}

	@Override
	public CameraViewpointFrame getFrame(float frame, boolean doNotInterpolate) {
		boolean nonHermite = getIsRotationNonHermite();

		float floorFrame = -1f;
		if (nonHermite) {
			floorFrame = searchExistingRotFrame(frame, true);
		}
		if (floorFrame == -1f) {
			floorFrame = (float) Math.floor(frame);
		}

		float frameDiff = frame - floorFrame;
		CameraViewpointFrame frm;
		if (doNotInterpolate || frameDiff == 0f || (rx.isEmpty() || ry.isEmpty() || rz.isEmpty())) {
			frm = getRotationExact(frame);
		} else {
			frm = new CameraViewpointFrame();

			float ceilFrame = -1f;
			if (nonHermite) {
				ceilFrame = searchExistingRotFrame(frame, false);
			}
			if (ceilFrame == -1f) {
				ceilFrame = (float) Math.ceil(frame);
			}

			CameraViewpointFrame left = getRotationExact(floorFrame);
			CameraViewpointFrame right = getRotationExact(ceilFrame);

			Quaternion q = left.getRotationQuat(false);
			q.slerp(right.getRotationQuat(false), frameDiff / (ceilFrame - floorFrame));
			Vec3f eulers = q.getEulerRotation();
			frm.rx = new AnimatedValue(MathEx.toDegreesf(eulers.x));
			frm.ry = new AnimatedValue(MathEx.toDegreesf(eulers.y));
			frm.rz = new AnimatedValue(MathEx.toDegreesf(eulers.z));
		}
		setCommonFrame(frm, frame, doNotInterpolate);
		return frm;
	}

	@Override
	public boolean exists() {
		return super.exists() || KeyFrameList.existAny(rx, ry, rz);
	}
}
