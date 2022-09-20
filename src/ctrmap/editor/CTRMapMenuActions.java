package ctrmap.editor;

import ctrmap.editor.gui.backuprestore.BackupRestoreForm;
import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.editor.gui.AboutDialog;
import ctrmap.creativestudio.CreativeStudioChecker;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.editor.gui.ControlsHelpDialog;
import ctrmap.editor.gui.settings.SettingsForm;
import ctrmap.missioncontrol_base.IMissionControl;
import xstandard.fs.FSFile;
import xstandard.gui.DialogUtils;
import xstandard.gui.file.XFileDialog;
import javax.swing.KeyStroke;

public class CTRMapMenuActions {

	public static void initMenuActions(CTRMap ctrmap) {
		CTRMapUIManager uiMgr = ctrmap.getUIManager();

		String menuName_File = "File";
		String menuName_Tools = "Tools";
		String menuName_Options = "Options";
		String menuName_Help = "Help";

		//Save
		uiMgr.addMenuItem(menuName_File, "Save", (cm) -> {
			cm.saveData();
		}).setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));

		//Pack Workspace
		uiMgr.addMenuItem(menuName_File, "Pack Workspace", (cm) -> {
			if (cm.saveData()) {
				cm.applyOvFS();
			}
		}).setAccelerator(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_DOWN_MASK));

		//Serialize Scene
		uiMgr.addMenuItem(menuName_File, "Serialize Scene", (cm) -> {
			IMissionControl mc = cm.getMissionControl();

			if (mc != null && mc.mcScene != null) {
				FSFile f = XFileDialog.openSaveFileDialog(CMIFFile.EXTENSION_FILTER);
				if (f != null) {
					new CMIFFile(mc.mcScene).write(f);
				}
			}
		});

		//CreativeStudio
		uiMgr.addMenuItem(menuName_Tools, "CreativeStudio", (cm) -> {
			if (CreativeStudioChecker.isCreativeStudioPresent()) {
				new NGCS().setVisible(true);
			} else {
				DialogUtils.showErrorMessage(cm, "Component not present", "CreativeStudio is not available in this build configuration.");
			}
		});

		//Backup & Restore
		uiMgr.addMenuItem(menuName_Tools, "Backup & Restore", (cm) -> {
			new BackupRestoreForm(cm).setVisible(true);
		});

		//Settings
		uiMgr.addMenuItem(menuName_Options, "Settings", (cm) -> {
			if (!SettingsForm.GLOBAL_SETTINGS_PANEL_LOCK) {
				new SettingsForm(cm).setVisible(true);
			}
		});

		//Issue tracker
		/*ctrmap.Help_IssueTracker.addActionListener((ActionEvent e) -> {
			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				try {
					Desktop.getDesktop().browse(new URI("https://github.com/HelloOO7/CTRMap/issues"));
				} catch (URISyntaxException | IOException ex) {
					Logger.getLogger(CTRMapMenuActions.class.getName()).log(Level.SEVERE, null, ex);
				}
			} else {
				DialogUtils.showErrorMessage("Browser open error", "Your system either does not support the Java Desktop API or you do not have a suitable browser installed.");
			}
		});*/
		uiMgr.addMenuItem(menuName_Help, "Controls", (cm) -> {
			ControlsHelpDialog dlg = new ControlsHelpDialog();
			dlg.setLocationRelativeTo(cm);
			dlg.setVisible(true);
		});

		//About dialog
		uiMgr.addMenuItem(menuName_Help, "About", (cm) -> {
			AboutDialog dlg = new AboutDialog(cm, true);
			dlg.setCredits(cm.getEditorManager().getCredits());
			dlg.setSpecialThanks(cm.getEditorManager().getSpecialThanks());
			dlg.setLocationRelativeTo(cm);
			dlg.setVisible(true);

			cm.broadcastGlobalEvent(AboutDialog.GEVENT_OPEN_ID);
		});
	}
}
