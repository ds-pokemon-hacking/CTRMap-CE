
package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class MeshNode extends CSNode {

	private static final String MESH_HIGHLIGHT_METADATA_KEY = "isMeshHighLit";
	
	public static final int RESID = 0x420101;
	
	private Mesh mesh;
	
	private MetaDataValue visMetaData = new MetaDataValue(MESH_HIGHLIGHT_METADATA_KEY, false, true, true);

	public MeshNode(Mesh mesh, CSJTree tree) {
		super(tree);
		this.mesh = mesh;
		mesh.metaData.putValue(visMetaData);
	}
	
	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.meshEditor;
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return mesh.name;
	}
	
	@Override
	public void onNodeSelected() {
		visMetaData.setValue(true);
	}
	
	@Override
	public void onNodeDeselected() {
		visMetaData.setValue(false);
	}

	@Override
	public NamedResource getContent() {
		return mesh;
	}

	@Override
	public ListenableList getParentList() {
		return mesh.parentModel.meshes;
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.MESH;
	}

	@Override
	public void setContent(NamedResource cnt) {
		mesh = (Mesh) cnt;
	}
	
	@Override
	public void putForExport(G3DResource rsc) {
		Model mdl = getDmyModel(rsc);
		mdl.skeleton = mesh.parentModel.skeleton;
		mdl.meshes.add(mesh);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(getDmyModel(rsc).materials);
	}
}
