package ctrmap.renderer.backends.base.shaderengine;

public class ShaderUniform {
	public final String uniformName;
	public int intValue;
	
	public ShaderUniform(String name, int val) {
		uniformName = name;
		intValue = val;
	}
}
