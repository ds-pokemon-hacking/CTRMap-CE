package ctrmap.editor.system.workspace.backup;

import ctrmap.CTRMapResources;
import ctrmap.editor.CTRMap;
import ctrmap.editor.system.workspace.CTRMapProject;
import xstandard.gui.DialogUtils;
import xstandard.res.ResourceAccess;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.Timer;

public class CTRMapBackupCheckTimer extends Timer implements ActionListener {

	private CTRMap cm;
	
	public CTRMapBackupCheckTimer(CTRMap ctrmap) {
		super(60 * 1000, null);
		addActionListener(this);
		this.cm = ctrmap;
		setRepeats(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final CTRMapProject proj = cm.getProject();
		if (proj != null) {
			if (proj.backupCfg.backupPeriod <= 0) {
				return;
			}
			if ((proj.getPlaytime() - proj.backupCfg.lastBackupTimestamp) < proj.backupCfg.backupPeriod) {
				return;
			}

			performAutoBackup();
		}
	}

	public void performAutoBackup() {
		final CTRMapProject proj = cm.getProject();

		proj.backupCfg.setNowLastBackupTimestamp(proj.getPlaytime()); //if some error occured, don't loop the backup

		final TrayIcon trayIconf;
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			Image image = Toolkit.getDefaultToolkit().createImage(CTRMapResources.ACCESSOR.getByteArray("backup/backup_tray.png"));
			TrayIcon trayIcon = new TrayIcon(image, "CTRMap Automatic Backup");
			trayIcon.setImageAutoSize(true);
			try {
				tray.add(trayIcon);
				trayIcon.displayMessage("CTRMap Automatic Backup", "Performing automatic backup. Please do not exit the program.", TrayIcon.MessageType.INFO);
			} catch (AWTException ex) {
				Logger.getLogger(CTRMapBackupCheckTimer.class.getName()).log(Level.SEVERE, null, ex);
			}
			trayIconf = trayIcon;
		} else {
			DialogUtils.showInfoMessage(cm, "Automatic backup", "CTRMap will now perform a scheduled backup. Please do not exit the program.\n\n(This pop-up was invoked because your OS does not support the system tray API)");
			trayIconf = null;
		}

		SwingWorker worker = new SwingWorker() {
			@Override
			protected Object doInBackground() throws Exception {
				cm.getBackupSystem().createNewAutoBackupWithDefaultName();
				return null;
			}

			@Override
			public void done() {
				if (trayIconf != null) {
					try {
						get();
						trayIconf.displayMessage("CTRMap Automatic Backup", "Backup finished.", TrayIcon.MessageType.INFO);
					} catch (Exception ex) {
						ex.printStackTrace();
						trayIconf.displayMessage("CTRMap Automatic Backup", "Backup failed.", TrayIcon.MessageType.ERROR);
					}
					
					Timer iconHideTimer = new Timer(5000, new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							SystemTray.getSystemTray().remove(trayIconf);
						}
					});
					iconHideTimer.start();
				}
			}
		};
		worker.execute();
	}
}
