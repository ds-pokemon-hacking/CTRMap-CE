package ctrmap.editor.gui.editors.common.input;

import ctrmap.util.gui.cameras.FPSCameraInputManager;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;
import ctrmap.missioncontrol_base.DebugCameraController;
import ctrmap.missioncontrol_base.InputManager;
import ctrmap.missioncontrol_base.IMCSurfaceListener;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import java.awt.event.MouseEvent;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import javax.swing.JComponent;
import javax.swing.event.MouseInputListener;

/**
 * Listens to user input interfaces and forwards any interaction to CM3D.
 */
public class CM3DInputManager implements IMCSurfaceListener, DebugCameraController, MouseInputListener, KeyListener {

	private AbstractPerspective editors;

	public FPSCameraInputManager fpsCamera = new FPSCameraInputManager();
	private ArrayList<Integer> keycodes = fpsCamera.getKcListReference();

	private boolean walkThroughWalls = false;
	private boolean isToolInputEnabled = true;

	public CM3DInputManager(AbstractPerspective edt) {
		super();
		this.editors = edt;
	}

	private JComponent parent;

	public void setIsToolInputEnabled(boolean val) {
		isToolInputEnabled = val;
		if (editors.tool != null) {
			editors.tool.onTileMouseUp(new MouseEvent(parent, 0, 0, 0, 0, 0, 0, false));
		}
	}

	@Override
	public void attach(JComponent backend) {
		parent = backend;
		fpsCamera.attachComponent(backend);
		parent.addMouseListener(this);
		parent.addMouseMotionListener(this);
		parent.addKeyListener(this);
	}

	@Override
	public void detach(JComponent comp) {
		parent = null;
		fpsCamera.attachComponent(null);
		comp.removeMouseListener(this);
		comp.removeMouseMotionListener(this);
		comp.removeKeyListener(this);
	}
	
	public void overrideCamera(float x, float y, float z, float rx, float ry, float rz) {
		fpsCamera.overrideCamera(x, y, z, rx, ry, rz);
	}

	public void reset() {
		keycodes.clear();
	}

	private double roundIfClose(double d) {
		if (Math.abs(Math.round(d) - d) < 0.001) {
			return Math.round(d);
		} else {
			return d;
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (keycodes.isEmpty()) {
			if (editors.tool != null && isToolInputEnabled) {
				editors.tool.onTileMouseDown(e);
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (editors.tool != null && isToolInputEnabled) {
			editors.tool.onTileMouseUp(e);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (keycodes.isEmpty()) {
			if (editors.tool != null && isToolInputEnabled) {
				editors.tool.onTileMouseDragged(e);
			}
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if (e.getButton() == MouseEvent.NOBUTTON) {
			if (editors.tool != null && isToolInputEnabled) {
				editors.tool.onTileMouseMoved(e);
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setAxesAndButtons();
		switch (e.getKeyCode()) {
			case KeyEvent.VK_SHIFT:
				walkThroughWalls = true;
				break;
			case KeyEvent.VK_ESCAPE:
				if (editors.tool != null && isToolInputEnabled){
					editors.tool.fireCancel();
				}
				break;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setAxesAndButtons();
		if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
			walkThroughWalls = false;
		}
	}

	public void clearInput(){
		keycodes.clear();
		setAxesAndButtons();
	}
	
	public void setAxesAndButtons() {
		InputManager input = editors.getCTRMap().mcInUse.input;
		float ax = 0;
		float ay = 0;
		if (keycodes.contains(KeyEvent.VK_W)) {
			ay++;
		}
		if (keycodes.contains(KeyEvent.VK_S)) {
			ay--;
		}
		if (keycodes.contains(KeyEvent.VK_A)) {
			ax--;
		}
		if (keycodes.contains(KeyEvent.VK_D)) {
			ax++;
		}
		if (keycodes.contains(KeyEvent.VK_C)){
			input.pressButton(InputManager.Button.B);
		}
		else {
			input.releaseButton(InputManager.Button.B);
		}
		if (keycodes.contains(KeyEvent.VK_CONTROL)){
			input.pressButton(InputManager.Button.LB);
		}
		else {
			input.releaseButton(InputManager.Button.LB);
		}
		if (keycodes.contains(KeyEvent.VK_SHIFT)){
			input.pressButton(InputManager.Button.DEBUG0);
		}
		else {
			input.releaseButton(InputManager.Button.DEBUG0);
		}
		if (keycodes.contains(KeyEvent.VK_X)){
			input.pressButton(InputManager.Button.A);
		}
		else {
			input.releaseButton(InputManager.Button.A);
		}
		input.setAxis(InputManager.Axis.LS_X, ax);
		input.setAxis(InputManager.Axis.LS_Y, ay);
	}

	@Override
	public boolean getDebugCameraEnabled() {
		return fpsCamera.isCameraEnabled;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void setDebugCameraMotionEnabled(boolean value) {
		fpsCamera.setAllowMotion(value);
	}

	@Override
	public void onControllerActivated() {
		for (AbstractTool tool : editors.getTools()) {
			tool.onViewportSwitch(false);
		}
	}
	
	@Override
	public void addSceneTarget(Scene scene) {
		fpsCamera.addSceneTarget(scene);
	}

	@Override
	public void activateCamera() {
		fpsCamera.activate();
	}

	@Override
	public void deactivateCamera() {
		fpsCamera.deactivate();
	}

	@Override
	public Camera getDebugCamera() {
		return fpsCamera.cam;
	}
}
