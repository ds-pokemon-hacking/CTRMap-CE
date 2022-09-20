package ctrmap.missioncontrol_base;

import ctrmap.missioncontrol_base.debug.IMCDebugger;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;

public interface DebugCameraController extends IMCDebugger {
	public void setDebugCameraMotionEnabled(boolean value);
	public boolean getDebugCameraEnabled();
	public void addSceneTarget(Scene scene);
	public void activateCamera();
	public void deactivateCamera();
	public Camera getDebugCamera();
	public void onControllerActivated();
}
