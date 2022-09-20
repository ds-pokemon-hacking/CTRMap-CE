package ctrmap.editor.gui.editors.common.input;

import ctrmap.util.gui.cameras.OrthoCameraInputManager;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.missioncontrol_base.DebugCameraController;
import ctrmap.missioncontrol_base.IMCSurfaceListener;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import xstandard.math.vec.Vec3f;
import javax.swing.JComponent;

public class LevelEditor2DInputManager implements IMCSurfaceListener, DebugCameraController {

	private AbstractPerspective editors;
	public OrthoCameraInputManager orthoCam = new OrthoCameraInputManager();

	public LevelEditor2DInputManager(AbstractPerspective edt) {
		editors = edt;
	}

	@Override
	public boolean getDebugCameraEnabled() {
		return orthoCam.getAllowMotion();
	}

	@Override
	public Camera getDebugCamera() {
		return orthoCam.cam;
	}

	@Override
	public void attach(JComponent comp) {
		orthoCam.attachComponent(comp);
	}

	@Override
	public void detach(JComponent comp) {
		orthoCam.attachComponent(null);
	}

	public Vec3f getCenterCameraPos() {
		return new Vec3f((int) orthoCam.cam.translation.x, 0f, (int) orthoCam.cam.translation.z);
	}

	@Override
	public void setDebugCameraMotionEnabled(boolean value) {
		orthoCam.setAllowMotion(value);
	}

	@Override
	public void onControllerActivated() {
		for (AbstractTool tool : editors.getTools()) {
			tool.onViewportSwitch(true);
		}
	}

	@Override
	public void addSceneTarget(Scene scene) {
		orthoCam.addSceneTarget(scene);
	}

	@Override
	public void activateCamera() {
		orthoCam.activate();
	}

	@Override
	public void deactivateCamera() {
		orthoCam.deactivate();
	}
}
