package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.AnimatedValue;

public class CameraLookAtFrame extends CameraAnimationFrame {

	public AnimatedValue targetX;
	public AnimatedValue targetY;
	public AnimatedValue targetZ;

	public AnimatedValue upX;
	public AnimatedValue upY;
	public AnimatedValue upZ;
	
	@Override
	protected void applyCameraEx(Camera cam) {
		if (targetX.exists) {
			cam.lookAtTarget.x = targetX.value;
		}
		if (targetY.exists) {
			cam.lookAtTarget.y = targetY.value;
		}
		if (targetZ.exists) {
			cam.lookAtTarget.z = targetZ.value;
		}
		if (upX.exists){
			cam.lookAtUpVec.x = upX.value;
		}
		if (upY.exists){
			cam.lookAtUpVec.y = upY.value;
		}
		if (upZ.exists){
			cam.lookAtUpVec.z = upZ.value;
		}
		cam.mode = Camera.Mode.LOOKAT;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Position: ");
		sb.append(tx);
		sb.append("/");
		sb.append(ty);
		sb.append("/");
		sb.append(tz);
		sb.append("; FOV: ");
		sb.append(fov);
		sb.append("; LookAt Target: ");
		sb.append(targetX);
		sb.append("/");
		sb.append(targetY);
		sb.append("/");
		sb.append(targetZ);
		sb.append("; Up Vector: ");
		sb.append(upX);
		sb.append("/");
		sb.append(upY);
		sb.append("/");
		sb.append(upZ);
		return sb.toString();
	}
}
