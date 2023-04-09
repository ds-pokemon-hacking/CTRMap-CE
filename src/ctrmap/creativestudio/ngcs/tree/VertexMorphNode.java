package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.VertexMorph;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import java.util.logging.Level;
import java.util.logging.Logger;
import xstandard.util.ListenableList;

public class VertexMorphNode extends CSNode {

	public static final int RESID = 0x420111;

	private ListenableList<VertexMorph> list;
	private VertexMorph morph;
	private final Mesh parentMesh;
	
	public VertexMorphNode(Mesh mesh, VertexMorph morph, CSJTree tree) {
		super(tree);
		this.parentMesh = mesh;
		list = ((MorphableVertexList)mesh.vertices).morphs();
		this.morph = morph;
	}
	
	@Override
	public void onNodeSelected() {
		((MorphableVertexList)parentMesh.vertices).setMorph(morph);
		
		//For debugging. Never, never EVER multithread like this.
		/*Thread t = new Thread(() -> {
			MorphableVertexList mvl = ((MorphableVertexList)parentMesh.vertices);
			VertexMorph l = mvl.currentMorph();
			if (l == null) {
				l = mvl.morphs().get(0);
			}
			for (int i = 0; i < 100; i++) {
				mvl.setMorph(l, morph, i / 99f);
				try {
					Thread.sleep(10);
				} catch (InterruptedException ex) {
					Logger.getLogger(VertexMorphNode.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		});
		t.start();*/
	}

	@Override
	public void onNodeDeselected() {
		((MorphableVertexList)parentMesh.vertices).setMorph(null);
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
		return morph.name;
	}

	@Override
	public NamedResource getContent() {
		return morph;
	}

	@Override
	public ListenableList getParentList() {
		return list;
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.VERTEX_MORPH;
	}

	@Override
	public void setContent(NamedResource cnt) {
		morph = VertexMorph.fromMesh((Mesh) cnt);
	}

	@Override
	public void putForExport(G3DResource rsc) {
		Mesh m = morph.toMesh();
		m.setAttributes(parentMesh);
		getDmyModel(rsc).addMesh(m);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(getDmyModel(rsc).meshes);
	}
}
