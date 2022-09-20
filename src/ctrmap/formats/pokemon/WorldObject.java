package ctrmap.formats.pokemon;

import xstandard.math.vec.Vec3f;

public interface WorldObject {

	public Vec3f getWPos();

	public void setWPos(Vec3f vec);

	public Vec3f getWDim();

	public default void setWDim(Vec3f vec) {
		
	}
	
	public Vec3f getMinVector();

	public default float getRotationY(){
		return 0f;
	}

	public default float getCmOffset() {
		return 0f;
	}
}
