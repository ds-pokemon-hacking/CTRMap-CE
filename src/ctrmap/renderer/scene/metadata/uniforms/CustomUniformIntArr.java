
package ctrmap.renderer.scene.metadata.uniforms;

import ctrmap.renderer.scene.metadata.MetaDataValue;

public abstract class CustomUniformIntArr extends MetaDataValue {

	public CustomUniformIntArr(String name) {
		super(name, null, true);
	}

	@Override
	public MetaDataValue.Type getType(){
		return Type.INT;
	}
	
	@Override
	public int intValue(int index){
		return intValues()[index];
	}
	
	@Override
	public abstract int[] intValues();
	
	@Override
	public int getValueCount(){
		return intValues().length;
	}
}
