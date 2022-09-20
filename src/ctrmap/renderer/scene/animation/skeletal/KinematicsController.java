
package ctrmap.renderer.scene.animation.skeletal;

import xstandard.math.vec.Vec3f;
import xstandard.math.MatrixUtil;
import xstandard.math.vec.Matrix4;

public class KinematicsController {
	public boolean enabled = true;
	public String targetJointName;
	public TransformType type;
	public Vec3f value = new Vec3f();
	public KinematicsCallback callback;
	
	public KinematicsController(String jointName, KinematicsCallback callback){
		this(jointName, TransformType.CALLBACK);
		this.callback = callback;
	}
	
	public KinematicsController(String jointName, TransformType type){
		targetJointName = jointName;
		this.type = type;
		callback = null;
	}
	
	public Matrix4 applyToMatrix(Matrix4 globalJointMatrix){
		switch (type){
			case RELATIVE:
				globalJointMatrix.translate(value.x, value.y, value.z);
				break;
			case ABSOLUTE:
				globalJointMatrix.set(MatrixUtil.createTranslation(value.x, value.y, value.z));
				break;
			case CALLBACK:
				if (callback != null){
					callback.run(globalJointMatrix);
				}
				break;
		}
		return globalJointMatrix;
	}
	
	public static interface KinematicsCallback {
		public void run(Matrix4 globalJointMatrix);
	}
	
	public static enum TransformType{
		RELATIVE,
		ABSOLUTE,
		CALLBACK
	}
}
