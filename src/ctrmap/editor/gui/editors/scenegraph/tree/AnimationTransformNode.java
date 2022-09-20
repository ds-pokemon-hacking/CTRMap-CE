
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.renderer.scene.animation.AbstractBoneTransform;

public class AnimationTransformNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420201;
	
	private AbstractBoneTransform bt;

	public AnimationTransformNode(AbstractBoneTransform bt, ScenegraphJTree tree) {
		super(tree);
		this.bt = bt;
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return bt.name;
	}

	@Override
	public Object getContent() {
		return bt;
	}
}
