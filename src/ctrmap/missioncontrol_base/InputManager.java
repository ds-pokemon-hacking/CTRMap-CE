package ctrmap.missioncontrol_base;

import xstandard.math.FAtan;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;

public final class InputManager {

	private ButtonSnapshot buttons = new ButtonSnapshot();
	private AxisSnapshot[] axes;
	
	private ButtonQueue toPress = new ButtonQueue();
	private ButtonQueue toRelease = new ButtonQueue();
	private float[] axisQueue;
		
	protected InputManager() {
		Axis[] vals = Axis.values();
		
		axisQueue = new float[vals.length];
		axes = new AxisSnapshot[vals.length];
		
		for (Axis a : vals) {
			axes[a.ordinal()] = new AxisSnapshot();
			axisQueue[a.ordinal()] = 0f;
		}
	}

	public boolean isButtonDown(Button btn) {
		return buttons.currentButtons.contains(btn);
	}
	
	public boolean isButtonNewPressed(Button btn) {
		return buttons.newlyPressedButtons.contains(btn);
	}
	
	public boolean isButtonNewReleased(Button btn) {
		return buttons.justReleasedButtons.contains(btn);
	}
	
	public boolean isStickActive(Joystick stick) {
		for (Axis a : stick.axes()) {
			if (getAxisState(a).isActive()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isStickSignumChange(Joystick stick) {
		for (Axis a : stick.axes()) {
			if (getAxisState(a).isSignumChange()) {
				return true;
			}
		}
		return false;
	}
	
	public AxisSnapshot getAxisState(Axis axis) {
		return axes[axis.ordinal()];
	}

	public void pressButton(Button btn) {
		ArraysEx.addIfNotNullOrContains(toPress, btn);
	}

	public void releaseButton(Button btn) {
		ArraysEx.addIfNotNullOrContains(toRelease, btn);
	}
	
	public void setAxis(Axis a, float value) {
		axisQueue[a.ordinal()] = value;
	}
	
	public float getStickAngle(Joystick stick) {
		return FAtan.atan2(getAxisState(stick.Y).value, getAxisState(stick.X).value);
	}
	
	public void updateInput() {
		toPress.removeAll(toRelease);
		buttons.justReleasedButtons.clear();
		buttons.newlyPressedButtons.clear();
		for (Button b : toPress) {
			if (!buttons.currentButtons.contains(b)) {
				buttons.currentButtons.add(b);
				buttons.newlyPressedButtons.add(b);
			}
		}
		for (Button b : toRelease) {
			if (buttons.currentButtons.contains(b)) {
				buttons.currentButtons.remove(b);
				buttons.justReleasedButtons.add(b);
			}
		}
		for (Axis a : Axis.values()) {
			AxisSnapshot ss = getAxisState(a);
			ss.lastValue = ss.value;
			ss.value = axisQueue[a.ordinal()];
		}
		toPress.clear();
		toRelease.clear();
	}
	
	public enum Joystick {
		LEFT(Axis.LS_X, Axis.LS_Y),
		RIGHT(Axis.RS_X, Axis.RS_Y)
		;
		
		private final Axis X;
		private final Axis Y;
		
		private Joystick(Axis x, Axis y) {
			this.X = x;
			this.Y = y;
		}
		
		public Axis[] axes() {
			return new Axis[]{X, Y};
		}
	}
	
	public enum Axis {
		LS_X,
		LS_Y,
		RS_X,
		RS_Y
	}

	public enum Button {
		A,
		B,
		X,
		Y,
		DPAD_UP,
		DPAD_DOWN,
		DPAD_LEFT,
		DPAD_RIGHT,
		LB,
		RB,
		START,
		SELECT
	}

	private static class ButtonQueue extends ArrayList<Button> {

		@Override
		public boolean add(Button b) {
			if (!contains(b)) {
				return super.add(b);
			}
			return false;
		}
	}

	private static class ButtonSnapshot {

		public List<Button> newlyPressedButtons = new ArrayList<>();
		public List<Button> currentButtons = new ArrayList<>();
		public List<Button> justReleasedButtons = new ArrayList<>();
	}
	
	public static class AxisSnapshot {
		public float lastValue;
		public float value;
		
		public boolean isActive() {
			return value != 0f;
		}
		
		public boolean isSignumChange() {
			return Math.signum(lastValue) != Math.signum(value);
		}
	}

	public interface McInput {

		public void init();

		public void update();
	}
}
