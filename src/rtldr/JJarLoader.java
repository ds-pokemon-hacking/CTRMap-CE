package rtldr;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JJarLoader {

	public static ClassLoader mountFileToClasspath(File jarFile) {
		try {
			return mountURLToClasspath(jarFile.toURI().toURL());
		} catch (MalformedURLException ex) {
			Logger.getLogger(JJarLoader.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	private static ClassLoader mountURLToClasspath(URL... urls) {
		ClassLoader loader = new JHierarchicalURLClassLoader(
			urls,
			JJarLoader.class.getClassLoader()
		);
		return loader;
	}
}
