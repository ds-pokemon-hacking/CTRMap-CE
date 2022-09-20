
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.Camera;

public class CameraNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420003;
	
	private Camera cam;

	public CameraNode(Camera cam, ScenegraphJTree tree) {
		super(tree);
		this.cam = cam;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.cameraEditor, cam);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return cam.name;
	}

	@Override
	public Object getContent() {
		return cam;
	}
}
