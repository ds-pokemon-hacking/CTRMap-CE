package rtldr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JGarbageCollector<T> {
	private final Map<RExtensionBase, List<T>> garbage = new HashMap<>();
	
	private RExtensionBase currentPlugin;
	
	public static void startListening(RExtensionBase plugin, JGarbageCollector... gcs) {
		for (JGarbageCollector c : gcs) {
			c.startListening(plugin);
		}
	}
	
	public static void stopListening(JGarbageCollector... gcs) {
		for (JGarbageCollector c : gcs) {
			c.stopListening();
		}
	}
	
	public boolean isListening() {
		return currentPlugin != null;
	}
	
	public void startListening(RExtensionBase plugin) {
		this.currentPlugin = plugin;
	}
	
	public void stopListening() {
		this.currentPlugin = null;
	}
	
	public T regGc(T g) {
		if (currentPlugin != null) {
			List<T> l = garbage.get(currentPlugin);
			if (l == null) {
				l = new ArrayList<>();
				garbage.put(currentPlugin, l);
			}
			l.add(g);
			return g;
		}
		return null;
	}
	
	public void collect(RExtensionBase plugin, Consumer<T> consumer) {
		List<T> gl = garbage.get(plugin);
		if (gl != null) {
			for (T g : gl) {
				consumer.accept(g);
			}
		}
	}
}
