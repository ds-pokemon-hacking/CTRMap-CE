package rtldr;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import xstandard.fs.FSUtil;

public class JRTLDRCore {

	private static final Map<Class<? extends JExtensionReceiver>, List<JExtensionManager>> mgrMaps = new HashMap<>();
	private static final List<JExtensionManager> managers = new ArrayList<>();
	private static final List<RExtensionBase> plugins = new ArrayList<>();

	private static final Set<String> suppressedPluginFileNames = new HashSet<>();
	private static final List<String> loadedPluginPaths = new ArrayList<>();
	private static final Map<String, JExtensionClassLoader> classloaders = new HashMap<>();

	private static final JCombinedClassLoader linkingClassLoader;
	private static final List<JExtensionClassLoader> selfClassLoaders = new ArrayList<>();

	static {
		linkingClassLoader = new JCombinedClassLoader(JRTLDRCore.class.getClassLoader());
		//create a duplicate classloader pointing to the same source for loading child plugins
		addDebugSelfClassLoader(JRTLDRCore.class.getProtectionDomain().getCodeSource());
		JGlobalExtensionDB.init();
	}

	public static void addDebugSelfClassLoader(CodeSource codeSrc) {
		JExtensionClassLoader rootSelfClassLoader = new JExtensionClassLoader(
			new URL[]{
				codeSrc.getLocation()
			},
			linkingClassLoader
		);
		selfClassLoaders.add(rootSelfClassLoader);
		linkingClassLoader.addChild(rootSelfClassLoader);
		onCldrAdded(rootSelfClassLoader);
	}

	public static void suppressDebugPluginByFileName(String fileName) {
		suppressedPluginFileNames.add(fileName);
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
		for (JExtensionClassLoader cldr : selfClassLoaders) {
			loadRmoFromCldr(rmoClassName, cldr, mgr);
		}
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
		for (Map.Entry<String, JExtensionClassLoader> e : classloaders.entrySet()) {
			if (e.getValue() == cldr) {
				return e.getKey();
			}
		}
		return null;
	}

	static boolean isRmoPluginClass(String className) {
		if (className.contains("$")) {
			className = className.substring(0, className.indexOf('$'));
		}
		for (JExtensionManager mgr : managers) {
			if (mgr.rmoClassName.equals(className)) {
				return true;
			}
		}
		return false;
	}

	private static void loadRmoFromCldr(String rmoClassName, ClassLoader cldr, JExtensionManager mgr) {
		try {
			Class rmoClass = Class.forName(rmoClassName, true, cldr);
			if (rmoClass.getClassLoader() == cldr && RExtensionBase.class.isAssignableFrom(rmoClass)) {
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

	private static void onCldrAdded(ClassLoader cldr) {
		for (JExtensionManager mgr : managers) {
			String rmoClassName = mgr.rmoClassName;
			loadRmoFromCldr(rmoClassName, cldr, mgr);
		}
	}

	public static void loadJarExt(File file) {
		loadJarExt(file, false);
	}
	
	public static void loadJarExt(File file, boolean ignoreSuppressed) {
		if (file != null && file.isFile()) {
			if (!ignoreSuppressed && suppressedPluginFileNames.contains(file.getName())) {
				System.out.println("Suppressed debug plugin " + file);
				return;
			}
			String absPath = file.getAbsolutePath();
			if (!loadedPluginPaths.contains(absPath)) {
				JExtensionClassLoader cldr = JJarLoader.mountFileToClasspath(linkingClassLoader, file);
				if (cldr != null) {
					linkingClassLoader.addChild(cldr);
					classloaders.put(absPath, cldr);
					onCldrAdded(cldr);
				}
				loadedPluginPaths.add(absPath);
			}
		}
	}

	public static void unloadJarExt(File file) {
		if (file != null && file.isFile()) {
			String absPath = file.getAbsolutePath();
			if (loadedPluginPaths.contains(absPath)) {
				JExtensionClassLoader cldr = classloaders.get(absPath);
				if (cldr != null) {
					System.out.println("Got classloader " + cldr + " for plug-in " + absPath + ", terminating...");
					for (JExtensionManager mgr : managers) {
						String rmoClassName = mgr.rmoClassName;
						unloadRmoFromCldr(rmoClassName, cldr, mgr);
					}
					linkingClassLoader.removeChild(cldr);
				}
				loadedPluginPaths.remove(absPath);
				classloaders.remove(absPath);
			}
		}
	}
}
