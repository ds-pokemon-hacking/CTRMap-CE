package ctrmap.util.gui.cameras;

import ctrmap.renderer.scene.Camera;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

public class OrthoCameraInputManager extends AbstractCameraInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener {

	private float minScale = 0f;
	private float zoomSpeed = 1f;

	public OrthoCameraInputManager() {
		cam.mode = Camera.Mode.ORTHO;
	}

	public void setMinimumScale(float v) {
		minScale = v;
	}
	
	public void setZoomSpeed(float zoomSpeed) {
		this.zoomSpeed = zoomSpeed;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		setTY(Math.max(minScale, cam.translation.y + e.getWheelRotation() * (Math.max(Math.abs(cam.translation.y), 1f) / 20f * zoomSpeed)));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			if (parent != null) {
				setTX(cam.translation.x - (e.getX() - originMouseX) * Math.abs(cam.translation.y / parent.getWidth()));
				setTZ(cam.translation.z - (e.getY() - originMouseY) * Math.abs(cam.translation.y / parent.getWidth()));
			}
		}
		setOrigins(e);
	}
}
