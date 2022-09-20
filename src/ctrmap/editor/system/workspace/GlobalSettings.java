package ctrmap.editor.system.workspace;

import ctrmap.util.CMPrefs;
import java.util.prefs.Preferences;

public class GlobalSettings {
	
	public static final String CTRMAP_GLOBAL_SETTINGS_DIR = "Settings";

	private static final Preferences PREFS = CMPrefs.node(CTRMAP_GLOBAL_SETTINGS_DIR);

	public static final String ESPICA_PATH_KEY = "ESPICAPath";

	public static String getValue(String key, String defaultValue){
		return PREFS.get(key, defaultValue);
	}
	
	public static void put(String key, String value){
		PREFS.put(key, value);
	}
}
