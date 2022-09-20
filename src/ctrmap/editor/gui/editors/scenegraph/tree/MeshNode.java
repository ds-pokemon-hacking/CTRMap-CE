
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.model.Mesh;

public class MeshNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420101;
	
	private Mesh mesh;

	public MeshNode(Mesh mesh, ScenegraphJTree tree) {
		super(tree);
		this.mesh = mesh;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.meshViewer, mesh);
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
	public Object getContent() {
		return mesh;
	}
}
