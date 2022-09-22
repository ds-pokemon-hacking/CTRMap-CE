package rtldr;

import java.util.prefs.Preferences;

public class JGlobalExtensionDB extends JAbstractPluginDatabase {

	private static final Preferences DIR = JRTLDRCore.getPrefsNodeForExtensionManager("GlobalPlugins");
	private static JGlobalExtensionDB INSTANCE;

	private JGlobalExtensionDB() {

	}
	
	public static void init() {
		if (INSTANCE != null) {
			throw new RuntimeException("Init called twice!");
		}
		INSTANCE = new JGlobalExtensionDB();
	}
	
	public static JGlobalExtensionDB getInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("Init not called!");
		}
		return INSTANCE;
	}
	
	@Override
	protected boolean isSynchronized() {
		return true;
	}

	@Override
	protected Preferences getPrefsRoot() {
		return DIR;
	}

}
