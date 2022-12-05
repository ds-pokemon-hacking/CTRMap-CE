package ctrmap.util.gui.cameras;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

public class OrthoCameraInputManager extends AbstractCameraInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener {

	private float minScale = 0f;
	private float zoomSpeed = 1f;

	private float centerX = 0f;
	private float centerZ = 0f;
	private float zoom = 0f;

	public OrthoCameraInputManager() {
		cam.setDefaultOrtho();
	}

	public void setMinimumScale(float v) {
		minScale = v;
	}

	public void setZoomSpeed(float zoomSpeed) {
		this.zoomSpeed = zoomSpeed;
	}

	public void setZoom(float zoom) {
		if (allowMotion) {
			this.zoom = zoom;
			updateCam();
		}
	}

	public void setCenter(float cx, float cz) {
		if (allowMotion) {
			centerX = cx;
			centerZ = cz;
			updateCam();
		}
	}

	private void updateCam() {
		if (parent != null) {
			cam.makeZoomOrtho(centerX, centerZ, zoom, parent.getWidth() / (float) parent.getHeight());
		}
	}

	@Override
	protected void onActivated() {
		updateCam();
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		setZoom(Math.max(minScale, zoom + e.getWheelRotation() * (Math.max(Math.abs(zoom), 1f) / 20f * zoomSpeed)));
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (SwingUtilities.isRightMouseButton(e)) {
			if (parent != null && allowMotion) {
				setCenter(
					centerX - (e.getX() - originMouseX) * Math.abs(zoom / parent.getWidth()),
					centerZ - (e.getY() - originMouseY) * Math.abs(zoom / parent.getWidth())
				);
			}
		}
		setOrigins(e);
	}
}
