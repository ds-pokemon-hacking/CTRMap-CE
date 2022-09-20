
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.Light;

public class LightNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420109;
	
	private Light light;

	public LightNode(Light light, ScenegraphJTree tree) {
		super(tree);
		this.light = light;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.lightEditor, light);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return light.name;
	}

	@Override
	public Object getContent() {
		return light;
	}
}
