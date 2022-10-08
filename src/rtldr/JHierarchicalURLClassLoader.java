package rtldr;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Inspired by
 * https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader-in-java-or-how-to-overr
 * 
 * This will force all root extension classes to be loaded from the lowest priority classloader,
 * meaning that even plug-ins in JRTLDRCore's dummyRootClassloader will be loaded on the same level
 * as JAR ones, and in turn will not cause problems that would stem from being subject to the AppClassLoader.
 */
public class JHierarchicalURLClassLoader extends URLClassLoader {

	public JHierarchicalURLClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			// first try to use the URLClassLoader findClass
			return super.findClass(name);
		} catch (ClassNotFoundException e) {
			// if that fails, we ask our real parent classloader to load the class (we give up)
			return getParent().loadClass(name);
		}
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if (JRTLDRCore.isRmoPluginClass(name)) {
			try {
				// first we try to find a class inside the child classloader
				return findClass(name);
			} catch (ClassNotFoundException e) {
				// didn't find it, try the parent
			}
		}
		return super.loadClass(name, resolve);
	}
}
