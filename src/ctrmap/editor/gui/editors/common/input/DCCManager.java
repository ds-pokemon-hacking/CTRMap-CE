package ctrmap.editor.gui.editors.common.input;

import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.missioncontrol_base.DebugCameraController;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import java.util.HashMap;
import java.util.Map;

public class DCCManager implements DebugCameraController {

	public static final String CAMERA_CONTROLLER_ID_2D = "CM2D";
	public static final String CAMERA_CONTROLLER_ID_3D = "CM3D";

	private Map<String, DebugCameraController> cameraControllers = new HashMap<>();

	private AbstractPerspective editors;
	private boolean debugCameraEnabled = true;
	private DebugCameraController currentDcc = null;

	public DCCManager(AbstractPerspective editors) {
		this.editors = editors;
		cameraControllers.put(CAMERA_CONTROLLER_ID_2D, editors.tilemapInput);
		cameraControllers.put(CAMERA_CONTROLLER_ID_3D, editors.m3DInput);
		setDebugCamera(CAMERA_CONTROLLER_ID_2D);

		setDebugCameraEnabled(false);
	}
	
	public Iterable<DebugCameraController> camControllers() {
		return cameraControllers.values();
	}

	public DebugCameraController getCurrentDcc() {
		return currentDcc;
	}

	public void removeCameraController(String id) {
		cameraControllers.remove(id);
	}

	public void setDebugCameraEnabled(boolean v) {
		debugCameraEnabled = v;
		editors.m3DInput.setIsToolInputEnabled(v);

		if (debugCameraEnabled) {
			addInjectionScene();

			for (DebugCameraController c : cameraControllers.values()) {
				c.setDebugCameraMotionEnabled(c == currentDcc);
			}
		} else {
			removeInjectionScene();

			for (DebugCameraController ctrl : cameraControllers.values()) {
				ctrl.setDebugCameraMotionEnabled(false);
			}
		}
	}

	public void removeInjectionScene() {
		Scene inject = editors.getInjectionScene();

		if (inject != null) {
			inject.removeChild(editors.scn);
		}
	}

	public void addInjectionScene() {
		Scene inject = editors.getInjectionScene();

		if (inject != null) {
			inject.addChild(editors.scn);
		}
	}

	public void toggleDebugCameraEnabled() {
		setDebugCameraEnabled(!debugCameraEnabled);
	}

	public void setDebugCamera(String id) {
		if (cameraControllers.containsKey(id)) {
			DebugCameraController newDCC = cameraControllers.get(id);
			if (currentDcc != newDCC) {
				currentDcc = newDCC;
				for (DebugCameraController c : cameraControllers.values()) {
					c.setDebugCameraMotionEnabled(c == currentDcc);
				}
				if (currentDcc != null) {
					currentDcc.onControllerActivated();
				}
			}
		}
	}
	@Override
	public boolean getDebugCameraEnabled() {
		return debugCameraEnabled;
	}

	@Override
	public void setDebugCameraMotionEnabled(boolean value) {
		if (currentDcc != null) {
			currentDcc.setDebugCameraMotionEnabled(value);
		}
	}

	@Override
	public void onControllerActivated() {
		setDebugCameraEnabled(debugCameraEnabled);
		if (currentDcc != null) {
			currentDcc.onControllerActivated();
		}
	}

	@Override
	public void addSceneTarget(Scene scene) {
		for (DebugCameraController dcc : cameraControllers.values()) {
			dcc.addSceneTarget(scene);
		}
	}

	@Override
	public void activateCamera() {
		for (DebugCameraController dcc : cameraControllers.values()) {
			if (!debugCameraEnabled || dcc != currentDcc) {
				dcc.deactivateCamera();
			}
		}
		if (debugCameraEnabled && currentDcc != null) {
			currentDcc.activateCamera();
		}
	}

	@Override
	public void deactivateCamera() {
		for (DebugCameraController dcc : cameraControllers.values()) {
			dcc.deactivateCamera();
		}
	}

	@Override
	public Camera getDebugCamera() {
		if (currentDcc != null) {
			return currentDcc.getDebugCamera();
		}
		return new Camera();
	}
}
