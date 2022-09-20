package ctrmap.util.gui.cameras;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scenegraph.SceneAnimationCallback;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JComponent;

public class FPSCameraInputManager extends AbstractCameraInputManager implements MouseWheelListener, MouseMotionListener, MouseInputListener, KeyListener, FocusListener {

	private float speed = 30f;
	protected final ArrayList<Integer> keycodes = new ArrayList<>();

	public boolean isCameraEnabled = true;
	private boolean disableMotionIfCtrlAlt = false;

	private SceneAnimationCallback callback = null;

	public FPSCameraInputManager() {
		super();
		cam.name = "FPSCamera";
	}

	@Override
	public void attachComponent(JComponent parent) {
		if (callback == null) {
			callback = createAnimationCallback();
		}
		if (this.parent != null) {
			this.parent.removeFocusListener(this);
		}
		super.attachComponent(parent);
		if (parent != null) {
			parent.addFocusListener(this);
		}
	}

	@Override
	public void activate() {
		for (Scene s : presentScenes) {
			s.addSceneAnimationCallback(callback);
		}
		super.activate();
	}

	@Override
	public void deactivate() {
		for (Scene s : presentScenes) {
			s.removeSceneAnimationCallback(callback);
		}
		super.deactivate();
	}

	public ArrayList<Integer> getKcListReference() {
		return keycodes;
	}

	public void toggleEnabled() {
		isCameraEnabled = !isCameraEnabled;
	}

	public void setDisableMotionIfCtrlAlt(boolean val) {
		disableMotionIfCtrlAlt = val;
	}

	public void overrideCamera(float x, float y, float z, float rx, float ry, float rz) {
		cam.translation.x = x;
		cam.translation.y = y;
		cam.translation.z = z;
		cam.rotation.x = rx;
		cam.rotation.y = ry;
		cam.rotation.z = rz;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	protected SceneAnimationCallback createAnimationCallback() {
		return new SceneAnimationCallback() {

			@Override
			public void run(float frameAdvance) {
				if (disableMotionIfCtrlAlt) {
					if (keycodes.contains(KeyEvent.VK_CONTROL) || keycodes.contains(KeyEvent.VK_ALT)) {
						return;
					}
				}

				float gSpeed = speed * frameAdvance;

				float translateX;
				float translateY;
				float translateZ;

				if (isCameraEnabled && allowMotion) {
					//reset values
					translateX = 0f;
					translateY = 0f;
					translateZ = 0f;
					float upDownSignum = keycodes.contains(KeyEvent.VK_W) ? 1f : keycodes.contains(KeyEvent.VK_S) ? -1f : 0f;
					float leftRightSignum = keycodes.contains(KeyEvent.VK_A) ? 1f : keycodes.contains(KeyEvent.VK_D) ? -1f : 0f;
					float verticalSignum = keycodes.contains(KeyEvent.VK_E) ? 1f : keycodes.contains(KeyEvent.VK_Q) ? -1f : 0f;

					if (cam.mode == Camera.Mode.PERSPECTIVE) {
						if (upDownSignum != 0f) {
							translateX -= upDownSignum * Math.sin(Math.toRadians(cam.rotation.y)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(cam.rotation.x)))) * gSpeed;
							translateZ -= upDownSignum * Math.cos(Math.toRadians(cam.rotation.y)) * Math.min(1f, Math.tan(Math.toRadians(90 - Math.abs(cam.rotation.x)))) * gSpeed;
							translateY += upDownSignum * Math.sin(Math.toRadians(cam.rotation.x)) * gSpeed;

						}
						if (leftRightSignum != 0f) {
							translateX += leftRightSignum * Math.sin(Math.toRadians(cam.rotation.y - 90f)) * gSpeed;
							translateZ += leftRightSignum * Math.cos(Math.toRadians(cam.rotation.y - 90f)) * gSpeed;
						}
						translateY += verticalSignum * gSpeed * 0.5f; //fake slowdown since there isn't any trigonometric adjustment to the vertical motion
					} else {
						if (keycodes.contains(KeyEvent.VK_ALT)) {
							float mult = -Math.signum(cam.translation.z - cam.lookAtTarget.z);
							cam.lookAtTarget.z += mult * upDownSignum * gSpeed;
							cam.lookAtTarget.x += mult * leftRightSignum * gSpeed;
						} else {
							float mult = -Math.signum(cam.translation.z - cam.lookAtTarget.z);

							translateZ = mult * upDownSignum * gSpeed;
							translateX = mult * leftRightSignum * gSpeed;
						}
					}
					if (keycodes.contains(KeyEvent.VK_F)) {
						translateY = 0f;
					}

					//commit
					cam.translation.x += translateX;
					cam.translation.y += translateY;
					cam.translation.z += translateZ;
				}
			}
		};
	}

	public void reset() {
		keycodes.clear();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (isCameraEnabled && allowMotion) {
			if (SwingUtilities.isRightMouseButton(e)) {
				cam.translation.x -= (e.getX() - originMouseX);
				cam.translation.y += (e.getY() - originMouseY);
			} else if (SwingUtilities.isLeftMouseButton(e)) {
				if (cam.mode == Camera.Mode.PERSPECTIVE) {
					cam.rotation.y -= ((e.getX() - originMouseX) / 2f) % 360f;
					cam.rotation.x = Math.max(-90f, Math.min(90f, cam.rotation.x - (e.getY() - originMouseY) / 2f)) % 360f;
				} else if (cam.mode == Camera.Mode.LOOKAT) {
					cam.lookAtTarget.y += (e.getY() - originMouseY) * 10f;
					cam.lookAtUpVec.x -= (e.getX() - originMouseX) / 50f;
					cam.lookAtUpVec.normalize();
				}
			}
		}
		for (Scene s : presentScenes) {
			s.stopCameraAnimations();
		}

		setOrigins(e);
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (isCameraEnabled && allowMotion) {
			if (!e.isControlDown()) {
				speed = Math.max(speed - (e.getWheelRotation()) * speed * 0.1f, 0.001f);
			} else {
				cam.translation.z += e.getWheelRotation() * (cam.translation.z / 10f);
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!keycodes.contains(e.getKeyCode())) {
			keycodes.add(e.getKeyCode());
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		while (keycodes.contains(e.getKeyCode())) {
			keycodes.remove((Integer) e.getKeyCode());
		}
	}

	@Override
	public void focusLost(FocusEvent e) {
		keycodes.clear();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		keycodes.clear();
		super.mouseExited(e);
	}
}
