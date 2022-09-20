package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.G3DUtils;
import xstandard.math.vec.Vec3f;

/**
 *
 */
public class DimensionalGizmoModel extends ModelInstance {

	private WorldObjInstanceAdapter adapter;
	private boolean isEnd;

	public DimensionalGizmoModel(WorldObjInstanceAdapter a, G3DResource res, boolean isEnd) {
		this.adapter = a;
		setResource(res);
		this.isEnd = isEnd;
	}

	@Override
	public Vec3f getScale() {
		Vec3f scale = adapter.getScale();
		float maxScale = Math.abs(scale.x * scale.z); //can not make a square root of a negative number
		float gizmoScale = (float)Math.pow(Math.sqrt(maxScale), 0.732f) / G3DUtils.getMaxModelDimXZ(resource);
		gizmoScale = Math.max(0.1f, gizmoScale);
		return new Vec3f(gizmoScale);
	}

	@Override
	public Vec3f getPosition() {
		Vec3f objWDim = adapter.getScale();
		float y = objWDim.y / 2f;
		Vec3f v;
		if (!isEnd) {
			v = new Vec3f(0, y, 0);
		} else {
			v = new Vec3f(objWDim.x, y, objWDim.z);
		}
		return v;
	}
}
