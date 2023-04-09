package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.creativestudio.ngcs.io.NGCSImporter;
import ctrmap.creativestudio.ngcs.rtldr.NGCSIOManager;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.VertexMorph;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import java.util.List;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.XFileDialog;
import xstandard.util.ListenableList;

public class MeshNode extends CSNode {

	private static final String MESH_HIGHLIGHT_METADATA_KEY = "isMeshHighLit";

	public static final int RESID = 0x420101;

	private Mesh mesh;
	private CSNodeListener<VertexMorph> morphListener;

	private MetaDataValue visMetaData = new MetaDataValue(MESH_HIGHLIGHT_METADATA_KEY, false, true, true);

	public MeshNode(Mesh mesh, CSJTree tree) {
		super(tree);
		this.mesh = mesh;
		mesh.metaData.putValue(visMetaData);

		if (mesh.isMorphable()) {
			registerAction("Import Vertex Morphs", this::callAddVertexMorphs);

			morphListener = new CSNodeListener<VertexMorph>(this) {
				@Override
				protected CSNode createNode(VertexMorph elem) {
					return new VertexMorphNode(mesh, elem, tree);
				}
			};
			
			rebuildSubNodes();
		} else {
			registerAction("Enable Vertex Morphs", () -> {
				int index = mesh.parentModel.meshes.indexOf(mesh);
				if (index != -1) {
					mesh.parentModel.meshes.remove(index);
					mesh.makeMorphable();
					mesh.parentModel.meshes.add(index, mesh);
				}
			});
		}
	}
	
	protected final void rebuildSubNodes() {
		if (mesh.isMorphable()) {
			removeAllChildren();
			ListenableList<VertexMorph> morphs = ((MorphableVertexList) mesh.vertices).morphs();
			for (VertexMorph morph : morphs) {
				addChild(new VertexMorphNode(mesh, morph, tree));
			}
			morphs.addListener(morphListener);
		}
	}

	@Override
	public void onNodeRemoved() {
		if (morphListener != null && mesh.isMorphable()) {
			((MorphableVertexList) mesh.vertices).morphs().removeListener(morphListener);
		}
	}

	private void callAddVertexMorphs() {
		NGCSIOManager ioMgr = getCS().getIOManager();
		List<DiskFile> sourceFiles = XFileDialog.openMultiFileDialog(CSNodeContentType.VERTEX_MORPH.getFiltersImport(ioMgr));

		G3DResource imported = NGCSImporter.importFiles(getCS(), CSNodeContentType.VERTEX_MORPH.getFormats(ioMgr), sourceFiles.toArray(new DiskFile[sourceFiles.size()]));

		for (Mesh morphMesh : imported.meshes()) {
			VertexMorph vm = VertexMorph.fromMesh(morphMesh);
			((MorphableVertexList) morphMesh.vertices).addMorph(vm);
			addChild(new VertexMorphNode(this.mesh, vm, tree));
		}
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
	public void onReplaceFinish(Object oldContent) {
		super.onReplaceFinish(oldContent);
		rebuildSubNodes();
	}

	@Override
	public void putForExport(G3DResource rsc) {
		Model mdl = getDmyModel(rsc);
		mdl.skeleton.addJoints(mesh.parentModel.skeleton.getJoints());
		mdl.meshes.add(Mesh.mirror(mesh)); //prevent unparenting
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(getDmyModel(rsc).meshes);
	}
}
