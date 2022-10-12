package ctrmap.renderer.util;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;

public class G3DUtils {

	public static float getMaxModelDimXZ(G3DResource model) {
		return Math.max(model.boundingBox.max.z - model.boundingBox.min.z, model.boundingBox.max.x - model.boundingBox.min.x);
	}

	public static float getMaxModelDimXYZ(G3DResource model) {
		return model.boundingBox.getDimensions().getHighestAbsComponent();
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
			float far = getMaxModelDimXYZ(resource);
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
			float far = getMaxModelDimXYZ(resource);
			return Math.max(far * 10f, minimum);
		}
		return minimum;
	}
}
