package rtldr;

import java.util.ArrayList;
import java.util.List;

public class JExtensionManager<J extends JExtensionReceiver<R>, R extends RExtensionBase<J>> {

	final J iface;
	final String rmoClassName;

	private final List<R> loadedExtensions = new ArrayList<>();
	
	private JExtensionStateListener extensionStateListener;

	public JExtensionManager(J iface, String rmoClassName) {
		this.iface = iface;
		this.rmoClassName = rmoClassName;
	}
	
	public Iterable<R> getLoadedExtensions() {
		return loadedExtensions;
	}
	
	public void setExtensionStateListener(JExtensionStateListener l) {
		this.extensionStateListener = l;
	}

	void bootstrapR(R riface) {
		if (!loadedExtensions.contains(riface)) {
			riface.attach(iface);
			loadedExtensions.add(riface);
			if (extensionStateListener != null) {
				extensionStateListener.onExtensionLoaded(riface);
			}
		}
	}
}
