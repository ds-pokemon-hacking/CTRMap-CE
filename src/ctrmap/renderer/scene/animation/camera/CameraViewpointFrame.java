package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.AnimatedValue;
import xstandard.math.MathEx;
import xstandard.math.vec.Quaternion;

public class CameraViewpointFrame extends CameraAnimationFrame {

	public AnimatedValue rx;
	public AnimatedValue ry;
	public AnimatedValue rz;

	@Override
	protected void applyCameraEx(Camera cam) {
		if (rx.exists) {
			cam.rotation.x = rx.value;
		}
		if (ry.exists) {
			cam.rotation.y = ry.value;
		}
		if (rz.exists) {
			cam.rotation.z = rz.value;
		}
		cam.mode = Camera.Mode.PERSPECTIVE;
	}
	
	public void loadDefaultsIfNeeded(Camera cam, boolean toRadians) {
		float mul = toRadians ? MathEx.DEGREES_TO_RADIANS : 1f;
		if (!rx.exists) {
			rx.value = cam.rotation.x * mul;
		}
		if (!ry.exists) {
			ry.value = cam.rotation.y * mul;
		}
		if (!rz.exists) {
			rz.value = cam.rotation.z * mul;
		}
	}

	public Quaternion getRotationQuat(boolean rad) {
		return getRotationQuat(rad, new Quaternion());
	}
	
	public Quaternion getRotationQuat(boolean rad, Quaternion dest) {
		if (!rad) {
			dest.rotationZYX(MathEx.toRadiansf(rz.value), MathEx.toRadiansf(ry.value), MathEx.toRadiansf(rx.value));
		} else {
			dest.rotationZYX(rz.value, ry.value, rx.value);
		}
		return dest;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Position: ");
		sb.append(tx);
		sb.append("/");
		sb.append(ty);
		sb.append("/");
		sb.append(tz);
		sb.append("; ");
		sb.append("FOV: ");
		sb.append(fov);
		sb.append("; ");
		sb.append("View rotation: ");
		sb.append(rx);
		sb.append("/");
		sb.append(ry);
		sb.append("/");
		sb.append(rz);
		return sb.toString();
	}
}
