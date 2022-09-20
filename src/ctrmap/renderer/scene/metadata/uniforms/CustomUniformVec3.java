
package ctrmap.renderer.scene.metadata.uniforms;

import ctrmap.renderer.scene.metadata.MetaDataValue;
import xstandard.math.vec.Vec3f;

public abstract class CustomUniformVec3 extends MetaDataValue {

	public CustomUniformVec3(String name) {
		super(name, null, true);
	}

	@Override
	public MetaDataValue.Type getType(){
		return Type.VEC3;
	}
	
	@Override
	public float floatValue(int index){
		return floatValues()[index];
	}
	
	@Override
	public abstract Vec3f vec3Value();
	
	@Override
	public float[] floatValues(){
		return vec3Value().toFloatUniform();
	}
	
	@Override
	public Vec3f[] vec3Values(){
		return new Vec3f[]{vec3Value()};
	}
	
	@Override
	public int getValueCount(){
		return 1;
	}
}
