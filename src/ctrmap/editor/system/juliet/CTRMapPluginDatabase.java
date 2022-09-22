package ctrmap.editor.system.juliet;

import ctrmap.util.CMPrefs;
import java.util.prefs.Preferences;
import rtldr.JAbstractPluginDatabase;

public class CTRMapPluginDatabase extends JAbstractPluginDatabase {

	private static final Preferences DIR = CMPrefs.node("JulietDB");
	public static final CTRMapPluginDatabase INSTANCE = new CTRMapPluginDatabase();

	private CTRMapPluginDatabase() {
	}

	@Override
	protected Preferences getPrefsRoot() {
		return DIR;
	}
}
