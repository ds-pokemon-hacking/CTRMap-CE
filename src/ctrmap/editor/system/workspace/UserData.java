package ctrmap.editor.system.workspace;

import xstandard.fs.FSFile;

public class UserData {

	public static enum UsrDirectory {
		SCRIPT_INCLUDE("scripting/include"),
		SCRIPT_WORKSPACE("scripting/workspace"),
		SCRIPT_MNG("scripting/manager"),
		SCRIPT_CACHE("scripting/cache");
		
		private final String pathFromUsrDir;

		private UsrDirectory(String pathFromUsrDir) {
			this.pathFromUsrDir = pathFromUsrDir;
		}
	}

	public static enum UsrFile {
		MC_LOG("log/MissionControl.log"),
		MC_LOG_OLD("log/MissionControl.log.old"),
		ERROR_LOG("log/StdErr.log"),
		SCRIPT_HASH_CACHE("scripting/cache/HashCache.bin");

		private final String pathFromUsrDir;

		private UsrFile(String pathFromUsrDir) {
			this.pathFromUsrDir = pathFromUsrDir;
		}
	}

	private FSFile usrDir;

	public UserData(FSFile usrDir) {
		this.usrDir = usrDir;
	}

	public FSFile getUserDataDir(UsrDirectory dir) {
		FSFile f = usrDir.getChild(dir.pathFromUsrDir);
		f.mkdirs();
		return f;
	}

	public FSFile getUserDataFile(UsrFile file) {
		FSFile f = usrDir.getChild(file.pathFromUsrDir);
		FSFile parent = f.getParent();
		if (parent != null && !parent.exists()) {
			parent.mkdirs();
		}
		return f;
	}
}
