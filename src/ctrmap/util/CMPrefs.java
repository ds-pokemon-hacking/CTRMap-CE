
package ctrmap.util;

import java.util.prefs.Preferences;

public class CMPrefs {
	private static final Preferences PREFS_ROOT = Preferences.userRoot().node("CTRMap");
	
	public static Preferences node(String name){
		if (name == null) {
			return rootNode();
		}
		return PREFS_ROOT.node(name);
	}
	
	public static Preferences rootNode() {
		return PREFS_ROOT;
	}
}
