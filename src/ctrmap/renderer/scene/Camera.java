package ctrmap.renderer.scene;

import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import xstandard.math.vec.Vec3f;

public class Camera implements NamedResource {

	public String name = "Camera";

	public ProjectionMode projMode = ProjectionMode.PERSPECTIVE;
	public ViewMode viewMode = ViewMode.ROTATE;

	protected Matrix4 cachedTransformMatrix = null;

	//rotate view
	public Vec3f translation = new Vec3f();
	public Vec3f rotation = new Vec3f();
	public Quaternion rotQuat; //ugly workaround for animation. should really be rewritten

	//lookat view
	public Vec3f lookAtTarget = new Vec3f();
	public Vec3f lookAtUpVec = new Vec3f(0f, 1f, 0f);

	//perspective projection
	public float FOV = RenderSettings.Defaults.FOV;
	public float aspect = 16f / 9f;

	//ortho projection
	public float top;
	public float bottom;
	public float left;
	public float right;

	public float zNear = RenderSettings.Defaults.Z_NEAR;
	public float zFar = RenderSettings.Defaults.Z_FAR;

	public MetaData metaData = new MetaData();

	public Camera() {

	}

	public Camera(Camera src) {
		copy(src);
	}

	public void copyProjection(Camera src) {
		projMode = src.projMode;
		FOV = src.FOV;
		aspect = src.aspect;
		top = src.top;
		bottom = src.bottom;
		left = src.left;
		right = src.right;
		zFar = src.zFar;
		zNear = src.zNear;
	}

	public void copyView(Camera src) {
		viewMode = src.viewMode;
		translation = new Vec3f(src.translation);
		rotation = new Vec3f(src.rotation);
		lookAtTarget = new Vec3f(src.lookAtTarget);
		lookAtUpVec = new Vec3f(src.lookAtUpVec);
	}

	public void copy(Camera src) {
		name = src.name;
		copyProjection(src);
		copyView(src);

		metaData = new MetaData(src.metaData);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public static enum ProjectionMode {
		PERSPECTIVE,
		ORTHO
	}

	public static enum ViewMode {
		ROTATE,
		LOOK_AT
	}

	public Vec3f getRotationEulerToDir() {
		return getRotation().getDirFromEulersDegYXZ(new Vec3f());
	}

	public Vec3f getRotation() {
		switch (viewMode) {
			case LOOK_AT:
				return getTransformMatrix(false).getRotation().mul(MathEx.RADIANS_TO_DEGREES);
			case ROTATE:
			default:
				return rotation;
		}
	}

	public void calcTransformMatrix() {
		getTransformMatrix(false);
	}

	public Matrix4 getTransformMatrix() {
		return getTransformMatrix(false);
	}

	public Matrix4 getTransformMatrix(boolean useCache) {
		if (!useCache || cachedTransformMatrix == null) {
			//System.out.println(rotation);
			Matrix4 mtx = new Matrix4();
			switch (viewMode) {
				case ROTATE:
					mtx.translation(translation);
					if (rotQuat != null) {
						mtx.rotate(rotQuat);
					} else {
						mtx.rotateZYXDeg(rotation.z, rotation.y, rotation.x);
					}
					break;
				case LOOK_AT:
					mtx.setLookAt(translation, lookAtTarget, lookAtUpVec).invert();
					break;
				default:
					break;
			}
			cachedTransformMatrix = mtx;
			//System.out.println(this);
			//System.out.println(zNear + " / " + zFar);
		}
		return cachedTransformMatrix;
	}

	public static float fovYToFovX(float fovy, float aspectWH) {
		return (float) (2 * Math.atan(Math.tan(fovy * 0.5f) * aspectWH));
	}

	public static float fovXToFovY(float fovx, float aspectWH) {
		return (float) (2 * Math.atan(Math.tan(fovx * 0.5f) / aspectWH));
	}

	public Matrix4 getProjectionMatrix() {
		return getProjectionMatrix(new Matrix4());
	}

	public Matrix4 getProjectionMatrix(Matrix4 dest) {
		if (projMode == ProjectionMode.PERSPECTIVE) {
			dest.setPerspective(toRadians(FOV), aspect, zNear, zFar);
		} else {
			dest.setOrtho(left, right, bottom, top, -zFar, zFar);
		}
		return dest;
	}

	public void setDefaultOrtho() {
		projMode = ProjectionMode.ORTHO;
		rotation.set(-90f, 0f, 0f);
		translation.zero();
	}

	public void makeZoomOrtho(float cx, float cz, float zoom, float aspect) {
		setDefaultOrtho();
		float halfW = zoom * 0.5f;
		float halfH = halfW / aspect;
		left = cx - halfW;
		right = cx + halfW;
		top = -(cz - halfH);
		bottom = -(cz + halfH);
	}

	public void makeEncompassOrtho(float top, float bottom, float left, float right, float surfaceAspect) {
		setDefaultOrtho();
		float w = right - left;
		float h = bottom - top;

		//ty -> we need both 2 * halfW and 2 * halfH to be at least the window dim
		float desiredAspect = w / h;
		float actualAspect = surfaceAspect;

		float actualW;

		if (desiredAspect < actualAspect) {
			actualW = h * actualAspect;
		} else {
			actualW = w;
		}

		float actualH = actualW / actualAspect;

		float halfW = actualW * 0.5f;
		float halfH = actualH * 0.5f;

		float centerX = left + w * 0.5f;
		float centerZ = top + h * 0.5f;

		this.left = centerX - halfW;
		this.right = centerX + halfW;
		this.top = -(centerZ - halfH);
		this.bottom = -(centerZ + halfH);
	}

	private static final float DEGREES_TO_RADIANS_F = 0.017453292519943295f;

	private static float toRadians(float deg) {
		return deg * DEGREES_TO_RADIANS_F;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Camera mode: ").append(projMode).append(" / ").append(viewMode).append("\n");
		sb.append("Translation: ").append(translation).append("\n");
		sb.append("Rotation: ").append(rotation).append("\n");
		sb.append("FOV: ").append(FOV);
		sb.append("Near: ").append(zNear).append(" | Far: ").append(zFar);
		return sb.toString();
	}
}
