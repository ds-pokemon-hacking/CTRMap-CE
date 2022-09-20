
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;

public class AnimationControllerNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420004;
	
	private AbstractAnimationController ctrl;

	public AnimationControllerNode(AbstractAnimationController ctrl, ScenegraphJTree tree) {
		super(tree);
		this.ctrl = ctrl;
		
		if (ctrl.anim != null){
			addChild(new AnimationNode(ctrl.anim, tree));
		}
		for (AbstractAnimation a : ctrl.animeList){
			addChild(new AnimationNode(a, tree));
		}
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.animeCtrlEditor, ctrl);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return ctrl.getName();
	}

	@Override
	public Object getContent() {
		return ctrl;
	}
}
