
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;

public class AnimationNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420200;
	
	private AbstractAnimation anm;

	public AnimationNode(AbstractAnimation anm, ScenegraphJTree tree) {
		super(tree);
		this.anm = anm;
		
		for (AbstractBoneTransform bt : anm.getBones()){
			addChild(new AnimationTransformNode(bt, tree));
		}
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return anm.name;
	}

	@Override
	public Object getContent() {
		return anm;
	}
}
