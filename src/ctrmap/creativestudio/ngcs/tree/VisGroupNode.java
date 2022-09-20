package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.gui.components.tree.CheckboxTreeCell;
import xstandard.util.ListenableList;

public class VisGroupNode extends CSNode {

	public static final int RESID = 0x42010C;

	private ListenableList<MeshVisibilityGroup> list;
	private MeshVisibilityGroup visGroup;
	
	private CheckboxTreeCell checkbox = new CheckboxTreeCell();

	public VisGroupNode(Model mdl, MeshVisibilityGroup visGroup, CSJTree tree) {
		super(tree);
		setTreeCellComponent(checkbox);
		list = mdl.visGroups;
		this.visGroup = visGroup;
		checkbox.setChecked(visGroup.isVisible);
		checkbox.addActionListener(((e) -> {
			visGroup.isVisible = checkbox.isChecked();
		}));
	}

	@Override
	public IEditor getEditor(NGEditorController editors) {
		return null;
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return visGroup.name;
	}

	@Override
	public NamedResource getContent() {
		return visGroup;
	}

	@Override
	public ListenableList getParentList() {
		return list;
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.VISGROUP;
	}

	@Override
	public void setContent(NamedResource cnt) {
		visGroup = (MeshVisibilityGroup) cnt;
	}

	@Override
	public void putForExport(G3DResource rsc) {
		getDmyModel(rsc).addVisGroup(visGroup);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(getDmyModel(rsc).visGroups);
	}
}
