
import ctrmap.Launc;

import ctrmap.creativestudio.NGCSStarter;
import ctrmap.editor.CTRMap;

public class LauncherPlugin implements Launc.IPlugin {
	
	@Override
	public void attach(Launc.JulietInterface j) {
		j.registerSubprocess("CTRMap", CTRMap.STARTER);
		j.registerSubprocess("CreativeStudio", NGCSStarter.INSTANCE);
	}
}
