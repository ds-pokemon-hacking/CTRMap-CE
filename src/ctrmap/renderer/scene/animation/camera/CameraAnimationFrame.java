package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.AnimatedValue;

public abstract class CameraAnimationFrame {

	public AnimatedValue fov;
	public AnimatedValue zNear;
	public AnimatedValue zFar;

	public AnimatedValue tx;
	public AnimatedValue ty;
	public AnimatedValue tz;

	protected abstract void applyCameraEx(Camera cam);

	public void applyToCamera(Camera cam) {
		applyCameraCommon(cam);
		applyCameraEx(cam);
		//System.out.println(this);
	}

	protected void applyCameraCommon(Camera cam) {
		if (fov.exists) {
			cam.FOV = fov.value;
		}
		if (zNear.exists) {
			cam.zNear = zNear.value;
		}
		if (zFar.exists) {
			cam.zFar = zFar.value;
		}
		if (tx.exists) {
			cam.translation.x = tx.value;
		}
		if (ty.exists) {
			cam.translation.y = ty.value;
		}
		if (tz.exists) {
			cam.translation.z = tz.value;
		}
	}
}
