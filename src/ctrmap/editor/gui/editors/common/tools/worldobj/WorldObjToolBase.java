package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.editor.gui.editors.common.tools.BaseTool;
import xstandard.math.vec.RGBA;
import xstandard.util.ListenableList;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.util.ObjectSelection;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.ModelInstance;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.util.MaterialProcessor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;

public abstract class WorldObjToolBase extends BaseTool implements MaterialProvider {

	private boolean isCustomizableGraphicsInitialized = false;

	protected AbstractPerspective edt;

	private MaterialParams.DepthColorMask instanceAdapterDepthTest = new MaterialParams.DepthColorMask();
	
	protected final float unitSize;

	public WorldObjToolBase(AbstractPerspective editors, float unitSize) {
		edt = editors;
		this.unitSize = unitSize;
	}

	@Override
	public void onToolInit() {
		selectedWorldObj = null;

		ensureInitCustomizableGraphics();
	}

	private void ensureInitCustomizableGraphics() {
		if (!isCustomizableGraphicsInitialized) {
			//We have to do this in onToolInit because the colors are retrieved with overridable methods and as such can not be initialized in the constructor
			gridObj_SelectionLineMaterial = MaterialProcessor.createConstantMaterial(WorldObjInstanceAdapter.G3D_SELECTED_LINE_MAT_NAME, getSelectionColor());
			gridObj_RegularBBoxLineMaterial = MaterialProcessor.createConstantMaterial(WorldObjInstanceAdapter.G3D_LINE_MAT_NAME, getRegLineColor());
			gridObj_RegularBBoxFillMaterial = MaterialProcessor.createConstantMaterial(WorldObjInstanceAdapter.G3D_FILL_MAT_NAME, getRegFillColor());
			dimGizmoMaterial.name = WorldObjInstanceAdapter.G3D_DIM_GIZMO_MAT_NAME;
			dimGizmoMaterial.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.VCOL);
			isCustomizableGraphicsInitialized = true;
		}
	}

	@Override
	public void onToolShutdown() {
		selectedWorldObj = null;
		if (getWorldObjects() != null) {
			getWorldObjects().removeListener(worldObjListListener);
		}
	}

	@Override
	public boolean getSelectorEnabled() {
		return false;
	}

	protected boolean isEnforceShaderlessSelection() {
		return false;
	}

	@Override
	public abstract boolean getNaviEnabled();

	@Override
	public void onTileClick(MouseEvent e) {
	}

	@Override
	public void onTileMouseDown(MouseEvent evt) {
		isDraggingSelectedGridObj = false;
		if (SwingUtilities.isLeftMouseButton(evt)) {
			List<? extends WorldObject> objs = getWorldObjects();
			WorldObjSelectionInfo info;
			if (isEnforceShaderlessSelection()) {
				info = getSelectedGridObjIndex3D_Old(objs, evt);
			} else {
				info = getSelectedGridObjIndex3D_SHA(evt, true);
			}
			int idx = info.selectedObjId;
			if (idx != -1 && idx < objs.size()) {
				int curObjIndex = objs.indexOf(selectedWorldObj);
				edt.dcc.setDebugCameraMotionEnabled(false);
				if (curObjIndex != idx) {
					selectedWorldObj = objs.get(idx);
					showWorldObjInEditor(idx);
					isLastSelectedSameAsBefore = false;
				} else {
					isLastSelectedSameAsBefore = true;
				}
				WorldObject obj = objs.get(idx);
				setSelectedObject(obj);
				isDragSelectedGridObj = true;
				if (edt.dcc.getDebugCamera().projMode == Camera.ProjectionMode.ORTHO || Math.abs(edt.dcc.getDebugCamera().rotation.x) > 15f) { //Do not drag when camera is too low
					dragStatus = new DragStatus(worldObjHelperMap.get(obj), edt.getWorldCollisionProvider(), edt.getInjectionScene(), info.selectionType);
					dragStatus.beginDrag(evt, edt.getRenderer());
				}
			}
		}
	}

	public abstract RGBA getSelectionColor();

	public abstract RGBA getRegLineColor();

	public abstract RGBA getRegFillColor();

	public abstract boolean getIsGizmoEnabled();

	public abstract ListenableList<? extends WorldObject> getWorldObjects();

	protected WorldObjInstanceAdapter createInstanceAdapter(WorldObject obj) {
		return new WorldObjInstanceAdapter(obj, unitSize, this);
	}

	private void setDepthTestInstanceToModelInstance(G3DResourceInstance inst) {
		if (inst != null) {
			for (Model model : inst.resource.models) {
				for (Material mat : model.materials) {
					mat.depthColorMask = instanceAdapterDepthTest;
				}
			}
		}
	}

	protected WorldObjInstanceAdapter getCreateInstanceAdapter(WorldObject obj) {
		WorldObjInstanceAdapter ia = createInstanceAdapter(obj);
		setDepthTestInstanceToModelInstance(ia);
		setDepthTestInstanceToModelInstance(ia.dimGizmo_BR);
		setDepthTestInstanceToModelInstance(ia.dimGizmo_TL);
		ia.setFixedDimGizmoSize(getFixedDimGizmoSize());
		ia.setGizmoEnabled(getIsGizmoEnabled());
		return ia;
	}

	public abstract void showWorldObjInEditor(int index);

	public abstract WorldObject getSelectedEditorObject();

	protected Scene worldObjScene = new Scene("WorldObj");
	protected Map<WorldObject, WorldObjInstanceAdapter> worldObjHelperMap = new HashMap<>();

	public Collection<WorldObjInstanceAdapter> getInstanceAdapters() {
		return worldObjHelperMap.values();
	}

	private ListenableList.ElementChangeListener worldObjListListener = new ListenableList.ElementChangeListener() {
		@Override
		public void onEntityChange(ListenableList.ElementChangeEvent evt) {
			if (evt.element instanceof WorldObject) {
				if (evt.element == null) {
					return;
				}
				WorldObject entity = (WorldObject) evt.element;

				switch (evt.type) {
					case REMOVE:
						ModelInstance miForObj = worldObjHelperMap.get(entity);
						if (miForObj != null) {
							worldObjHelperMap.remove(entity);
							worldObjScene.removeChild(miForObj);
						}
						break;
					case ADD:
						if (!worldObjHelperMap.containsKey(entity)) {
							WorldObjInstanceAdapter adapter = getCreateInstanceAdapter(entity);
							worldObjHelperMap.put(entity, adapter);
							worldObjScene.addChild(adapter);
						}
						setSelectedObject(getSelectedEditorObject());
						break;
				}
			}
		}
	};

	public void setSelectedObject(WorldObject obj) {
		selectedWorldObj = obj;
		for (Map.Entry<WorldObject, WorldObjInstanceAdapter> e : new ArrayList<>(worldObjHelperMap.entrySet())) {
			e.getValue().setSelected(e.getKey() == obj);
		}
	}

	private ListenableList lastWOs = null;

	public void setNewObjForObjAdapter(WorldObject old, WorldObject newO) {
		WorldObjInstanceAdapter a = worldObjHelperMap.get(old);
		if (a != null) {
			a.setObj(newO);
		}
	}

	public void rebuildAdapterForObj(WorldObject wo) {
		worldObjScene.removeChild(worldObjHelperMap.get(wo));
		WorldObjInstanceAdapter ia = getCreateInstanceAdapter(wo);
		ia.setSelected(wo == getSelectedEditorObject());
		worldObjHelperMap.put(wo, ia);
		worldObjScene.addChild(worldObjHelperMap.get(wo));
	}

	public void fullyRebuildScene() {
		ensureInitCustomizableGraphics();
		worldObjScene.clear();
		worldObjHelperMap.clear();
		if (getWorldObjects() == null) {
			return;
		}
		WorldObject sel = getSelectedEditorObject();
		selectedWorldObj = sel;
		if (isCustomizableGraphicsInitialized) {
			int idx = 0;
			for (WorldObject wo : getWorldObjects()) {
				if (wo != null) {
					WorldObjInstanceAdapter ia = getCreateInstanceAdapter(wo);
					ia.setSelected(wo == sel);
					worldObjHelperMap.put(wo, ia);
					worldObjScene.addChild(worldObjHelperMap.get(wo));
					idx++;
				}
			}
		}
		ListenableList<? extends WorldObject> wos = getWorldObjects();
		if (wos != lastWOs) {
			if (lastWOs != null) {
				lastWOs.removeListener(worldObjListListener);
			}
			lastWOs = wos;
		}
		wos.addListener(worldObjListListener);
	}

	private Material gridObj_SelectionLineMaterial;
	private Material gridObj_RegularBBoxLineMaterial;
	private Material gridObj_RegularBBoxFillMaterial;
	private Material dimGizmoMaterial = new Material();

	@Override
	public Material getLineMaterial() {
		return gridObj_RegularBBoxLineMaterial;
	}

	@Override
	public Material getFillMaterial() {
		return gridObj_RegularBBoxFillMaterial;
	}

	@Override
	public Material getSelectionMaterial() {
		return gridObj_SelectionLineMaterial;
	}

	@Override
	public Material getDimGizmoMaterial() {
		return dimGizmoMaterial;
	}

	@Override
	public ObjectIDMetaDataValue createObjIdMDV(WorldObject obj) {
		return new ObjectIDMetaDataValue(getWorldObjects(), obj);
	}

	@Override
	public Scene getG3DEx() {
		return worldObjScene;
	}

	@Override
	public void onTileMouseUp(MouseEvent e) {
		if (isDragSelectedGridObj && !isDraggingSelectedGridObj && isLastSelectedSameAsBefore) {
			//try select again
			WorldObjSelectionInfo info = getSelectedGridObjIndex3D_SHA(e, false);
			int idx = info.selectedObjId;
			if (idx != -1) {
				selectedWorldObj = getWorldObjects().get(idx);
				showWorldObjInEditor(idx);
				setSelectedObject(selectedWorldObj);
			}
		}
		isDragSelectedGridObj = false;
		isDraggingSelectedGridObj = false;
		dragStatus = null;
		edt.dcc.setDebugCameraMotionEnabled(true);
	}

	@Override
	public void onTileMouseMoved(MouseEvent e) {
	}

	protected boolean isDragSelectedGridObj = false;
	protected boolean isDraggingSelectedGridObj = false;
	protected boolean isLastSelectedSameAsBefore = false;

	@Override
	public void onTileMouseDragged(MouseEvent e) {
		if (isBusyDragging()) {
			if (dragStatus != null) {
				isDraggingSelectedGridObj = true;
				dragStatus.updateDrag(e, edt.getRenderer());
				updateComponents();
			}
		}
	}

	public boolean isBusyDragging() {
		return isDragSelectedGridObj && selectedWorldObj != null;
	}

	protected WorldObject selectedWorldObj = null;
	protected DragStatus dragStatus = null;

	private List<WorldObject> lastCandidates = new ArrayList<>();
	private int lastCandidateRelIdx = -1;

	protected WorldObjSelectionInfo getSelectedGridObjIndex3D_SHA(MouseEvent e, boolean enableLastSelected) {
		WorldObject selectedObj = getSelectedEditorObject();

		WorldObjSelectionInfo info = new WorldObjSelectionInfo();
		
		Point location = e.getPoint();

		//The gizmos use the old system to account for alpha testing shenanigans
		if (selectedObj != null) {
			WorldObjInstanceAdapter selectedObjAdapter = worldObjHelperMap.get(selectedObj);

			if (selectedObjAdapter != null) {
				if (getIsGizmoEnabled()) {
					if (ObjectSelection.getIsObjSelected(location, selectedObjAdapter.dimGizmo_TL, edt.getRenderer())) {
						info.selectedObjId = getWorldObjects().indexOf(selectedObj);
						info.selectionType = DragStatus.DragType.GIZMO_POSDIM;
						return info;
					} else if (ObjectSelection.getIsObjSelected(location, selectedObjAdapter.dimGizmo_BR, edt.getRenderer())) {
						info.selectedObjId = getWorldObjects().indexOf(selectedObj);
						info.selectionType = DragStatus.DragType.GIZMO_DIM;
						return info;
					}
				}

				if (enableLastSelected && ObjectSelection.getIsObjSelected(location, selectedObjAdapter, edt.getRenderer())) {
					info.selectedObjId = getWorldObjects().indexOf(selectedObj);
					info.selectionType = DragStatus.DragType.OBJECT;
					return info;
				}
			}
		}

		int selectedIndex = ObjectSelection.getSelectedObjectIDSHA(location, edt.getRenderer());

		if (selectedIndex != -1) {
			int dragType = selectedIndex >>> 16;
			if (dragType <= DragStatus.DragType.GIZMO_DIM.ordinal()) {
				info.selectedObjId = selectedIndex & 0xFFFF;
				info.selectionType = DragStatus.DragType.values()[dragType];
			}
		}

		return info;
	}

	protected WorldObjSelectionInfo getSelectedGridObjIndex3D_Old(List<? extends WorldObject> l, MouseEvent e) {
		WorldObjSelectionInfo info = new WorldObjSelectionInfo();
		List<WorldObject> newCandidates = new ArrayList<>();
		
		Point location = e.getPoint();

		int lIdx = 0;
		for (WorldObject obj : l) {
			WorldObjInstanceAdapter adapter = worldObjHelperMap.get(obj);
			if (adapter != null) {
				if (adapter.isSelected && adapter.isGizmoEnabled) {
					if (ObjectSelection.getIsObjSelected(location, adapter.dimGizmo_TL, edt.getRenderer())) {
						info.selectedObjId = lIdx;
						info.selectionType = DragStatus.DragType.GIZMO_POSDIM;
						return info;
					}
					if (ObjectSelection.getIsObjSelected(location, adapter.dimGizmo_BR, edt.getRenderer())) {
						info.selectedObjId = lIdx;
						info.selectionType = DragStatus.DragType.GIZMO_DIM;
						return info;
					}
				}

				boolean isSelected = ObjectSelection.getIsObjSelected(location, adapter, edt.getRenderer());
				if (isSelected) {
					newCandidates.add(obj);
				}
			}
			lIdx++;
		}
		if (!newCandidates.isEmpty()) {
			if (lastCandidates.containsAll(newCandidates)) {
				lastCandidateRelIdx++;
				if (lastCandidateRelIdx >= newCandidates.size()) {
					lastCandidateRelIdx = 0;
				}
				info.selectedObjId = l.indexOf(newCandidates.get(lastCandidateRelIdx));
			} else {
				lastCandidates = newCandidates;
				lastCandidateRelIdx = 0;
				info.selectedObjId = l.indexOf(newCandidates.get(lastCandidateRelIdx));
			}
		}
		return info;
	}

	@Override
	public void onViewportSwitch(boolean isOrtho) {
		dimGizmoMaterial.depthColorMask.depthFunction = isOrtho ? MaterialParams.TestFunction.ALWAYS : MaterialParams.TestFunction.LEQ;
		instanceAdapterDepthTest.depthFunction = isOrtho ? MaterialParams.TestFunction.ALWAYS : MaterialParams.TestFunction.LEQ;
	}

	protected float getFixedDimGizmoSize() {
		return 0f;
	}

	@Override
	public abstract void updateComponents();

	protected static class WorldObjSelectionInfo {

		public int selectedObjId = -1;
		public DragStatus.DragType selectionType = DragStatus.DragType.OBJECT;
	}
}
