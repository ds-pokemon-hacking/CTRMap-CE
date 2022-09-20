
package ctrmap.formats.generic.collada;

import ctrmap.formats.generic.collada.structs.DAEIDAble;
import xstandard.INamed;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DAEConvMemory<I extends INamed, O extends DAEIDAble> {
	
	protected final DAEConvState state;
		
	private String idSuffix = "";
	
	protected final Map<I, O> map = new HashMap<>();
	
	public DAEConvMemory(DAEConvState state, String idSuffix) {
		this.state = state;
		this.idSuffix = idSuffix;
	}
	
	protected boolean isNonunique(String id) {
		return state.existsID(id);
	}
	
	protected void setID(O output, String id) {
		output.setID(id);
	}
	
	protected String getOmnisuffix() {
		return "-id";
	}
	
	private String getUniqueId(String name) {
		//WARNING
		//Shitty tools (spoiler alert: blender again) exert undefined behavior with IDs that are 64+ characters in length
		//We have to make sure that our names are less than that to satisfy shitty tools
		
		name = XmlFormat.sanitizeName(name);
		String id = XmlFormat.makeSafeId(name + "-" + idSuffix + getOmnisuffix());
		int index = 2;
		while (isNonunique(id)) {
			id = XmlFormat.makeSafeId(name + "-" + idSuffix + "-" + index + getOmnisuffix());
			index++;
		}
		return id;
	}
	
	public void put(I input, O output) {
		map.put(input, output);
		String id = getUniqueId(input.getName());
		setID(output, id);
		state.registerID(id);
	}
	
	public O findByInput(I input) {
		return map.get(input);
	}
	
	public O findByInputName(String inputName) {
		for (Map.Entry<I, O> e : map.entrySet()) {
			if (Objects.equals(e.getKey().getName(), inputName)) {
				return e.getValue();
			}
		}
		return null;
	}
}
