package ctrmap.renderer.scene;

import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;

public class Camera implements NamedResource {

	public String name = "Camera";
	
	public Mode mode = Mode.PERSPECTIVE;

	protected Matrix4 cachedTransformMatrix = null;

	public Vec3f translation = new Vec3f();
	
	public Vec3f rotation = new Vec3f();

	public Vec3f lookAtTarget = new Vec3f();
	public Vec3f lookAtUpVec = new Vec3f(0f, 1f, 0f);

	public float FOV = RenderSettings.Defaults.FOV;
	public float zNear = RenderSettings.Defaults.Z_NEAR;
	public float zFar = RenderSettings.Defaults.Z_FAR;
	
	public MetaData metaData = new MetaData();
	
	public Camera(){
		
	}
	
	public Camera(Camera src){
		copy(src);
	}
	
	public void copy(Camera src){
		name = src.name;
		mode = src.mode;
		translation = new Vec3f(src.translation);
		rotation = new Vec3f(src.rotation);
		lookAtTarget = new Vec3f(src.lookAtTarget);
		lookAtUpVec = new Vec3f(src.lookAtUpVec);
		FOV = src.FOV;
		zFar = src.zFar;
		zNear = src.zNear;
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

	public static enum Mode {
		PERSPECTIVE,
		ORTHO,
		LOOKAT
	}
	
	public Vec3f getRotationEulerToDir(){
		return getRotation().getDirFromEulersDegYXZ(new Vec3f());
	}
	
	public Vec3f getRotation() {
		switch (mode) {
			case ORTHO:
				return new Vec3f(90f, 0f, 0f);
			case LOOKAT:
				return getTransformMatrix(false).getRotation().mul(MathEx.RADIANS_TO_DEGREES);
		}
		return rotation;
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
			if (null != mode) switch (mode) {
				case PERSPECTIVE:
					mtx.translate(translation);
					mtx.rotateZYXDeg(rotation.z, rotation.y, rotation.x);
					//mtx.rotateYXZ(toRadians(rotation.y), toRadians(rotation.x), toRadians(rotation.z));
					break;
				case ORTHO:
					mtx.rotate(toRadians(90f), 1.0f, 0f, 0f);
					break;
				case LOOKAT:
					mtx.lookAt(translation, lookAtTarget, lookAtUpVec).invert();
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
		return (float)(2 * Math.atan(Math.tan(fovy * 0.5f) * aspectWH));
	}
	
	public static float fovXToFovY(float fovx, float aspectWH) {
		return (float)(2 * Math.atan(Math.tan(fovx * 0.5f) / aspectWH));
	}

	public Matrix4 getProjectionMatrix(ViewportInfo vi) {
		Matrix4 mtx = new Matrix4();
		int height = vi.surfaceDimensions.height;
		int width = vi.surfaceDimensions.width;
		if (mode != Mode.ORTHO) {
			mtx.setPerspective(toRadians(FOV), (float) width / (float) height, zNear, zFar);
		} else {
			float aspect = vi.getAspectRatio();
			float halfW = translation.y / 2f;
			float halfH = halfW * aspect;
			mtx.setOrtho(translation.x - halfW, translation.x + halfW, translation.z + halfH, translation.z - halfH, zFar, -zFar);
		}
		return mtx;
	}

	public void makeEncompassOrtho(float top, float bottom, float left, float right, ViewportInfo vi) {
		mode = Mode.ORTHO;
		float w = right - left;
		float h = bottom - top;
		translation.x = left + w / 2f;
		translation.z = top + h / 2f;
		//ty -> we need both 2 * halfW and 2 * halfH to be at least the window dim
		float aspect = h / w;
		float viewportAspect = vi.getAspectRatio();

		if (aspect > viewportAspect) {
			translation.y = h / viewportAspect;
		} else {
			translation.y = w;
		}
	}

	private static final float DEGREES_TO_RADIANS_F = 0.017453292519943295f;

	private static float toRadians(float deg) {
		return deg * DEGREES_TO_RADIANS_F;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Camera mode: ").append(mode).append("\n");
		sb.append("Translation: ").append(translation).append("\n");
		sb.append("Rotation: ").append(rotation).append("\n");
		sb.append("FOV: ").append(FOV);
		sb.append("Near: ").append(zNear).append(" | Far: ").append(zFar);
		return sb.toString();
	}
}
