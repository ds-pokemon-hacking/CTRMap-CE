package ctrmap.editor.gui.settings;

import xstandard.formats.msgtxt.MsgTxt;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;

public interface SettingsPanel {

	public void attachParent(SettingsForm form);
	public MsgTxt getMsgTxt();
	public SettingsForm getParentForm();
	public default void save() {
		
	}

	public static void batchAddMouseOverListener(SettingsPanel panel, String descTarget, JComponent... comps) {
		MouseAdapter a = new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				panel.getParentForm().showHint(panel, descTarget);
			}
		};
		for (JComponent c : comps) {
			c.addMouseListener(a);
		}
	}
}
