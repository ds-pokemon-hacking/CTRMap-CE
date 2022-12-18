package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.CTRMapResources;
import ctrmap.formats.internal.CMVD;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.util.ObjectSelection;
import ctrmap.renderer.util.generators.BoundingBoxGenerator;
import java.util.HashMap;
import java.util.Map;

public class WorldObjInstanceAdapter extends ModelInstance {

	public static final String G3D_SELECTED_LINE_MAT_NAME = "SelectedGridObj";
	public static final String G3D_LINE_MAT_NAME = "GridObjLine";
	public static final String G3D_FILL_MAT_NAME = "GridObjFill";

	private static final String G3D_LINE_MESH_NAME = "GridObjLineBox";
	private static final String G3D_FILL_MESH_NAME = "GridObjFillBox";

	public static final String G3D_DIM_GIZMO_MAT_NAME = "DimGizmo";

	private static final Map<Float, Mesh[]> dimGizmoMeshes = new HashMap<>();

	protected static Mesh[] getDimGizmoMeshes(float size) {
		Mesh[] arr = dimGizmoMeshes.get(size);
		if (arr == null) {
			arr = new Mesh[]{
				BoundingBoxGenerator.generateBBox(
				size,
				size,
				size,
				true,
				false,
				0,
				RGBA.WHITE
				),
				BoundingBoxGenerator.generateBBox(
				size,
				size,
				size,
				true,
				true,
				2,
				RGBA.BLACK
				)
			};
			arr[0].renderLayer = 0;
			arr[1].renderLayer = 0;
			dimGizmoMeshes.put(size, arr);
		}
		return arr;
	}

	protected static final G3DResource dimGizmoCompassRoseResource = new CMVD(CTRMapResources.ACCESSOR.getStream("navigator/CompassRose.cmvd")).toGeneric();

	protected WorldObject obj;
	public int objId;
	public boolean isGizmoEnabled;
	public boolean isSelected;

	public DimensionalGizmoModel dimGizmo_TL;
	public DimensionalGizmoModel dimGizmo_BR;

	public float fixedDimGizmoScale = 0f;
	
	private float unitSize;

	public WorldObjInstanceAdapter(float unitSize) {
		obj = null;
		this.unitSize = unitSize;
	}

	public void setObj(WorldObject obj) {
		this.obj = obj;
	}

	private static G3DResource getGizmoModel(MaterialProvider matProvider, float unitSize) {
		Model model = new Model();
		Mesh[] meshes = getDimGizmoMeshes(unitSize);
		model.addMesh(meshes[0]);
		model.addMesh(meshes[1]);
		for (Model src : dimGizmoCompassRoseResource.models) {
			for (Mesh mesh : src.meshes) {
				model.addMesh(mesh);
				mesh.renderLayer = 0;
				mesh.materialName = G3D_DIM_GIZMO_MAT_NAME;
			}
		}

		Material mat = matProvider.getDimGizmoMaterial();
		MaterialProcessor.setAlphaBlend(mat);
		model.addMaterial(mat);

		return new G3DResource(model);
	}

	public WorldObjInstanceAdapter(WorldObject obj, float unitSize, MaterialProvider matProvider) {
		this(unitSize);
		createFromObj(obj, matProvider);
	}

	public WorldObject getObj() {
		return obj;
	}

	public void resetProvider(MaterialProvider newProvider) {
		createFromObj(obj, newProvider);
	}

	public final void createFromObj(WorldObject obj, MaterialProvider matProvider) {
		children.clear();
		this.obj = obj;

		Model model = createGenericWorldObjHelperModel();
		model.name = "WorldObjInstanceAdapterG3DResMdl";

		model.metaData.putValue(matProvider.createObjIdMDV(obj));

		Material line = matProvider.getLineMaterial();
		Material fill = matProvider.getFillMaterial();
		Material sel = matProvider.getSelectionMaterial();
		line.name = G3D_LINE_MAT_NAME;
		fill.name = G3D_FILL_MAT_NAME;
		sel.name = G3D_SELECTED_LINE_MAT_NAME;
		ObjectSelection.enableObjSelSHA(sel);
		ObjectSelection.enableObjSelSHA(fill);
		ObjectSelection.enableObjSelSHA(line);
		model.addMaterial(sel);
		model.addMaterial(fill);
		model.addMaterial(line);

		G3DResource gizmoMdl = getGizmoModel(matProvider, unitSize);
		dimGizmo_TL = new DimensionalGizmoModel(this, gizmoMdl, false);
		dimGizmo_BR = new DimensionalGizmoModel(this, gizmoMdl, true);
		dimGizmo_BR.parentMode = ParentMode.TRANSLATION_AND_ROTATION;
		dimGizmo_TL.parentMode = ParentMode.TRANSLATION_AND_ROTATION;

		ObjectIDMetaDataValue dimGizmoTLMDV = matProvider.createObjIdMDV(obj);
		dimGizmoTLMDV.type = DragStatus.DragType.GIZMO_POSDIM;
		dimGizmo_TL.metaData.putValue(dimGizmoTLMDV);
		ObjectIDMetaDataValue dimGizmoBRMDV = matProvider.createObjIdMDV(obj);
		dimGizmoBRMDV.type = DragStatus.DragType.GIZMO_DIM;
		dimGizmo_BR.metaData.putValue(dimGizmoBRMDV);

		addChild(dimGizmo_BR);
		addChild(dimGizmo_TL);
		setGizmoVisible(false);

		setResource(new G3DResource(model));

		setSelected(false);
		setMaterialToMesh(G3D_FILL_MESH_NAME, G3D_FILL_MAT_NAME);
	}

	private void setMaterialToMesh(String meshName, String materialName) {
		for (Model mdl : resource.models) {
			Mesh mesh = mdl.getMeshByName(meshName);
			if (mesh != null) {
				mesh.materialName = materialName;
			}
		}
	}

	private void setLayerToMesh(String meshName, int layer) {
		for (Model mdl : resource.models) {
			Mesh mesh = mdl.getMeshByName(meshName);
			if (mesh != null) {
				mesh.renderLayer = layer;
			}
		}
	}

	public void setSelected(boolean value) {
		if (value) {
			setMaterialToMesh(G3D_LINE_MESH_NAME, G3D_SELECTED_LINE_MAT_NAME);
			setLayerToMesh(G3D_FILL_MESH_NAME, 4);
			setLayerToMesh(G3D_LINE_MESH_NAME, 3);
		} else {
			setMaterialToMesh(G3D_LINE_MESH_NAME, G3D_LINE_MAT_NAME);
			setLayerToMesh(G3D_FILL_MESH_NAME, 4);
			setLayerToMesh(G3D_LINE_MESH_NAME, 3);
		}
		if (isGizmoEnabled) {
			setGizmoVisible(value);
		}
		isSelected = value;
	}

	protected void setGizmoVisible(boolean value) {
		dimGizmo_BR.setVisible(value);
		dimGizmo_TL.setVisible(value);
	}

	public void setGizmoEnabled(boolean value) {
		isGizmoEnabled = value;
		if (!value) {
			setGizmoVisible(false);
		}
	}

	public void setFixedDimGizmoSize(float v) {
		fixedDimGizmoScale = v;
	}

	@Override
	public Vec3f getPosition() {
		Vec3f p = obj.getMinVector().clone().rotateY((float) Math.toRadians(obj.getRotationY())).add(obj.getWPos());
		p.y += obj.getCmOffset();
		return p;
	}

	@Override
	public Vec3f getRotation() {
		return new Vec3f(0f, obj.getRotationY(), 0f);
	}

	@Override
	public Vec3f getScale() {
		return obj.getWDim();
	}

	protected Mesh getBBox(RGBA color, boolean isLines) {
		return BoundingBoxGenerator.generateBBox(1f, 1f, 1f, 0f, 0f, 0f, isLines, 3, color);
	}

	private Mesh createBasicLineBoxMesh(RGBA color) {
		Mesh line = getBBox(color, true);
		line.renderLayer = 4;
		line.name = G3D_LINE_MESH_NAME;
		return line;
	}

	private Mesh createBasicFillBoxMesh(RGBA color) {
		Mesh fill = getBBox(color, false);
		fill.renderLayer = 3;
		fill.name = G3D_FILL_MESH_NAME;
		return fill;
	}

	private Model createGenericWorldObjHelperModel() {
		Model model = new Model();
		model.addMesh(createBasicFillBoxMesh(RGBA.WHITE));
		model.addMesh(createBasicLineBoxMesh(RGBA.BLACK));
		return model;
	}

}
