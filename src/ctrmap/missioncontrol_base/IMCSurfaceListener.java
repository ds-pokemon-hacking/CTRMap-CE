package ctrmap.missioncontrol_base;

import javax.swing.JComponent;

public interface IMCSurfaceListener {
	public void attach(JComponent surface);
	public void detach(JComponent surface);
}
