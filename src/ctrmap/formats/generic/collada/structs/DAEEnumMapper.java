package ctrmap.formats.generic.collada.structs;

import java.util.HashMap;
import java.util.Map;

public class DAEEnumMapper<E extends Enum> {

	private final E defaultValue;

	private final Map<String, E> values = new HashMap<>();
	private final Map<E, String> tags = new HashMap<>();

	public DAEEnumMapper(E defaultValue, Object... setup) {
		this.defaultValue = defaultValue;
		for (int i = 0; i < (setup.length >> 1); i++) {
			String key = (String) (setup[i * 2]);
			E value = (E) (setup[i * 2 + 1]);
			values.put(key, value);
			if (!tags.containsKey(value)) {
				tags.put(value, key);
			}
		}
	}

	public E getValue(String str) {
		return getValue(str, defaultValue);
	}
	
	public E getValue(String str, E defaultValue) {
		return values.getOrDefault(str, defaultValue);
	}

	public String getName(E value) {
		return tags.getOrDefault(value, null);
	}
}
