
package ctrmap.renderer.scene.metadata.uniforms;

import ctrmap.renderer.scene.metadata.MetaDataValue;

public abstract class CustomUniformFloatArr extends MetaDataValue {

	public CustomUniformFloatArr(String name) {
		super(name, null, true);
	}

	@Override
	public MetaDataValue.Type getType(){
		return Type.FLOAT;
	}
	
	@Override
	public float floatValue(int index){
		return floatValues()[index];
	}
	
	@Override
	public abstract float[] floatValues();
	
	@Override
	public int getValueCount(){
		return floatValues().length;
	}
}
