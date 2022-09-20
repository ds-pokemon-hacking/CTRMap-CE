
package ctrmap.renderer.backends;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import java.util.HashMap;
import java.util.Map;

public class DriverHandle {
	private Map<Object, Integer> values = new HashMap<>();
	
	public int get(IRenderDriver drv){
		Object key = drv.getIdentity();
		return values.getOrDefault(key, -1);
	}
	
	public void set(IRenderDriver drv, int value) {
		if (value == -1) {
			System.err.println("Warning: manual handle assignment to -1");
		}
		values.put(drv.getIdentity(), value);
	}
	
	public void resetAll(){
		for (Object key : values.keySet()){
			values.remove(key);
		}
	}
	
	public void remove(IRenderDriver drv) {
		values.remove(drv.getIdentity());
	}
}
