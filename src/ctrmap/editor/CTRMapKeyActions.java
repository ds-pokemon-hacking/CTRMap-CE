package ctrmap.editor;

import ctrmap.editor.gui.editors.common.input.DCCManager;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class CTRMapKeyActions {

	private static final String ACMD_2D_SWITCH = "switch2D";
	private static final String ACMD_3D_SWITCH = "switch3D";
	private static final String ACMD_DEBUG_TOGGLE = "toggleDebug";

	public static void initActionMap(CTRMap cm) {
		InputMap inputMap = cm.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap actionMap = cm.getRootPane().getActionMap();

		inputMap.put(KeyStroke.getKeyStroke("F2"), ACMD_2D_SWITCH);
		actionMap.put(ACMD_2D_SWITCH, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cm.currentPerspective != null) {
					cm.currentPerspective.dcc.setDebugCamera(DCCManager.CAMERA_CONTROLLER_ID_2D);
					cm.currentPerspective.onDCCCameraChanged();
				}
			}
		});

		inputMap.put(KeyStroke.getKeyStroke("F3"), ACMD_3D_SWITCH);
		actionMap.put(ACMD_3D_SWITCH, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cm.currentPerspective != null) {
					cm.currentPerspective.dcc.setDebugCamera(DCCManager.CAMERA_CONTROLLER_ID_3D);
					cm.currentPerspective.onDCCCameraChanged();
				}
			}
		});
		inputMap.put(KeyStroke.getKeyStroke("F5"), ACMD_DEBUG_TOGGLE);
		actionMap.put(ACMD_DEBUG_TOGGLE, new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (cm.currentPerspective != null) {
					cm.currentPerspective.dcc.toggleDebugCameraEnabled();
					cm.currentPerspective.onDCCCameraChanged();
				}
			}
		});
	}
}
