package rtldr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import xstandard.formats.zip.ZipArchive;
import xstandard.fs.accessors.DiskFile;

public abstract class JAbstractPluginDatabase {

	protected JAbstractPluginDatabase() {
		if (isSynchronized()) {
			loadAllPlugins();
		}
	}

	protected abstract Preferences getPrefsRoot();

	protected boolean isSynchronized() {
		return false;
	}

	public boolean hasPlugin(String name) {
		return getPrefsRoot().get(name, null) != null;
	}

	public List<PluginEntry> getPlugins() {
		Preferences dir = getPrefsRoot();
		List<PluginEntry> plugins = new ArrayList<>();
		try {
			for (String key : dir.keys()) {
				String path = dir.get(key, null);
				if (path != null) {
					DiskFile file = new DiskFile(path);
					if (ZipArchive.isZip(file)) {
						plugins.add(new PluginEntry(key, path));
					} else {
						dir.remove(key);
					}
				}
			}
		} catch (BackingStoreException ex) {

		}
		return plugins;
	}

	public PluginEntry getPlugin(String name) {
		String path = getPrefsRoot().get(name, null);
		if (path != null) {
			return new PluginEntry(name, path);
		}
		return null;
	}

	public void removePlugin(String name) {
		if (name != null) {
			PluginEntry e = getPlugin(name);
			if (isSynchronized()) {
				JRTLDRCore.unloadJarExt(new File(e.path));
			}
			getPrefsRoot().remove(name);
		}
	}

	public PluginEntry addPluginPath(String name, String path) {
		getPrefsRoot().put(name, path);
		if (isSynchronized()) {
			JRTLDRCore.loadJarExt(new File(path));
		}
		return new PluginEntry(name, path);
	}

	public final void loadAllPlugins() {
		for (PluginEntry plg : getPlugins()) {
			System.out.println("Loading plug-in " + plg.path + "...");
			JRTLDRCore.loadJarExt(new File(plg.path));
		}
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
