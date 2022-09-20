package ctrmap.renderer.util;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;

public class G3DUtils {

	public static float getMaxModelDimXZ(G3DResource model) {
		return Math.max(model.maxVector.z - model.minVector.z, model.maxVector.x - model.minVector.x);
	}

	public static float getMaxModelDimXYZ(G3DResource model) {
		return getMaxModelDimXYZ(model.minVector, model.maxVector);
	}

	public static float getMaxModelDimXYZ(Vec3f minVector, Vec3f maxVector) {
		return Math.max(maxVector.z - minVector.z, Math.max(maxVector.x - minVector.x, maxVector.y - minVector.y));
	}

	public static float getAvgModelDimXYZ(Vec3f minVector, Vec3f maxVector) {
		return ((maxVector.z - minVector.z) + (maxVector.x - minVector.x) + (maxVector.y - minVector.y)) / 3f;
	}
	
	public static float getIdealNearClipDistance(G3DResourceInstance i, float minimum) {
		if (i != null) {
			return getIdealNearClipDistance(i.resource, minimum);
		}
		return minimum;
	}
	
	public static float getIdealNearClipDistance(G3DResource resource) {
		return getIdealNearClipDistance(resource, 1f);
	}

	public static float getIdealNearClipDistance(G3DResource resource, float minimum) {
		if (resource != null) {
			float far = Math.max(Math.max(resource.maxVector.x - resource.minVector.x, resource.maxVector.y - resource.minVector.y), resource.maxVector.z - resource.minVector.z);
			return Math.max(minimum, far / 300f);
		}
		return minimum;
	}

	public static float getIdealFarClipDistance(G3DResourceInstance i, float minimum) {
		if (i != null) {
			return getIdealFarClipDistance(i.resource, minimum);
		}
		return minimum;
	}

	public static float getIdealFarClipDistance(G3DResource resource) {
		return getIdealFarClipDistance(resource, 3000f);
	}
	
	public static float getIdealFarClipDistance(G3DResource resource, float minimum) {
		if (resource != null) {
			float far = Math.max(Math.max(resource.maxVector.x - resource.minVector.x, resource.maxVector.y - resource.minVector.y), resource.maxVector.z - resource.minVector.z);
			return Math.max(far * 10f, minimum);
		}
		return minimum;
	}
}
