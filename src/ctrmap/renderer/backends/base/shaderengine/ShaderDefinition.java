
package ctrmap.renderer.backends.base.shaderengine;

public class ShaderDefinition {
	public String key;
	public String value;
	public final ShaderDefinitionType type;
	
	protected ShaderDefinition(String key, Object value, ShaderDefinitionType type){
		this.key = key;
		this.value = String.valueOf(value);
		this.type = type;
	}
	
	public ShaderDefinition(String key, Object value){
		this(key, value, ShaderDefinitionType.NORMAL);
	}
	
	public static enum ShaderDefinitionType {
		NORMAL,
		RENDER_TARGET
	}
}
