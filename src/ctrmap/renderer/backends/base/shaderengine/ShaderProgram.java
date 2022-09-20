package ctrmap.renderer.backends.base.shaderengine;

import ctrmap.renderer.backends.DriverHandle;
import ctrmap.renderer.backends.base.RenderState;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import java.util.HashMap;
import java.util.Map;

public class ShaderProgram {
	public final DriverHandle handle = new DriverHandle();
	
	protected Map<String, Integer> uniformLocations = new HashMap<>();
	
	public boolean isCurrent(RenderState rs){
		return handle == rs.program;
	}
	
	public int getUniformLocation(String name, IRenderDriver driver){
		int loc;
		if (uniformLocations.containsKey(name)){
			loc = uniformLocations.get(name);
		}
		else {
			loc = driver.getUniformLocation(name, this);
			uniformLocations.put(name, loc);
		}
		return loc;
	}
}
