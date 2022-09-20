
package ctrmap.renderer.scene.metadata.uniforms;

public abstract class CustomUniformInt extends CustomUniformIntArr {

	public CustomUniformInt(String name) {
		super(name);
	}
	
	@Override
	public abstract int intValue();
	
	@Override
	public int[] intValues(){
		return new int[]{intValue()};
	}
	
	@Override
	public int getValueCount(){
		return 1;
	}
}
