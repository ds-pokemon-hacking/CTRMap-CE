package ctrmap.formats.common.collision;

import ctrmap.renderer.scenegraph.G3DResource;
import java.util.List;

public abstract class CollisionFile {

	public boolean modified = false;

	public abstract List<? extends ICollisionMesh> getMeshes();

	public abstract G3DResource getModel();

	public abstract void write();

	protected abstract void replaceImpl(G3DResource res);

	public void replace(G3DResource res) {
		modified = true;
		replaceImpl(res);
	}

	public boolean hasHeightAtPoint(float x, float z) {
		for (ICollisionMesh mesh : getMeshes()) {
			for (Triangle tri : mesh.getTriangleList()) {
				if (tri.containsXZ(x, z)){
					return true;
				}
			}
		}
		return false;
	}

	public float getHeightAtPoint(float x, float y, float z) {
		//just check all the tris, if they are deduped, GF's hacks are useless
		float result = -Float.MAX_VALUE;
		float lowestDiff = Float.MAX_VALUE;
		boolean found = false;
		for (ICollisionMesh mesh : getMeshes()) {
			for (Triangle tri : mesh.getTriangleList()) {
				if (tri.containsXZ(x, z)) {
					float check = tri.getYAtXZ(x, z);
					float diff = Math.abs(check - y);
					if (diff < lowestDiff) {
						result = check;
						lowestDiff = diff;
					}
					found = true;
				}
			}
		}
		return found ? result : 0f;
	}
}
