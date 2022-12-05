package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.formats.common.collision.ICollisionProvider;
import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.renderer.backends.base.AbstractBackend;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.util.ConstYPlaneRayCaster;
import java.awt.event.MouseEvent;

public class DragStatus {

	private WorldObjInstanceAdapter adapter;
	private WorldObject obj;
	private ICollisionProvider map;
	private Scene scene;
	private DragType type;

	private float currentConstPlaneHeight;
	private Vec3f diffVector;

	private float heightOffset;

	private Vec3f cachedCenterVector;

	public DragStatus(WorldObjInstanceAdapter adapter, ICollisionProvider map, Scene context, DragType type) {
		this.obj = adapter.obj;
		this.adapter = adapter;
		this.type = type;
		this.scene = context;
		this.map = map;
		cachedCenterVector = adapter.resource.getCenterVector();
		switch (type) {
			case GIZMO_DIM:
				heightOffset = adapter.dimGizmo_BR.getPosition().y;
				break;
			case GIZMO_POSDIM:
				heightOffset = adapter.dimGizmo_TL.getPosition().y;
				break;
			case OBJECT:
				heightOffset = 0f;
				break;
		}
	}

	public void beginDrag(MouseEvent e, AbstractBackend backend) {
		updateConstPlaneHeight();
		ViewportInfo vi = backend.getViewportInfo();
		Vec3f constPlaneContact = ConstYPlaneRayCaster.getConstYPlaneIntersectMouse(
			e,
			currentConstPlaneHeight,
			scene.getTransformMatrix().getMatrix(),
			scene.getAbsoluteProjectionMatrix().getMatrix(),
			vi.getViewportMatrix()
		);
		setDiffVector(constPlaneContact, getDiffVectorPoint());
	}

	public void updateDrag(MouseEvent e, AbstractBackend backend) {
		float[] mtxMv = scene.getTransformMatrix().getMatrix();
		ViewportInfo vi = backend.getViewportInfo();
		float[] mtxProj = scene.getAbsoluteProjectionMatrix().getMatrix();
		int[] mtxVp = vi.getViewportMatrix();

		Vec3f oldWPos = obj.getWPos();
		Vec3f oldDim = obj.getWDim();

		Vec3f oldConstPlaneContact = ConstYPlaneRayCaster.getConstYPlaneIntersectMouse(e, currentConstPlaneHeight, mtxMv, mtxProj, mtxVp);
		Vec3f newObjPos = oldConstPlaneContact.add(diffVector);
//		System.out.println(newObjPos);

		switch (type) {
			case OBJECT:
				obj.setWPos(newObjPos);
				//Some objects have position very far off from the actual model (f.e. regiruins)
				//For that reason, we shall calculate Y based on bbox center
				//Note that procedural bboxes are unaffected
				Vec3f ySource = new Vec3f(obj.getWPos());
				ySource.add(cachedCenterVector.clone().mul(adapter.getScale()));
				newObjPos.y = map.getHeightAtWorldLoc(ySource.x, newObjPos.y, ySource.z);
				obj.setWPos(newObjPos);
				//System.out.println("req y " + oldWPos.y + " got " + newObjPos.y);
				break;
			case GIZMO_POSDIM:
				//We do this because some InstanceAdapter implementations set dimensions with a fixed aspect ratio and stuff and that has to be taken into account
				Vec3f endPos = new Vec3f(oldWPos);
				endPos.add(obj.getMinVector());
				endPos.add(oldDim);

				obj.setWDim(new Vec3f(oldDim.x + (oldWPos.x - newObjPos.x), oldDim.y, oldDim.z + (oldWPos.z - newObjPos.z)));
				obj.setWPos(new Vec3f(obj.getWDim()).invert().add(endPos));
				break;
			case GIZMO_DIM:
				obj.setWDim(new Vec3f(newObjPos.x - oldWPos.x, oldDim.y, newObjPos.z - oldWPos.z));
				break;
		}

		updateConstPlaneHeight();
		Vec3f newConstPlaneContact = ConstYPlaneRayCaster.getConstYPlaneIntersectMouse(e, currentConstPlaneHeight, mtxMv, mtxProj, mtxVp);
		setDiffVector(newConstPlaneContact, newObjPos);
	}

	private Vec3f getDiffVectorPoint() {
		switch (type) {
			case GIZMO_POSDIM:
			case OBJECT:
				return obj.getWPos();
			case GIZMO_DIM:
				Vec3f endPoint = new Vec3f(obj.getWPos());
				Vec3f dim = obj.getWDim();
				endPoint.add(dim);
				/*Dimension dim = obj.getGDimensions();
				pos.add(new Vec3f(TilemapMath.tileToWorldCentered(dim.width), 0f, TilemapMath.tileToWorldCentered(dim.height)));*/
				return endPoint;
		}
		return null;
	}

	public void updateConstPlaneHeight() {
		currentConstPlaneHeight = obj.getWPos().y + heightOffset;
	}

	public void setDiffVector(Vec3f constPlaneCollisionVec, Vec3f objVec) {
		Vec3f inverseCollVec = new Vec3f(constPlaneCollisionVec).invert();
		diffVector = new Vec3f(objVec);
		diffVector.translate(inverseCollVec);
	}

	public Vec3f getObjPos() {
		return obj.getWPos();
	}

	public static enum DragType {
		OBJECT,
		GIZMO_POSDIM,
		GIZMO_DIM
	}
}
