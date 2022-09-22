
import ctrmap.Launc;

import ctrmap.creativestudio.NGCSStarter;
import ctrmap.editor.CTRMap;
import ctrmap.util.tools.GlobalExtensionManager;

public class LauncherPlugin implements Launc.IPlugin {
	
	@Override
	public void registSubprocesses(Launc.JulietInterface j) {
		j.registerSubprocess("CTRMap", CTRMap.STARTER);
		j.registerSubprocess("CreativeStudio", NGCSStarter.INSTANCE);
		j.registerSubprocess("Global plug-in manager", GlobalExtensionManager.STARTER_FROM_LAUNCHER);
	}
}
