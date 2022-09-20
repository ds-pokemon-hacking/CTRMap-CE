package ctrmap.util.gui.cameras;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import xstandard.gui.components.listeners.IKeyAdapter;
import xstandard.gui.components.listeners.IMouseAdapter;
import xstandard.gui.components.listeners.IMouseMotionAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;

/**
 *
 */
public class AbstractCameraInputManager implements IMouseAdapter, IMouseMotionAdapter, MouseWheelListener, IKeyAdapter, FocusListener {

	public Camera cam = new Camera();

	protected int originMouseX;
	protected int originMouseY;

	protected JComponent parent;

	protected boolean allowMotion = true;

	protected List<Scene> presentScenes = new ArrayList<>();

	public void setOrigins(MouseEvent e) {
		originMouseX = e.getX();
		originMouseY = e.getY();
	}

	public void attachComponent(JComponent parent) {
		if (this.parent != null) {
			this.parent.removeMouseWheelListener(this);
			this.parent.removeMouseMotionListener(this);
			this.parent.removeKeyListener(this);
			this.parent.removeMouseListener(this);
			this.parent.removeFocusListener(this);
		}
		this.parent = parent;
		if (parent != null) {
			parent.addMouseWheelListener(this);
			parent.addMouseMotionListener(this);
			parent.addKeyListener(this);
			parent.addMouseListener(this);
			parent.addFocusListener(this);
		}
	}

	public void resetCamera() {
		cam.translation.zero();
		cam.rotation.zero();
	}

	public void setAllowMotion(boolean v) {
		allowMotion = v;
		if (deactivated) {
			allowMotionChangedUserInactive = true;
		}
	}

	public boolean getAllowMotion() {
		return allowMotion;
	}

	public void addSceneTarget(Scene scn) {
		if (!presentScenes.contains(scn)) {
			presentScenes.add(scn);
		}
	}

	public void removeSceneTarget(Scene scn) {
		presentScenes.remove(scn);
	}

	public void addToScene(Scene scn) {
		addSceneTarget(scn);
		activate();
	}

	private boolean deactivated = true;
	private boolean allowMotionChangedUserInactive = false;
	private boolean allowMotionBeforeDeactivation = true;

	public void activate() {
		deactivated = false;
		if (!allowMotionChangedUserInactive) {
			allowMotion = allowMotionBeforeDeactivation;
		}
		updateCameraInAllScenes(cam, true);
	}

	public void deactivate() {
		deactivated = true;
		updateCameraInAllScenes(cam, false);
		allowMotionBeforeDeactivation = allowMotion;
		allowMotionChangedUserInactive = false;
		allowMotion = false;
	}

	protected void updateCameraInAllScenes(Camera cam, boolean shouldExist) {
		for (Scene scn : presentScenes) {
			removeAddCameraBoolean(cam, scn, shouldExist);
		}
	}

	protected static void removeAddCameraBoolean(Camera cam, Scene scn, boolean shouldExist) {
		if (shouldExist) {
			scn.instantiateCamera(cam);
		} else {
			scn.deinstantiateCamera(cam);
		}
	}

	public void setPitch(float v) {
		if (allowMotion) {
			cam.rotation.x = v;
		}
	}

	public void setYaw(float v) {
		if (allowMotion) {
			cam.rotation.y = v;
		}
	}

	public void setTX(float v) {
		if (allowMotion) {
			cam.translation.x = v;
		}
	}

	public void setTY(float v) {
		if (allowMotion) {
			cam.translation.y = v;
		}
	}

	public void setTZ(float v) {
		if (allowMotion) {
			cam.translation.z = v;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		requestParentFocus();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		setOrigins(e);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		requestParentFocus();
	}

	protected void requestParentFocus() {
		if (!parent.hasFocus()) {
			parent.requestFocus();
		}
	}

	@Override
	public void focusGained(FocusEvent e) {

	}

	@Override
	public void focusLost(FocusEvent e) {

	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
	}
}
