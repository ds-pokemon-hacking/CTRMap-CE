
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.animation.skeletal.KinematicsController;

public class KinematicsControllerNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420005;
	
	private KinematicsController ctrl;

	public KinematicsControllerNode(KinematicsController ctrl, ScenegraphJTree tree) {
		super(tree);
		this.ctrl = ctrl;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.kinematicsEditor, ctrl);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return ctrl.getClass().getSimpleName() + "@" + ctrl.targetJointName;
	}

	@Override
	public Object getContent() {
		return ctrl;
	}
}
