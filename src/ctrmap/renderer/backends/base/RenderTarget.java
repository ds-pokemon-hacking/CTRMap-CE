
package ctrmap.renderer.backends.base;

import ctrmap.renderer.backends.DriverHandle;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.base.shaderengine.ShaderDefinition;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
public class RenderTarget extends ShaderDefinition {	
	private DriverHandle rendererPointers = new DriverHandle();
	
	public final TextureFormatHandler format;
	public final int renderTargetId;
	
	public RenderTarget(String name, TextureFormatHandler format, int id){
		super(name, id, ShaderDefinitionType.RENDER_TARGET);
		this.format = format;
		renderTargetId = id;
	}
	
	public void setPointer(IRenderDriver drv, int value){
		rendererPointers.set(drv, value);
	}
	
	public int getPointer(IRenderDriver drv){
		return rendererPointers.get(drv);
	}
}
