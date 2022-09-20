
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.texturing.Texture;

public class TextureNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420108;
	
	private Texture tex;

	public TextureNode(Texture tex, ScenegraphJTree tree) {
		super(tree);
		this.tex = tex;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.textureViewer, tex);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return tex.name;
	}

	@Override
	public Object getContent() {
		return tex;
	}
}
