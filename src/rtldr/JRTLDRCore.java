package rtldr;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import xstandard.fs.FSUtil;

public class JRTLDRCore {

	private static final Map<Class<? extends JExtensionReceiver>, List<JExtensionManager>> mgrMaps = new HashMap<>();
	private static final List<JExtensionManager> managers = new ArrayList<>();
	private static final List<RExtensionBase> plugins = new ArrayList<>();

	private static final List<String> loadedPluginPaths = new ArrayList<>();
	private static final Map<String, ClassLoader> classloaders = new HashMap<>();
	
	static {
		JGlobalExtensionDB.init();
	}
	
	public static void suppressDebugPluginByFileName(String fileName) {
		for (String path : loadedPluginPaths) {
			if (FSUtil.getFileName(path).equals(fileName)) {
				unloadJarExt(new File(path));
				System.out.println("Suppressed debug plugin " + path);
				break;
			}
		}
	}

	public static Preferences getPrefsNodeForExtensionManager(String key) {
		return Preferences.userRoot().node("RTLDR").node(key);
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
		for (ClassLoader cldr : classloaders.values()) {
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
	
	private static String getPathOfCldr(ClassLoader cldr) {
		for (Map.Entry<String, ClassLoader> e : classloaders.entrySet()) {
			if (e.getValue() == cldr) {
				return e.getKey();
			}
		}
		return null;
	}

	private static void loadRmoFromCldr(String rmoClassName, ClassLoader cldr, JExtensionManager mgr) {
		try {
			Class rmoClass = Class.forName(rmoClassName, true, cldr);
			if (RExtensionBase.class.isAssignableFrom(rmoClass)) {
				try {
					Object rmo = rmoClass.getDeclaredConstructor().newInstance();
					if (rmo != null && rmo instanceof RExtensionBase) {
						RExtensionBase riface = (RExtensionBase) rmo;
						plugins.add(riface);
						System.out.println("Bootstrapping plugin " + riface + " from ClassLoader " + getPathOfCldr(cldr));
						mgr.bootstrapR(riface);
					}
				} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
					Logger.getLogger(JRTLDRCore.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		} catch (ClassNotFoundException ex) {
			//Not registered
		}
	}

	private static void unloadRmoFromCldr(String rmoClassName, ClassLoader cldr, JExtensionManager mgr) {
		try {
			Class rmoClass = Class.forName(rmoClassName, true, cldr);
			if (rmoClass.getClassLoader() == cldr && RExtensionBase.class.isAssignableFrom(rmoClass)) {
				List<RExtensionBase> terminatedPlugins = new ArrayList<>();
				for (RExtensionBase plg : plugins) {
					//This is the same classloader instance, not a new one, so the class is the same
					if (plg.getClass() == rmoClass) {
						terminatedPlugins.add(plg);
					}
				}
				for (RExtensionBase term : terminatedPlugins) {
					System.out.println("Terminating plugin " + rmoClassName + " for receiver " + mgr.iface);
					mgr.terminateR(term);
				}
				plugins.removeAll(terminatedPlugins);
			}
		} catch (ClassNotFoundException ex) {
			//Not registered
		}
	}
	
	public static void loadJarExt(File file) {
		if (file != null && file.isFile()) {
			String absPath = file.getAbsolutePath();
			if (!loadedPluginPaths.contains(absPath)) {
				ClassLoader cldr = JJarLoader.mountFileToClasspath(file);
				if (cldr != null) {
					classloaders.put(absPath, cldr);
					for (JExtensionManager mgr : managers) {
						String rmoClassName = mgr.rmoClassName;
						loadRmoFromCldr(rmoClassName, cldr, mgr);
					}
				}
				loadedPluginPaths.add(absPath);
			}
		}
	}
	
	public static void unloadJarExt(File file) {
		if (file != null && file.isFile()) {
			String absPath = file.getAbsolutePath();
			if (loadedPluginPaths.contains(absPath)) {
				ClassLoader cldr = classloaders.get(absPath);
				if (cldr != null) {
					System.out.println("Got classloader " + cldr + " for plug-in " + absPath + ", terminating...");
					for (JExtensionManager mgr : managers) {
						String rmoClassName = mgr.rmoClassName;
						unloadRmoFromCldr(rmoClassName, cldr, mgr);
					}
				}
				loadedPluginPaths.remove(absPath);
				classloaders.remove(absPath);
			}
		}
	}
}
