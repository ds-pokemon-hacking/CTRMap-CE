package rtldr;

import java.util.ArrayList;
import java.util.List;
import xstandard.util.ArraysEx;

/**
 * Sometimes (and often in actually warranted cases), extensions may want to have a dependency on another
 * extension. However, since the Java ClassLoader hierarchy only scans upwards, classes from sibling loaders
 * are normally not accessible. This ClassLoader solves the problem by resolving classes across all children,
 * and the parent AppClassLoader, making every class accessible on its level. However, the system ClassLoader
 * is still one level above, meaning that encapsulation away from the core is preserved.
 */
public class JCombinedClassLoader extends ClassLoader {

	private final ClassLoaderProtectionBypass bypassParent;
	private final List<JExtensionClassLoader> children = new ArrayList<>();

	public JCombinedClassLoader(ClassLoader parent) {
		this(new ClassLoaderProtectionBypass(parent));
	}

	private JCombinedClassLoader(ClassLoaderProtectionBypass parent) {
		super(parent);
		this.bypassParent = parent;
	}

	public void addChild(JExtensionClassLoader child) {
		ArraysEx.addIfNotNullOrContains(children, child);
	}

	public void removeChild(JExtensionClassLoader child) {
		children.remove(child);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		//We will not consider the parent here as it should already have stepped up
		//during loadClass
		for (JExtensionClassLoader child : children) {
			try {
				return child.findLocalClass(name);
			} catch (ClassNotFoundException e2) {
			}
		}
		throw new ClassNotFoundException(name);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return bypassParent.loadClass(name, resolve);
		} catch (ClassNotFoundException ex) {
			for (JExtensionClassLoader child : children) {
				try {
					return child.getLoadedClass(name);
				} catch (ClassNotFoundException e2) {
				}
			}
		}
		return findClass(name);
	}

	private static class ClassLoaderProtectionBypass extends ClassLoader {

		public ClassLoaderProtectionBypass(ClassLoader parent) {
			super(parent);
		}

		@Override
		public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
			return super.loadClass(name, resolve);
		}
	}
}
