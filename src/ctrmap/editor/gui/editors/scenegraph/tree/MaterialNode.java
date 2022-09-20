
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.texturing.Material;

public class MaterialNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420102;
	
	private Material mat;

	public MaterialNode(Material mat, ScenegraphJTree tree) {
		super(tree);
		this.mat = mat;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.materialEditor, mat);
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
	public Object getContent() {
		return mat;
	}
}
