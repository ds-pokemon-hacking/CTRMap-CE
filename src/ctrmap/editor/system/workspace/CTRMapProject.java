package ctrmap.editor.system.workspace;

import ctrmap.CTRMapResources;
import ctrmap.editor.system.workspace.backup.ProjectBackupConfig;
import xstandard.formats.msgtxt.MsgTxt;
import ctrmap.formats.common.GameInfo;
import xstandard.fs.VFSRootFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import ctrmap.editor.gui.workspace.ProjectManager;
import ctrmap.editor.system.workspace.wildcards.FSWildCardManagerCTR;
import ctrmap.editor.system.workspace.wildcards.FSWildCardManagerNTR;
import ctrmap.missioncontrol_base.McLogger;
import xstandard.formats.yaml.Yaml;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.io.base.impl.access.MemoryStream;
import xstandard.thread.ThreadingUtils;
import java.awt.event.ActionEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

public class CTRMapProject {

	public static final ExtensionFilter EXT_FILTER = new ExtensionFilter("CTRMap Project", "*.cmproj");

	private static final MsgTxt errorResource = new MsgTxt(CTRMapResources.ACCESSOR.getStream("message/project_loader_errors.msgtxt"));

	private static final String PRJ_USRDIR_KEY = "UserDataPath";
	private static final String VFS_OVFS_KEY = "VFSOverlay";
	private static final String VFS_BASEFS_KEY = "VFSBase";

	private static final String PRJ_BACKUP_KEY = "BackupAndRestore";
	private static final String PRJ_PLAYTIME_KEY = "Playtime";

	public WSFS wsfs;
	public UserData userData;
	public FSFile usrdir;
	public FSFile backupRoot;

	public GameInfo.DefaultGameManager gameInfo;

	public FSFile prjCfgFile;
	private Yaml prjCfg;

	public ProjectBackupConfig backupCfg;

	public long openTimestamp;

	public CTRMapProject(FSFile f) {
		prjCfgFile = f;

		prjCfg = new Yaml();

		openTimestamp = System.currentTimeMillis();

		backupCfg = new ProjectBackupConfig(prjCfg.getEnsureRootNodeKeyNode(PRJ_BACKUP_KEY));
	}

	public CTRMapProject(String prjFilePath) {
		this(new DiskFile(prjFilePath));
	}

	public CTRMapProject(String prjFilePath, ProjectManager man) {
		this(prjFilePath);
		readProjectData(man);
	}
	
	public void free() {
		saveProjectData();
		wsfs.free();
	}

	public FSFile getBackupRoot() {
		return backupRoot;
	}

	public String getProjectName() {
		return FSUtil.getFileNameWithoutExtension(prjCfgFile.getName());
	}

	public final void readProjectData(ProjectManager man) {
		if (!prjCfgFile.exists()) {
			man.raiseLoadError(errorResource.getLineForName("no_file"));
			return;
		}

		FSFile prjCfgParent = prjCfgFile.getParent();

		prjCfg = new Yaml(prjCfgFile);

		backupCfg = new ProjectBackupConfig(prjCfg.getEnsureRootNodeKeyNode(PRJ_BACKUP_KEY));

		openTimestamp = System.currentTimeMillis();

		if (!prjCfg.root.hasChildren(PRJ_USRDIR_KEY, VFS_BASEFS_KEY, VFS_OVFS_KEY)) {
			man.raiseLoadError(errorResource.getLineForName("bad_file"));
			return;
		}

		usrdir = new DiskFile(getAttribute(PRJ_USRDIR_KEY));
		if (!usrdir.exists()) {
			usrdir = prjCfgParent.getChild(getAttribute(PRJ_USRDIR_KEY));
		}
		backupRoot = prjCfgParent.getChild("backup");

		FSFile vfsOv = prjCfgParent.getChild(getAttribute(VFS_OVFS_KEY));
		FSFile vfsBase = new DiskFile(getAttribute(VFS_BASEFS_KEY));
		if (!vfsBase.exists()) {
			vfsBase = prjCfgParent.getChild(getAttribute(VFS_BASEFS_KEY));
		}

		wsfs = new WSFS(
			new VFSRootFile(vfsOv),
			new VFSRootFile(vfsBase),
			usrdir
		);

		if (!usrdir.exists()) {
			man.raiseLoadError(errorResource.getLineForName("no_usrdir"));
			return;
		}

		if (!wsfs.vfs.getBaseFSRoot().exists()) {
			man.raiseLoadError(errorResource.getLineForName("no_basefs"));
			return;
		}

		if (!wsfs.vfs.getOvFSRoot().exists()) {
			man.raiseLoadError(errorResource.getLineForName("no_ovfs"));
			return;
		}

		GameInfo.SubGame subGame = GameDetector.detectSubGame(wsfs.vfs.getBaseFSRoot());
		GameInfo.Game game = GameDetector.detectGameType(subGame);
		if (game == null || subGame == null) {
			List<String> details = new ArrayList<>();

			boolean isCtr = !GameDetector.isDSGame(wsfs.vfs.getBaseFSRoot());

			if (isCtr) {
				wsfs.setWildCardManager(FSWildCardManagerCTR.INSTANCE);
				if (!FSFile.exists(wsfs.getBaseFsFile(":romfs:"))) {
					details.add(errorResource.getLineForName("no_romfs"));
				}
				if (!FSFile.exists(wsfs.getBaseFsFile(":exefs:"))) {
					details.add(errorResource.getLineForName("no_exefs"));
				} else {
					if (!FSFile.exists(wsfs.getBaseFsFile(":exefs:/:codebin:"))) {
						details.add(errorResource.getLineForName("no_codebin"));
					}
				}
				if (!FSFile.exists(wsfs.getBaseFsFile(":exheader:"))) {
					details.add(errorResource.getLineForName("no_exheader"));
				}
			}

			StringBuilder errors = new StringBuilder(errorResource.getLineForName("bad_game"));
			for (String err : details) {
				errors.append("\n");
				errors.append(err);
			}
			if (game != null) {
				errors.append("Detected game: ").append(game);
			}

			man.raiseLoadError(errors.toString());
			return;
		}
		userData = new UserData(usrdir);
		gameInfo = new GameInfo.DefaultGameManager(game, subGame);
		if (gameInfo.isGenV()) {
			wsfs.setWildCardManager(FSWildCardManagerNTR.INSTANCE);
		} else {
			wsfs.setWildCardManager(FSWildCardManagerCTR.INSTANCE);
		}
	}

	public final String getAttribute(String key) {
		return prjCfg.getRootNodeKeyValue(key);
	}

	public final boolean getFlag(String key) {
		String att = getAttribute(key);
		if (att != null) {
			return Boolean.parseBoolean(att);
		}
		return false;
	}

	public final void setFlag(String key, boolean value) {
		setAttribute(key, String.valueOf(value));
		saveProjectData();
	}

	public final void setAttribute(String key, String value) {
		prjCfg.getEnsureRootNodeKeyNode(key).setValue(value);
	}

	public final long getNumberL(String key, long defaultVal) {
		if (prjCfg.root.hasChildren(key)) {
			return prjCfg.root.getChildLongValue(key);
		}
		return defaultVal;
	}

	public final int getNumberI(String key, int defaultVal) {
		if (prjCfg.root.hasChildren(key)) {
			return prjCfg.root.getChildIntValue(key);
		}
		return defaultVal;
	}

	public final void setNumber(String key, long value) {
		prjCfg.getEnsureRootNodeKeyNode(key).setValueLong(value);
	}

	public final long getPlaytime() {
		return (System.currentTimeMillis() - openTimestamp) + getNumberL(PRJ_PLAYTIME_KEY, 0);
	}

	public final void saveProjectData() {
		long saveTime = System.currentTimeMillis();
		long playTime = getPlaytime();
		setNumber(PRJ_PLAYTIME_KEY, playTime);
		openTimestamp = saveTime;
		backupCfg.saveToNode();
		prjCfg.writeToFile(prjCfgFile);
	}

	public static FSFile initProject(FSFile dest, String gamePath) {
		FSFile usrDir = dest.getChild("user");
		FSFile vfsDir = dest.getChild("vfs");
		usrDir.mkdir();
		vfsDir.mkdir();

		FSFile prjFile = dest.getChild(dest.getName() + EXT_FILTER.getExtensions().get(0));
		CTRMapProject proj = new CTRMapProject(prjFile);

		proj.setAttribute(PRJ_USRDIR_KEY, usrDir.getPathRelativeTo(dest));
		proj.setAttribute(VFS_BASEFS_KEY, gamePath);
		proj.setAttribute(VFS_OVFS_KEY, vfsDir.getPathRelativeTo(dest));

		proj.saveProjectData();
		return prjFile;
	}

	public static class ProjectLogger extends McLogger {

		private MemoryStream tempStream = new MemoryStream();

		private PrintStream out;

		private FSFile logFile;

		public ProjectLogger(CTRMapProject prj) {
			logFile = prj.userData.getUserDataFile(UserData.UsrFile.MC_LOG);
			if (logFile.exists()) {
				FSUtil.move(logFile, prj.userData.getUserDataFile(UserData.UsrFile.MC_LOG_OLD));
			}
			out = new PrintStream(tempStream.getOutputStream());
			Runtime.getRuntime().addShutdownHook(new Thread((() -> {
				out.close();
				flushLog();
			})));
			Timer timer = new Timer(1000, (ActionEvent e) -> {
				ThreadingUtils.runOnNewThread((() -> {
					flushLog();
				}));
			});
			timer.setRepeats(true);
			timer.start();
		}

		private void flushLog() {
			if (logFile.canWrite()) {
				logFile.setBytes(tempStream.toByteArray());
			}
		}

		@Override
		protected void print(String s) {
			if (out != null) {
				System.out.print(s);
				out.print(s);
			}
		}

	}
}
