package ctrmap.formats.generic.collada.structs;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.camera.CameraLookAtBoneTransform;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec2f;
import xstandard.text.StringEx;
import java.util.List;
import org.w3c.dom.Element;

public class DAEChannel implements DAEIDAble {

	public static boolean DAECHAN_SPLINE_ENABLE = true;

	public String targetBone;
	public String targetTransform;

	public float timeMax = 0f;

	public float[] frameTimes;
	public String[] interpolations;
	public Vec2f[] inTangents;
	public Vec2f[] outTangents;

	public DAESource curve;

	public CameraLookAtBoneTransform camTransform = new CameraLookAtBoneTransform();
	public SkeletalBoneTransform sklTransform = new SkeletalBoneTransform();
	public VisibilityBoneTransform visTransform = new VisibilityBoneTransform();

	private static final float _1_30 = 1f / 30f;

	public DAEChannel(Matrix4[] matrices, String targetBone, String targetTransform, String sourceLabel) {
		this.targetBone = targetBone;
		this.targetTransform = targetTransform;
		frameTimes = new float[matrices.length];
		interpolations = new String[matrices.length];

		for (int i = 0; i < matrices.length; i++) {
			frameTimes[i] = i * _1_30;
			interpolations[i] = "LINEAR";
		}

		curve = new DAESource(matrices, sourceLabel);
	}

	public DAEChannel(KeyFrameList kfl, String targetBone, String targetTransform, boolean toDegrees, String... sourceLabels) {
		this.targetBone = targetBone;
		this.targetTransform = targetTransform;
		frameTimes = new float[kfl.size()];
		interpolations = new String[kfl.size()];

		float[] values = new float[kfl.size()];
		for (int i = 0; i < kfl.size(); i++) {
			KeyFrame kf = kfl.get(i);
			frameTimes[i] = (kfl.startFrame + kf.frame) * _1_30;
			interpolations[i] = getInterpStr(kf.interpolation);
			values[i] = kf.value;
			if (toDegrees) {
				values[i] *= MathEx.RADIANS_TO_DEGREES;
			}
		}

		curve = new DAESource(values, sourceLabels);

		if (kfl.getInterpolationHighest() == KeyFrame.InterpolationMethod.HERMITE) {
			inTangents = new Vec2f[kfl.size()];
			outTangents = new Vec2f[kfl.size()];

			float lastTime = 0f;
			float nextTime;
			for (int i = 0; i < kfl.size(); i++) {
				KeyFrame kf = kfl.get(i);
				nextTime = (i + 1 < kfl.size()) ? kfl.get(i + 1).frame : kfl.lastFrameTime();
				if (kf.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
					inTangents[i] = bezierTangentFromHermiteSlopeIn(kf.frame, lastTime, kf.value, kf.inSlope);
					outTangents[i] = bezierTangentFromHermiteSlopeOut(kf.frame, nextTime, kf.value, kf.outSlope);
					if (toDegrees) {
						inTangents[i].y *= MathEx.RADIANS_TO_DEGREES;
						outTangents[i].y *= MathEx.RADIANS_TO_DEGREES;
					}
				} else {
					inTangents[i] = new Vec2f();
					outTangents[i] = new Vec2f();
				}
				lastTime = kf.frame;
			}
		}
	}

	public DAEChannel(Element elem, DAEDict<DAESource> sources, DAEDict<DAESampler> samplers) {
		String source = elem.getAttribute("source");
		String[] target = StringEx.splitOnecharFast(elem.getAttribute("target"), '/');
		targetBone = target[0];
		if (target.length > 1) {
			targetTransform = target[1];
		} else {
			targetTransform = "";
		}
		//System.out.println("tgt " + targetTransform);

		DAESampler samp = samplers.getByUrl(source);

		frameTimes = sources.getByUrl(samp.inputs.get("INPUT").sourceUrl).accessor.getFloatArray();
		interpolations = sources.getByUrl(samp.inputs.get("INTERPOLATION").sourceUrl).accessor.getStringArray();
		inTangents = null;
		outTangents = null;

		DAEInput inTangentInput = samp.inputs.get("IN_TANGENT");
		DAEInput outTangentInput = samp.inputs.get("OUT_TANGENT");
		if (inTangentInput != null) {
			inTangents = sources.getByUrl(inTangentInput.sourceUrl).accessor.getVec2fArray();
		}
		if (outTangentInput != null) {
			outTangents = sources.getByUrl(outTangentInput.sourceUrl).accessor.getVec2fArray();
		}

		for (float ft : frameTimes) {
			timeMax = Math.max(ft, timeMax);
		}

		DAEInput output = samp.inputs.get("OUTPUT");

		curve = sources.getByUrl(output.sourceUrl);

		DAEAccessor curveAccessor = curve.accessor;

		CurveInfo ci = new CurveInfo();
		ci.timestamps = frameTimes;
		ci.interpolations = interpolations;
		ci.inTangents = inTangents;
		ci.outTangents = outTangents;
		if (inTangentInput != null) {
			ci.tangentsIsKeyValue = sources.getByUrl(inTangentInput.sourceUrl).accessor.hasParams("X", "Y");
		}

		Vec3f destT = new Vec3f();
		Vec3f destR = new Vec3f();
		Vec3f destS = new Vec3f();

		if (curveAccessor.hasParamTypes(DAEAccessor.ParamFormat.FLOAT4x4)) {
			List<Matrix4> matrices = curveAccessor.getMatrix4Array();
			for (int i = 0; i < Math.min(matrices.size(), frameTimes.length); i++) {
				Matrix4 mtx = matrices.get(i);

				mtx.getTranslation(destT);
				mtx.getScale(destS);
				mtx.getRotationTo(destR);

				sklTransform.pushFullBakedFrame(frameTimes[i] * 30, destT, destR, destS);
			}
		} else if (curveAccessor.hasParams("X", "Y", "Z")) {
			List<Vec3f> vectors = curveAccessor.getVec3fArray();
			int max = Math.min(vectors.size(), frameTimes.length);
			for (int i = 0; i < max; i++) {
				Vec3f vector = vectors.get(i);
				switch (targetTransform) {
					case "translate":
					case "location":
						sklTransform.tx.add(makeKeyframe(i, vector.x, ci));
						sklTransform.ty.add(makeKeyframe(i, vector.y, ci));
						sklTransform.tz.add(makeKeyframe(i, vector.z, ci));
						break;
					case "rotate":
						sklTransform.rx.add(makeKeyframe(i, vector.x, ci, true));
						sklTransform.ry.add(makeKeyframe(i, vector.y, ci, true));
						sklTransform.rz.add(makeKeyframe(i, vector.z, ci, true));
						break;
					case "scale":
						sklTransform.sx.add(makeKeyframe(i, vector.x, ci));
						sklTransform.sy.add(makeKeyframe(i, vector.y, ci));
						sklTransform.sz.add(makeKeyframe(i, vector.z, ci));
						break;
					case "xfov":
					case "yfov":
						//For whatever reason, blender writes these as vec3
						camTransform.fov.add(makeKeyframe(i, vector.x, ci));
						break;
					case "":
						visTransform.isVisible.add(new KeyFrame(frameTimes[i] * 30f, vector.x == 0f ? 1f : 0f, 0f, 0f, KeyFrame.InterpolationMethod.STEP));
						break;
				}
			}
		} else if (curveAccessor.hasParams("ANGLE")) {
			float[] angles = curveAccessor.getFloatArray();
			int max = Math.min(angles.length, frameTimes.length);
			for (int i = 0; i < max; i++) {
				float angledeg = angles[i];
				switch (targetTransform) {
					case "rotateX.ANGLE":
					case "rotationX.ANGLE":
						sklTransform.rx.add(makeKeyframe(i, angledeg, ci, true));
						break;
					case "rotateY.ANGLE":
					case "rotationY.ANGLE":
						sklTransform.ry.add(makeKeyframe(i, angledeg, ci, true));
						break;
					case "rotateZ.ANGLE":
					case "rotationZ.ANGLE":
						sklTransform.rz.add(makeKeyframe(i, angledeg, ci, true));
						break;
				}
			}
		} else {
			if (curveAccessor.hasParamTypes(DAEAccessor.ParamFormat.FLOAT)) {
				float[] values = curveAccessor.getFloatArray();
				for (int i = 0; i < Math.min(frameTimes.length, values.length); i++) {
					float value = values[i];
					if (targetTransform.equals("")) {
						visTransform.isVisible.add(new KeyFrame(i, value == 0f ? 1f : 0f, 0f, 0f, KeyFrame.InterpolationMethod.STEP));
					} else {
						KeyFrameList destKFL = null;
						switch (targetTransform) {
							case "location.X":
								destKFL = sklTransform.tx;
								break;
							case "location.Y":
								destKFL = sklTransform.ty;
								break;
							case "location.Z":
								destKFL = sklTransform.tz;
								break;
							case "scale.X":
								destKFL = sklTransform.sx;
								break;
							case "scale.Y":
								destKFL = sklTransform.sy;
								break;
							case "scale.Z":
								destKFL = sklTransform.sz;
								break;
							case "xfov":
							case "yfov":
								destKFL = camTransform.fov;
								break;
							case "znear":
								destKFL = camTransform.zNear;
								break;
							case "zfar":
								destKFL = camTransform.zFar;
								break;
							case "lookat.Px":
								destKFL = camTransform.tx;
								break;
							case "lookat.Py":
								destKFL = camTransform.ty;
								break;
							case "lookat.Pz":
								destKFL = camTransform.tz;
								break;
							case "lookat.Ix":
								destKFL = camTransform.targetTX;
								break;
							case "lookat.Iy":
								destKFL = camTransform.targetTY;
								break;
							case "lookat.Iz":
								destKFL = camTransform.targetTZ;
								break;
							case "lookat.UPx":
								destKFL = camTransform.upX;
								break;
							case "lookat.UPy":
								destKFL = camTransform.upY;
								break;
							case "lookat.UPz":
								destKFL = camTransform.upZ;
								break;
						}
						if (destKFL != null) {
							destKFL.add(makeKeyframe(i, value, ci));
						}
					}
				}
			}
		}
	}

	private static String getInterpStr(KeyFrame.InterpolationMethod interp) {
		switch (interp) {
			case HERMITE:
				return "BEZIER"; //convert to bezier!!
			case LINEAR:
				return "LINEAR";
			case STEP:
				return "STEP";
		}
		throw new RuntimeException();
	}

	@Override
	public void setID(String id) {
		this.targetTransform = id;
	}

	private static KeyFrame makeKeyframe(int frameIndex, float value, CurveInfo ci) {
		return makeKeyframe(frameIndex, value, ci, false);
	}

	private static KeyFrame makeKeyframe(int frameIndex, float value, CurveInfo ci, boolean toRadians) {
		float time = ci.timestamps[frameIndex] * 30;
		if (toRadians) {
			value *= MathEx.DEGREES_TO_RADIANS;
		}

		float inSlope = 0f;
		float outSlope = 0f;
		KeyFrame.InterpolationMethod interp = KeyFrame.InterpolationMethod.LINEAR;

		String interpType = ci.interpolations[frameIndex];
		if (!DAECHAN_SPLINE_ENABLE) {
			if (!interpType.equals("STEP")) {
				interpType = "LINEAR";
			}
		}

		switch (interpType) {
			case "LINEAR":
				interp = KeyFrame.InterpolationMethod.LINEAR;
				break;
			/*
				Warning: Bezier and Hermite interpolations are most likely broken.
				I only had a handful of sample source data from Blender, and apparently their exporter is faulty
				in a way that all curves essentially evaluate to a slope along the X axis. This can even be observed simply by
				reimporting the COLLADA scene back to Blender.
				
				Until there is an actual working non-commercial bezier/hermite exporter, this will remain broken.
			 */
			case "BEZIER":
				interp = KeyFrame.InterpolationMethod.HERMITE;

				outSlope = ci.getOutSlope(frameIndex, value, false, toRadians);
				inSlope = ci.getInSlope(frameIndex, value, false, toRadians);

				break;
			case "HERMITE":
				interp = KeyFrame.InterpolationMethod.HERMITE;

				outSlope = ci.getOutSlope(frameIndex, value, true, toRadians);
				inSlope = ci.getInSlope(frameIndex, value, true, toRadians);

				break;
			case "STEP":
				interp = KeyFrame.InterpolationMethod.STEP;
				break;
		}

		KeyFrame kf = new KeyFrame(time, value, inSlope, outSlope, interp);
		//System.out.println("MKKEY @" + time + ": " + value + " INT " + inSlope + " OUTT " + outSlope);
		return kf;
	}

	private static final float _1_3 = 1f / 3f;

	private static Vec2f bezierTangentFromHermiteSlopeOut(float timeInFrames, float nextTimeInFrames, float value, float slope) {
		//always calculate from 1 frame after
		float diff23 = (nextTimeInFrames - timeInFrames) * _1_3;
		float tgtDiff = slope * diff23; //1/30th of a second
		float controlPoint = value + tgtDiff;
		Vec2f out = new Vec2f();
		out.y = controlPoint;
		out.x = (timeInFrames + diff23) * _1_30;
		return out;
	}

	private static Vec2f bezierTangentFromHermiteSlopeIn(float timeInFrames, float lastTimeInFrames, float value, float slope) {
		//always calculate from 1 frame before
		float diff23 = (timeInFrames - lastTimeInFrames) * _1_3;
		float tgtDiff = slope * diff23; //1/30th of a second
		float controlPoint = value - tgtDiff;
		Vec2f out = new Vec2f();
		out.y = controlPoint;
		out.x = (timeInFrames - diff23) * _1_30;
		return out;
	}

	private static class CurveInfo {

		public float[] timestamps;

		public String[] interpolations;

		public Vec2f[] inTangents;
		public Vec2f[] outTangents;

		public boolean tangentsIsKeyValue;

		public float getInSlope(int index, float value, boolean isHermite, boolean toRadians) {
			return getSlope(index, value, false, timestamps, inTangents, outTangents, isHermite, toRadians);
		}

		public float getOutSlope(int index, float value, boolean isHermite, boolean toRadians) {
			return getSlope(index, value, true, timestamps, outTangents, inTangents, isHermite, toRadians);
		}

		private static final float _1_3 = 1f / 3f;

		private static float hermite2BezierIn(float hermTgt, float hermValue) {
			return hermValue - hermTgt * _1_3;
		}

		private static float hermite2BezierOut(float hermTgt, float hermValue) {
			return hermTgt * _1_3 + hermValue;
		}

		private static float hermite2Bezier(float hermTgt, float hermValue, boolean isOut) {
			return isOut ? hermite2BezierOut(hermTgt, hermValue) : hermite2BezierIn(hermTgt, hermValue);
		}

		private static float getSlope(int index, float point, boolean isOut, float[] timestamps, Vec2f[] src1, Vec2f[] src2, boolean isHermite, boolean toRadians) {
			float controlPoint = 0f;
			float timeTgt = 0f;
			if (src1 != null) {
				controlPoint = src1[index].y;
				timeTgt = src1[index].x;
			} else if (src2 != null) {
				controlPoint = src2[index].y;
				timeTgt = src2[index].x;
			}
			if (isHermite) {
				controlPoint = hermite2Bezier(controlPoint, point, isOut);
			}
			if (toRadians) {
				controlPoint *= MathEx.DEGREES_TO_RADIANS;
			}
			float timeVal = timestamps[index];
			float timeDiff = isOut ? (timeTgt - timeVal) : (timeVal - timeTgt);
			float tgtDiff = isOut ? (controlPoint - point) : (point - controlPoint);
			float out = tgtDiff / (timeDiff * 30f);
			//System.out.println("tangent [" + timeTgt + ";" + controlPoint + "] value [" + timeVal + ";" + point + "] -> slope " + out);
			return out;
		}
	}

	public boolean isVisibility() {
		return targetTransform.equals("");
	}

	public boolean isCamera() {
		if (targetTransform.startsWith("lookat.")) {
			return true;
		}
		switch (targetTransform) {
			case "xfov":
			case "yfov":
			case "znear":
			case "zfar":
				return true;
		}
		return false;
	}

	@Override
	public String getID() {
		return targetTransform;
	}
}
