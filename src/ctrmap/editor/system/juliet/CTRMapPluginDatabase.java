package ctrmap.editor.system.juliet;

import ctrmap.util.CMPrefs;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import xstandard.formats.zip.ZipArchive;
import xstandard.fs.accessors.DiskFile;

public class CTRMapPluginDatabase {

	private static final Preferences DIR = CMPrefs.node("JulietDB");

	public static boolean hasPlugin(String name) {
		return DIR.get(name, null) != null;
	}

	public static List<PluginEntry> getPlugins() {
		List<PluginEntry> plugins = new ArrayList<>();
		try {
			for (String key : DIR.keys()) {
				String path = DIR.get(key, null);
				if (path != null) {
					DiskFile file = new DiskFile(path);
					if (ZipArchive.isZip(file)) {
						plugins.add(new PluginEntry(key, path));
					} else {
						DIR.remove(key);
					}
				}
			}
		} catch (BackingStoreException ex) {

		}
		return plugins;
	}

	public static void removePlugin(String name) {
		if (name != null) {
			DIR.remove(name);
		}
	}

	public static PluginEntry addPluginPath(String name, String path) {
		DIR.put(name, path);
		return new PluginEntry(name, path);
	}

	public static class PluginEntry {

		public final String name;
		public final String path;

		private PluginEntry(String name, String path) {
			this.name = name;
			this.path = path;
		}
	}
}
