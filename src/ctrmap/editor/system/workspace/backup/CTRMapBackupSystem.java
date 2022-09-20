package ctrmap.editor.system.workspace.backup;

import ctrmap.editor.CTRMap;
import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.gui.backuprestore.InitialBackupRestoreDialog;
import ctrmap.editor.gui.workspace.ProjectManager;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.VFS;
import xstandard.gui.DialogUtils;
import xstandard.text.FormattingUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

public class CTRMapBackupSystem {

	private final CTRMap cm;
	private CTRMapBackupCheckTimer backupTimer;

	public CTRMapBackupSystem(CTRMap cm) {
		this.cm = cm;
	}
	
	public void stopBackupTimer() {
		if (backupTimer != null) {
			backupTimer.stop();
		}
	}
	
	public void resetBackupTimer(){
		stopBackupTimer();
		backupTimer = new CTRMapBackupCheckTimer(cm);
		backupTimer.start();
	}

	public void setupBackupRestore() {
		CTRMapProject project = cm.getProject();
		if (project != null) {
			if (project.backupCfg.backupPeriod == -1) {
				InitialBackupRestoreDialog initialBkupDialog = new InitialBackupRestoreDialog(cm, true, project);
				initialBkupDialog.setVisible(true);
				project.backupCfg.backupPeriod = initialBkupDialog.getResult();
				project.saveProjectData();
				resetBackupTimer();
			}
		}
	}

	public void restoreBackup(ProjectBackupConfig.BackupInfo info) {
		CTRMapProject project = cm.getProject();
		FSFile backupRoot = project.getBackupRoot().getChild(info.path);
		if (backupRoot.exists() && backupRoot.isDirectory()) {
			ProjectManager man = new ProjectManager();

			FSFile restoreProjectFile = backupRoot.getChild(project.prjCfgFile.getName());
			if (restoreProjectFile.isFile()) {
				CTRMapProject restoreProject = new CTRMapProject(restoreProjectFile);
				restoreProject.readProjectData(man);

				if (man.isProjectLaunchSuccess()) {
					CTRMapProject oldProject = project;
					try {
						SwingUtilities.invokeAndWait((() -> {
							cm.closeProject();
						}));
					} catch (InterruptedException | InvocationTargetException ex) {
						Logger.getLogger(CTRMapBackupSystem.class.getName()).log(Level.SEVERE, null, ex);
					}

					FSFile oldProjectRoot = oldProject.prjCfgFile.getParent();

					oldProject.usrdir.delete();
					oldProject.wsfs.vfs.getOvFSRoot().delete();

					FSUtil.copy(restoreProjectFile, oldProject.prjCfgFile);
					FSUtil.copy(restoreProject.usrdir, oldProjectRoot.getChild(restoreProject.usrdir.getName()));
					FSUtil.copy(restoreProject.wsfs.vfs.getOvFSRoot(), oldProjectRoot.getChild(restoreProject.wsfs.vfs.getOvFSRoot().getName()));

					restoreProject.prjCfgFile = oldProject.prjCfgFile;
					restoreProject.readProjectData(man); //read the data again from the new location

					if (!man.isProjectLaunchSuccess()) {
						cm.setVisible(false);
						DialogUtils.showErrorMessage(null, "Catastrophical failure", "The restored backup is corrupted. CTRMap can not continue.");
						System.exit(1);
					} else {
						for (ProjectBackupConfig.BackupInfo bi : oldProject.backupCfg.backupInfos) {
							boolean canTransferBkup = true;
							for (ProjectBackupConfig.BackupInfo bi2 : restoreProject.backupCfg.backupInfos) {
								if (Objects.equals(bi.path, bi2.path)) {
									canTransferBkup = false;
									break;
								}
							}
							if (canTransferBkup) {
								restoreProject.backupCfg.backupInfos.add(bi);
							}
						}
						restoreProject.saveProjectData();

						SwingUtilities.invokeLater((() -> {
							cm.openProject(restoreProject);
						}));
					}
				} else {
					DialogUtils.showErrorMessage(cm, "Backup corrupted", "The backup project file failed to load.");
				}
			} else {
				DialogUtils.showErrorMessage(cm, "File not found", "Could not find the backup project file.");
			}
		} else {
			DialogUtils.showErrorMessage(cm, "Backup not found", "The backup path is invalid.");
		}
	}

	public void createNewAutoBackupWithDefaultName() {
		String name = FormattingUtils.getCommonFormattedDateForFileName();
		createNewBackup(name, true);
	}

	public ProjectBackupConfig.BackupInfo createNewBackup(String path, boolean automatic) {
		CTRMapProject project = cm.getProject();
		if (project != null) {
			FSFile bkupRoot = project.getBackupRoot();
			FSFile destBkupDir = bkupRoot.getChild(path);
			if (destBkupDir != null && (!destBkupDir.exists() || destBkupDir.isDirectory())) {
				destBkupDir.mkdirs();
				ProjectBackupConfig.BackupInfo bi = project.backupCfg.initNewBackupInfo(path);
				bi.automatic = automatic;

				project.saveProjectData();

				FSFile targetVFS = destBkupDir.getChild(project.wsfs.vfs.getOvFSRoot().getName());
				targetVFS.mkdirs();
				FSFile targetUser = destBkupDir.getChild(project.usrdir.getName());
				targetUser.mkdir();

				FSUtil.copy(project.usrdir, targetUser);
				copyDirectory(project.wsfs.vfs.getOvFSRoot(), targetVFS, project.wsfs.vfs);
				FSUtil.copy(project.prjCfgFile, destBkupDir.getChild(project.prjCfgFile.getName()));

				for (FSFile userFile : targetUser.listFiles()) {
					if (userFile.getName().endsWith(".tmp")) {
						userFile.delete();
					}
				}
				return bi;
			}
		}
		return null;
	}

	public static void copyDirectory(FSFile source, FSFile target, VFS fs) {
		if (source.isDirectory() && (!target.exists() || target.isDirectory())) {
			target.mkdirs();
			copyChildren(source, target, fs);
		}
	}

	private static void copyChildren(FSFile parent1, FSFile parent2, VFS fs) {
		List<? extends FSFile> files = parent1.listFiles();
		for (FSFile f : files) {
			FSFile f2 = parent2.getChild(f.getName());
			if (f.isDirectory()) {
				f2.mkdirs();
				copyChildren(f, f2, fs);
			} else {
				if (!fs.isFileChangeBlacklisted(fs.getWildCardManager().getWildCardedPath(f.getPathRelativeTo(fs.getOvFSRoot())))) {
					FSUtil.copy(f, f2);
				}
			}
		}
	}
}
