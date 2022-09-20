
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.model.Joint;
import java.util.Objects;

public class JointNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420103;
	
	private final Joint jnt;

	public JointNode(Joint jnt, ScenegraphJTree tree) {
		super(tree);
		this.jnt = jnt;
		
		for (Joint maybeChild : jnt.parentSkeleton.getJoints()){
			if (Objects.equals(maybeChild.parentName, jnt.name)){
				addChild(new JointNode(maybeChild, tree));
			}
		}
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.jointEditor, jnt);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return jnt.name;
	}

	@Override
	public Object getContent() {
		return jnt;
	}
}
