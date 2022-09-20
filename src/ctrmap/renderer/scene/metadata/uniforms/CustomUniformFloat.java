
package ctrmap.renderer.scene.metadata.uniforms;

public abstract class CustomUniformFloat extends CustomUniformFloatArr {

	public CustomUniformFloat(String name) {
		super(name);
	}

	@Override
	public abstract float floatValue();
	
	@Override
	public float[] floatValues(){
		return new float[]{floatValue()};
	}
	
	@Override
	public int getValueCount(){
		return 1;
	}
}
