package rtldr;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * Inspired by
 * https://stackoverflow.com/questions/5445511/how-do-i-create-a-parent-last-child-first-classloader-in-java-or-how-to-overr
 *
 * This will force all root extension classes to be loaded from the lowest priority classloader, meaning that
 * even plug-ins in JRTLDRCore's dummyRootClassloader will be loaded on the same level as JAR ones, and in
 * turn will not cause problems that would stem from being subject to the AppClassLoader.
 */
public class JExtensionClassLoader extends URLClassLoader {

	public JExtensionClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
	}

	public Class<?> findLocalClass(String name) throws ClassNotFoundException {
		return super.findClass(name);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			// first try to use the URLClassLoader findClass
			return findLocalClass(name);
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
				Class loaded = findLoadedClass(name);
				if (loaded != null) {
					return loaded;
				}
				Class c = findClass(name);
				return c;
			} catch (ClassNotFoundException e) {
				// didn't find it, try the parent
			}
		}
		return super.loadClass(name, resolve);
	}

	public Class<?> getLoadedClass(String name) throws ClassNotFoundException {
		Class<?> cls = findLoadedClass(name);
		if (cls == null) {
			throw new ClassNotFoundException(name);
		}
		return cls;
	}

	@Override
	public String toString() {
		if (getURLs().length > 0) {
			return "JExtensionClassLoader:" + getURLs()[0];
		}
		return super.toString();
	}
}
