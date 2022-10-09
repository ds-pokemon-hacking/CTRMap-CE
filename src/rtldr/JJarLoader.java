package rtldr;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JJarLoader {

	public static JExtensionClassLoader mountFileToClasspath(ClassLoader parent, File jarFile) {
		try {
			return mountURLToClasspath(parent, jarFile.toURI().toURL());
		} catch (MalformedURLException ex) {
			Logger.getLogger(JJarLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private static JExtensionClassLoader mountURLToClasspath(ClassLoader parent, URL... urls) {
		JExtensionClassLoader loader = new JExtensionClassLoader(
			urls,
			parent
		);
		return loader;
	}
}
