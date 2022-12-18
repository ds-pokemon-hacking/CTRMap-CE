package ctrmap.renderer.backends;

import ctrmap.renderer.scene.animation.AnimatedValue;
import org.joml.Matrix3f;
import xstandard.math.vec.Matrix4;
import xstandard.util.AllocationPool;

public class RenderAllocator {

	private static class UniqueMat4 extends Matrix4 {

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object o2) {
			return this == o2;
		}
	}
	
	private static class UniqueMat3f extends Matrix3f {

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object o2) {
			return this == o2;
		}
	}
	
	private static class UniqueAnimValue extends AnimatedValue {

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		@Override
		public boolean equals(Object o2) {
			return this == o2;
		}
	}

	private static final AllocationPool<Matrix4> MATRIX4_POOL = new AllocationPool<>(
		UniqueMat4.class,
		() -> {
			return new UniqueMat4();
		},
		(mtx) -> {
			mtx.identity();
		}
	);

	private static final AllocationPool<Matrix3f> MATRIX3_POOL = new AllocationPool<>(
		UniqueMat3f.class,
		() -> {
			return new UniqueMat3f();
		},
		(mtx) -> {
			mtx.identity();
		}
	);

	private static final AllocationPool<AnimatedValue> ANIMATED_VALUE_POOL = new AllocationPool<>(
		UniqueAnimValue.class,
		() -> {
			return new UniqueAnimValue();
		},
		(val) -> {
			val.exists = false;
			val.value = 0f;
		}
	);

	public static Matrix4 allocMatrix() {
		return MATRIX4_POOL.alloc();
	}

	public static void freeMatrix(Matrix4 mat) {
		MATRIX4_POOL.free(mat);
	}

	public static void freeMatrices(Matrix4... mats) {
		for (Matrix4 m : mats) {
			freeMatrix(m);
		}
	}

	public static Matrix3f allocMatrix3f() {
		return MATRIX3_POOL.alloc();
	}

	public static void freeMatrix3f(Matrix3f mat) {
		MATRIX3_POOL.free(mat);
	}

	public static void freeMatrices3f(Matrix3f... mats) {
		for (Matrix3f m : mats) {
			freeMatrix3f(m);
		}
	}

	public static AnimatedValue allocAnimatedValue() {
		return ANIMATED_VALUE_POOL.alloc();
	}

	public static void freeAnimatedValue(AnimatedValue val) {
		ANIMATED_VALUE_POOL.free(val);
	}

	public static void freeAnimatedValues(AnimatedValue... vals) {
		for (AnimatedValue v : vals) {
			freeAnimatedValue(v);
		}
	}
}
