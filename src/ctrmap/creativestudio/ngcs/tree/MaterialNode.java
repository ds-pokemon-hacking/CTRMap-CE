
package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class MaterialNode extends CSNode {

	public static final int RESID = 0x420102;
	
	private Material mat;

	public MaterialNode(Material mat, CSJTree tree) {
		super(tree);
		this.mat = mat;
	}
	
	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.materialEditor;
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return mat.name;
	}

	@Override
	public NamedResource getContent() {
		return mat;
	}

	@Override
	public ListenableList getParentList() {
		return mat.parentModel.materials;
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.MATERIAL;
	}

	@Override
	public void setContent(NamedResource cnt) {
		mat = (Material) cnt;
	}
	
	@Override
	public void putForExport(G3DResource rsc) {
		Model oldParent = mat.parentModel;
		getDmyModel(rsc).materials.add(mat);
		mat.parentModel = oldParent;
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(getDmyModel(rsc).materials);
	}
}
