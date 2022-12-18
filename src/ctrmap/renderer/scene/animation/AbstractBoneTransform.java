package ctrmap.renderer.scene.animation;

import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.math.MathEx;
import xstandard.math.vec.Vec3f;
import java.util.List;

public abstract class AbstractBoneTransform implements NamedResource {

	public String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public KeyFrame.InterpolationMethod getHighestInterpMethod() {
		KeyFrame.InterpolationMethod method = KeyFrame.InterpolationMethod.STEP;
		for (KeyFrameList kfl : getAllKfLists()) {
			KeyFrame.InterpolationMethod newMethod = kfl.getInterpolationHighest();
			if (newMethod == KeyFrame.InterpolationMethod.HERMITE || method == KeyFrame.InterpolationMethod.STEP) {
				method = newMethod;
			}
		}
		return method;
	}

	public abstract List<KeyFrameList> getAllKfLists();

	public void addVector(float frame, Vec3f vec, List<KeyFrame> x, List<KeyFrame> y, List<KeyFrame> z) {
		x.add(new KeyFrame(frame, vec.x));
		y.add(new KeyFrame(frame, vec.y));
		z.add(new KeyFrame(frame, vec.z));
	}

	public void addVectorDegreeAngle(float frame, Vec3f vec, List<KeyFrame> x, List<KeyFrame> y, List<KeyFrame> z) {
		x.add(new KeyFrame(frame, vec.x * MathEx.DEGREES_TO_RADIANS));
		y.add(new KeyFrame(frame, vec.y * MathEx.DEGREES_TO_RADIANS));
		z.add(new KeyFrame(frame, vec.z * MathEx.DEGREES_TO_RADIANS));
	}

	public static float getTangentAt(KeyFrameList l, float frame) {
		frame = l.normalizeFrame(frame);

		KeyFrame left = getNearKeyFrame(l, frame, true);
		KeyFrame right = getNearKeyFrame(l, frame, false);

		if (left == null || right == null) {
			return 0f;
		}

		float w = (frame - left.frame) / (right.frame - left.frame);

		return left.outSlope + w * (right.inSlope - left.outSlope);
	}

	public static AnimatedValue getValueAt(KeyFrameList l, float frame) {
		return getValueAt(l, frame, new AnimatedValue());
	}

	public static AnimatedValue getValueAt(KeyFrameList l, float frame, AnimatedValue dest) {
		return getValueAt(l, frame, false, dest);
	}

	public static AnimatedValue getValueAt(KeyFrameList l, float frame, boolean disableInterpolation) {
		return getValueAt(l, frame, disableInterpolation, new AnimatedValue());
	}

	public static AnimatedValue getValueAt(KeyFrameList l, float frame, boolean disableInterpolation, AnimatedValue dest) {
		frame = l.normalizeFrame(frame);

		KeyFrame left = getNearKeyFrame(l, frame, true);
		KeyFrame right = getNearKeyFrame(l, frame, false);

		if (left == null || right == null) {
			dest.exists = false;
			return dest;
		}

		if (disableInterpolation) {
			return dest.set(left.value);
		}

		return getValueInterpolated(left, right, frame, dest);
	}

	public static AnimatedValue getValueInterpolated(KeyFrame a, KeyFrame b, float frame) {
		return getValueInterpolated(a, b, frame, new AnimatedValue());
	}

	public static AnimatedValue getValueInterpolated(KeyFrame a, KeyFrame b, float frame, AnimatedValue dest) {
		if (a == null || b == null) {
			dest.exists = false;
			return dest;
		}

		if (a.interpolation == KeyFrame.InterpolationMethod.STEP || b.frame < frame) {
			return dest.set(a.value);
		}

		if (a.frame == b.frame || a.value == b.value || frame == a.frame) {
			return dest.set(a.value);
		}

		if (frame == b.frame || a.frame > frame) {
			dest.set(b.value);
			return dest;
		}

		if (a.interpolation == KeyFrame.InterpolationMethod.HERMITE && b.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
			dest.set(Herp(a.value, b.value, a.outSlope, b.inSlope, (frame - a.frame), (frame - a.frame) / (b.frame - a.frame)));
			return dest;
			//return new AnimatedValue(HermiteInterpolation.calc(a.value, a.outSlope, b.value, b.inSlope, (frame - a.frame)/(b.frame - a.frame)));

			//Not sure if the methods are the same
		}

		return dest.set(lerp(a.value, b.value, MathEx.clamp(0f, 1f, (frame - a.frame) / (b.frame - a.frame))));
	}

	public static float lerp(float lhs, float rhs, float weight) {
		return lhs * (1 - weight) + rhs * weight;
	}

	/*
	Taken from SPICA, but ...
	
	This method is actually... bad.. ?
	It produces unnatural results when an animation is slowed down because of the Diff factor...
	...but the factor is seemingly quite important on some animations.
	 */
	public static float Herp(float LHS, float RHS, float LS, float RS, float Diff, float Weight) {
		float Result;

		Result = LHS + (LHS - RHS) * (2 * Weight - 3) * Weight * Weight; //(2 * t3 - 3 * t2 + 1) * p0; + (-2 * t3 + 3 * t2) * p1
		Result += (Diff * (Weight - 1)) * (LS * (Weight - 1) + RS * Weight);

		return Result;
	}

	public static KeyFrame getNearKeyFrame(List<KeyFrame> l, float frame, boolean backwards) {
		if (!backwards) {
			for (int i = 0; i < l.size(); i++) {
				if (l.get(i).frame >= frame) {
					return l.get(i);
				}
			}
			if (!l.isEmpty()) {
				return l.get(l.size() - 1);
			}
		} else {
			for (int i = l.size() - 1; i >= 0; i--) {
				if (l.get(i).frame <= frame) {
					return l.get(i);
				}
			}
			if (!l.isEmpty()) {
				return l.get(0);
			}
		}
		return null;
	}

}
