
package ctrmap.formats.generic.collada;

import java.util.HashSet;

public class DAEConvState {
	private HashSet<String> idPool = new HashSet<>();
	
	public boolean existsID(String id) {
		return idPool.contains(id);
	}
	
	public void registerID(String id) {
		idPool.add(id);
	}
}
