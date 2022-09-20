
package ctrmap.renderer.backends;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import java.util.HashMap;
import java.util.Map;

public class DriverBool {
	private Map<Object, Boolean> values = new HashMap<>();
	
	public boolean get(IRenderDriver drv){
		Object key = drv.getIdentity();
		boolean v = true;
		if (values.containsKey(key)){
			v = values.get(key);
		}
		values.put(key, false);
		return v;
	}
	
	public void resetAll(){
		for (Object key : values.keySet()){
			values.put(key, true);
		}
	}
	
	public void remove(IRenderDriver drv) {
		values.remove(drv.getIdentity());
	}
}
