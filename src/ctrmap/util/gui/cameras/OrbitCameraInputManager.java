package ctrmap.util.gui.cameras;

import ctrmap.renderer.scene.Camera;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

import javax.swing.SwingUtilities;

import javax.swing.JComponent;

/**
 * Orbits the camera around an object with user input.
 */
public class OrbitCameraInputManager extends AbstractCameraInputManager {

	private Camera cam0 = new Camera();

	public Camera getTranslationCamera() {
		return cam;
	}

	public Camera getRotationCamera() {
		return cam0;
	}

	public OrbitCameraInputManager(JComponent parent) {
		attachComponent(parent);
	}

	@Override
	public void activate() {
		updateCameraInAllScenes(cam0, true);
		super.activate();
	}
	
	@Override
	public void deactivate(){
		updateCameraInAllScenes(cam0, false);
		super.deactivate();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			setTX(cam.translation.x - (e.getX() - originMouseX) * Math.abs(cam.translation.z / 500f));
			setTY(cam.translation.y + (e.getY() - originMouseY) * Math.abs(cam.translation.z / 500f));
		} else if (SwingUtilities.isLeftMouseButton(e)) {
			setYaw(cam0.rotation.y - (e.getX() - originMouseX) / 2f);
			setPitch(cam0.rotation.x - (e.getY() - originMouseY) / 2f);
		}
		setOrigins(e);
	}

	@Override
	public void resetCamera() {
		super.resetCamera();
		cam0.rotation.x = 0;
		cam0.rotation.y = 0;
	}

	@Override
	public void setPitch(float v) {
		if (allowMotion) {
			cam0.rotation.x = v;
		}
	}

	@Override
	public void setYaw(float v) {
		if (allowMotion) {
			cam0.rotation.y = v;
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		setTZ(cam.translation.z + e.getWheelRotation() * (Math.max(Math.abs(cam.translation.z), 1f) / 20f));
	}
}
