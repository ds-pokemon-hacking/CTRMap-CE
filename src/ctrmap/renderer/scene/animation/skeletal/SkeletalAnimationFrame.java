package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.backends.RenderAllocator;
import ctrmap.renderer.scene.animation.AnimatedValue;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import xstandard.math.vec.Vec3f;
import org.joml.Matrix3f;
import static ctrmap.renderer.backends.RenderAllocator.allocAnimatedValue;

public class SkeletalAnimationFrame {

	public AnimatedValue tx = null;
	public AnimatedValue ty = null;
	public AnimatedValue tz = null;

	public AnimatedValue rx = null;
	public AnimatedValue ry = null;
	public AnimatedValue rz = null;

	public AnimatedValue sx = null;
	public AnimatedValue sy = null;
	public AnimatedValue sz = null;

	private final boolean wasManualAlloc;

	public SkeletalAnimationFrame(boolean manualAlloc) {
		wasManualAlloc = manualAlloc;
		tx = allocValue(manualAlloc);
		ty = allocValue(manualAlloc);
		tz = allocValue(manualAlloc);
		rx = allocValue(manualAlloc);
		ry = allocValue(manualAlloc);
		rz = allocValue(manualAlloc);
		sx = allocValue(manualAlloc);
		sy = allocValue(manualAlloc);
		sz = allocValue(manualAlloc);
	}

	private AnimatedValue allocValue(boolean manual) {
		return manual ? allocAnimatedValue() : new AnimatedValue();
	}

	public void free() {
		if (wasManualAlloc) {
			RenderAllocator.freeAnimatedValues(tx, ty, tz, rx, ry, rz, sx, sy, sz);
		}
	}

	public Vec3f getTranslation() {
		return new Vec3f(tx.value, ty.value, tz.value);
	}

	public Vec3f getRotationEuler() {
		return new Vec3f(rx.value, ry.value, rz.value);
	}

	public Quaternion getRotation() {
		Quaternion q = new Quaternion();
		getRotation(q);
		return q;
	}

	public Quaternion getRotation(Quaternion dest) {
		dest.rotationZYX(rz.value, ry.value, rx.value);
		return dest;
	}

	public Matrix3f getRotationMatrix() {
		Matrix3f mtx = new Matrix3f();
		mtx.rotateZYX(rz.value, ry.value, rx.value);
		return mtx;
	}

	public Vec3f getScale() {
		return new Vec3f(sx.value, sy.value, sz.value);
	}

	public Matrix4 createTransformMatrix() {
		return getTransformMatrix(wasManualAlloc ? RenderAllocator.allocMatrix() : new Matrix4());
	}

	public Matrix4 getTransformMatrix(Matrix4 dest) {
		dest.translation(tx.value, ty.value, tz.value);
		dest.rotateZYX(rz.value, ry.value, rx.value);
		dest.scale(sx.value, sy.value, sz.value);
		return dest;
	}

	@Override
	public String toString() {
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
