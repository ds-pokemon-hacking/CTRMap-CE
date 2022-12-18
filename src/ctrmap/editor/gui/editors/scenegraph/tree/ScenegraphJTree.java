
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.renderer.scene.Scene;
import xstandard.gui.components.tree.CustomJTree;
import javax.swing.tree.MutableTreeNode;

public class ScenegraphJTree extends CustomJTree {
	public ScenegraphJTree(){
		super();
		
		registerIconResource(AnimationControllerNode.RESID, "animation_controller");
		registerIconResource(AnimationNode.RESID, "anime");
		registerIconResource(AnimationTransformNode.RESID, "transform");
		registerIconResource(CameraNode.RESID, "camera");
		registerIconResource(ContainerNode.RESID_DEFAULT, "dir");
		registerIconResource(JointNode.RESID, "bone");
		registerIconResource(KinematicsControllerNode.RESID, "kinematics_controller");
		registerIconResource(LightNode.RESID, "light");
		registerIconResource(MaterialNode.RESID, "material");
		registerIconResource(MeshNode.RESID, "mesh");
		registerIconResource(ModelNode.RESID, "model");
		registerIconResource(ModelNode.RESID_SKELETON, "skeleton");
		registerIconResource(ResourceInstanceNode.RESID, "resource_instance");
		registerIconResource(ResourceNode.RESID, "resource");
		registerIconResource(TextureNode.RESID, "texture");
	}
	
	public void loadRootScene(Scene scene){
		for (int i = 0; i < root.getChildCount(); i++){
			model.removeNodeFromParent((MutableTreeNode)root.getChildAt(0));
		}
		model.insertNodeInto(new ResourceInstanceNode(scene, this), root, 0);
		model.reload();
	}
	
	private void registerIconResource(int resID, String name) {
		registerIconResourceImpl(resID, "ctrmap/resources/discovery/scenegraph_explorer/" + name + ".png");
	}
}
