package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.formats.common.collision.ICollisionProvider;
import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.WorldObject;

public class DynamicHeightInstanceAdapter extends WorldObjInstanceAdapter {

	private ICollisionProvider map;

	private float lastY = 0f;

	public DynamicHeightInstanceAdapter(WorldObject obj, float unitSize, MaterialProvider matProvider, ICollisionProvider map) {
		super(unitSize);
		this.map = map;
		createFromObj(obj, matProvider);
	}

	@Override
	public Vec3f getPosition() {
		Vec3f posAtEnd = getHeightedPosAtEnd();
		Vec3f posBaseHeighted = getHeightedPos();

		if (posAtEnd.y < posBaseHeighted.y) {
			posBaseHeighted.y = posAtEnd.y;
		}

		posBaseHeighted.y += obj.getCmOffset();

		return posBaseHeighted;
	}

	protected Vec3f getBasePosition() {
		return super.getPosition();
	}

	@Override
	public Vec3f getScale() {
		Vec3f posBase = getHeightedPos();
		Vec3f posAtEnd = getHeightedPosAtEnd();
		float scaleY = Math.abs(posAtEnd.y - posBase.y) + 50f;
		Vec3f baseScale = super.getScale();
		return new Vec3f(baseScale.x, scaleY, baseScale.z);
	}

	private float getHeight(float x, float y, float z) {
		float v = map.getHeightAtWorldLoc(x, Float.MAX_VALUE / 2f, z);
		if (v >= Float.MAX_VALUE / 2f) {
			v = y;
		}
		lastY = v;
		return v;
	}

	public Vec3f getHeightedPos() {
		Vec3f posBase = new Vec3f(getBasePosition());
		//posBase.add(new Vec3f(obj.getMinVector()));
		Vec3f posBaseHeighted = new Vec3f(posBase.x, getHeight(posBase.x, lastY, posBase.z), posBase.z); //force highest Y
		return posBaseHeighted;
	}

	public Vec3f getHeightedPosAtEnd() {
		Vec3f posAtEnd = getHeightedPos();
		Vec3f dim = obj.getWDim();
		posAtEnd.x += dim.x;
		posAtEnd.z += dim.z;
		posAtEnd.y = getHeight(posAtEnd.x, lastY, posAtEnd.z);
		return posAtEnd;
	}
}
