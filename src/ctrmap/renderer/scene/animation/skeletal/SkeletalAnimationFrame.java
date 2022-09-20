
package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.scene.animation.AnimatedValue;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import xstandard.math.vec.Vec3f;
import org.joml.Matrix3f;

public class SkeletalAnimationFrame {
	public AnimatedValue tx = new AnimatedValue();
	public AnimatedValue ty = new AnimatedValue();
	public AnimatedValue tz = new AnimatedValue();

	public AnimatedValue rx = new AnimatedValue();
	public AnimatedValue ry = new AnimatedValue();
	public AnimatedValue rz = new AnimatedValue();

	public AnimatedValue sx = new AnimatedValue();
	public AnimatedValue sy = new AnimatedValue();
	public AnimatedValue sz = new AnimatedValue();
	
	public Vec3f getTranslation(){
		return new Vec3f(tx.value, ty.value, tz.value);
	}
	
	public Vec3f getRotationEuler() {
		return new Vec3f(rx.value, ry.value, rz.value);
	}
	
	public Quaternion getRotation(){
		Quaternion q = new Quaternion();
		getRotation(q);
		return q;
	}
	
	public Quaternion getRotation(Quaternion dest) {
		dest.rotationZYX(rz.value, ry.value, rx.value);
		return dest;
	}
	
	public Matrix3f getRotationMatrix(){
		Matrix3f mtx = new Matrix3f();
		mtx.rotateZYX(rz.value, ry.value, rx.value);
		return mtx;
	}
	
	public Vec3f getScale(){
		return new Vec3f(sx.value, sy.value, sz.value);
	}
	
	public Matrix4 createTransformMatrix(){
		Matrix4 mtx = new Matrix4();
		mtx.translate(tx.value, ty.value, tz.value);
		mtx.rotateZYX(rz.value, ry.value, rx.value);
		mtx.scale(sx.value, sy.value, sz.value);
		return mtx;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("TRA: ");
		sb.append(tx);
		sb.append("/");
		sb.append(ty);
		sb.append("/");
		sb.append(tz);
		sb.append("\n");
		sb.append("ROT: ");
		sb.append(rx);
		sb.append("/");
		sb.append(ry);
		sb.append("/");
		sb.append(rz);
		sb.append("\n");
		sb.append("SCA: ");
		sb.append(sx);
		sb.append("/");
		sb.append(sy);
		sb.append("/");
		sb.append(sz);
		return sb.toString();
	}
}
