package rtldr;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import xstandard.util.collections.IntList;

public class JRTLDRCore {

	private static final Map<Class<? extends JExtensionReceiver>, List<JExtensionManager>> mgrMaps = new HashMap<>();
	private static final List<JExtensionManager> managers = new ArrayList<>();
	private static final List<RExtensionBase> plugins = new ArrayList<>();

	private static final List<String> loadedPluginPaths = new ArrayList<>();
	private static final List<ClassLoader> classloaders = new ArrayList<>();

	private static final IntList loadedPluginHashes = new IntList();

	public static Preferences getPrefsNodeForExtensionManager(String key) {
		return Preferences.userRoot().node("RomeoConfig").node(key);
	}

	public static <R extends RExtensionBase<J>, J extends JExtensionReceiver<R>> JExtensionManager<J, R> bindExtensionManager(String rmoClassName, JExtensionReceiver<R> iface) {
		return bindExtensionManager(rmoClassName, iface, null);
	}

	public static <R extends RExtensionBase<J>, J extends JExtensionReceiver<R>> JExtensionManager<J, R> bindExtensionManager(String rmoClassName, JExtensionReceiver<R> iface, JExtensionStateListener<R> extensionListener) {
		List<JExtensionManager> mgrList = mgrMaps.get(iface.getClass());
		if (mgrList == null) {
			mgrList = new ArrayList<>();
			mgrMaps.put(iface.getClass(), mgrList);
		}
		for (JExtensionManager mgr : mgrList) {
			if (mgr.iface == iface) {
				return mgr;
			}
		}

		JExtensionManager mgr = new JExtensionManager(iface, rmoClassName);
		mgr.setExtensionStateListener(extensionListener);
		managers.add(mgr);
		mgrList.add(mgr);
		//if the parent module contains a plugin, load it as well
		loadRmoFromCldr(rmoClassName, JRTLDRCore.class.getClassLoader(), mgr);
		for (ClassLoader cldr : classloaders) {
			//already loaded JARs
			loadRmoFromCldr(rmoClassName, cldr, mgr);
		}

		return mgr;
	}

	public static void unregistExtensionManager(JExtensionReceiver iface) {
		if (iface != null) {
			List<JExtensionManager> l = mgrMaps.get(iface.getClass());
			if (l != null) {
				for (JExtensionManager mgr : l) {
					if (mgr.iface == iface) {
						l.remove(mgr);
						managers.remove(mgr);
						break;
					}
				}
			}
		}
	}

	public static void loadExtensionDirectory(File dir) {
		if (dir != null && dir.exists() && dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.getName().endsWith(".jar")) {
					loadJarExt(file);
				}
			}
		}
	}

	public static void loadExtension(JExtensionReceiver j, RExtensionBase r) {
		if (!plugins.contains(r)) {
			for (JExtensionManager mgr : managers) {
				if (mgr.iface == j) {
					plugins.add(r);
					mgr.bootstrapR(r);
					break;
				}
			}
		}
	}

	public static void loadExtensions(JExtensionReceiver j, RExtensionBase... rs) {
		for (RExtensionBase r : rs) {
			loadExtension(j, r);
		}
	}

	private static int makeClassHash(ClassLoader cldr, String className) {
		try (InputStream in = new BufferedInputStream(cldr.getResourceAsStream(className.replace('.', '/') + ".class"))) {
			int hash = 7;
			while (in.available() > 0) {
				hash = 37 * hash + in.read();
			}
			return hash;
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return 0;
	}

	private static void loadRmoFromCldr(String rmoClassName, ClassLoader cldr, JExtensionManager mgr) {
		try {
			Class rmoClass = Class.forName(rmoClassName, true, cldr);
			if (RExtensionBase.class.isAssignableFrom(rmoClass)) {
				int hash = makeClassHash(cldr, rmoClassName);
				if (!loadedPluginHashes.contains(hash)) {
					try {
						Object rmo = rmoClass.getDeclaredConstructor().newInstance();
						if (rmo != null && rmo instanceof RExtensionBase) {
							RExtensionBase riface = (RExtensionBase) rmo;
							plugins.add(riface);
							loadedPluginHashes.add(hash);
							mgr.bootstrapR(riface);
						}
					} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
						Logger.getLogger(JRTLDRCore.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
			}
		} catch (ClassNotFoundException ex) {
			//Not registered
		}
	}

	public static void loadJarExt(File file) {
		String absPath = file.getAbsolutePath();
		if (!loadedPluginPaths.contains(absPath)) {
			ClassLoader cldr = JJarLoader.mountFileToClasspath(file);
			if (cldr != null) {
				classloaders.add(cldr);
				for (JExtensionManager mgr : managers) {
					String rmoClassName = mgr.rmoClassName;
					loadRmoFromCldr(rmoClassName, cldr, mgr);
				}
			}
			loadedPluginPaths.add(absPath);
		}
	}
}
