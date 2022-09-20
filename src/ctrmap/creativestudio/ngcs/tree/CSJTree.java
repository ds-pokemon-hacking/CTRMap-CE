
package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.ngcs.NGCS;
import xstandard.gui.components.tree.CustomJTree;
import javax.swing.tree.MutableTreeNode;

public class CSJTree extends CustomJTree {
	
	private NGCS cs;
	
	public CSJTree(){
		super();
	}
	
	public NGCS getCS() {
		return cs;
	}
	
	public void initCS(NGCS cs) {
		this.cs = cs;
		
		registerIconResource(AnimationNode.RESID, "anime_skl");
		registerIconResource(AnimationTransformNode.RESID, "transform");
		registerIconResource(CameraNode.RESID, "camera");
		registerIconResource(ContainerNode.RESID_DEFAULT, "directory_node");
		registerIconResource(JointNode.RESID, "bone");
		registerIconResource(LightNode.RESID, "light");
		registerIconResource(MaterialNode.RESID, "material");
		registerIconResource(MeshNode.RESID, "mesh");
		registerIconResource(ModelNode.RESID, "model");
		registerIconResource(ModelNode.RESID_SKELETON, "skeleton");
		registerIconResource(CSRootSceneNode.RESID, "scene");
		registerIconResource(TextureNode.RESID, "texture");
		registerIconResource(OtherFileNode.RESID, "other");
		registerIconResource(SceneTemplateNode.RESID, "scene");
		registerIconResource(VisGroupNode.RESID, "visgroup");
		
		for (int i = 0; i < root.getChildCount(); i++){
			model.removeNodeFromParent((MutableTreeNode)root.getChildAt(0));
		}
		model.insertNodeInto(new CSRootSceneNode(cs, this), root, 0);
		model.reload();
	}
	
	public CSRootSceneNode getRootCSNode() {
		return (CSRootSceneNode) root.getChildAt(0);
	}
	
	private void registerIconResource(int resID, String name) {
		registerIconResourceImpl(resID, "ctrmap/resources/cs/data/" + name + ".png");
	}
}
