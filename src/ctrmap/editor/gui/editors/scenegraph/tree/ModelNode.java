
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;

public class ModelNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420100;
	public static final int RESID_SKELETON = 0x421101;
	
	private Model mdl;

	public ModelNode(Model mdl, ScenegraphJTree tree) {
		super(tree);
		this.mdl = mdl;
		
		ContainerNode meshListNode = new ContainerNode("Meshes", -1, tree);
		addChild(meshListNode);
		
		for (Mesh mesh : mdl.meshes){
			meshListNode.addChild(new MeshNode(mesh, tree));
		}
		
		ContainerNode materialList = new ContainerNode("Materials", -1, tree);
		addChild(materialList);
		for (Material mat : mdl.materials){
			materialList.addChild(new MaterialNode(mat, tree));
		}
		
		ContainerNode skeleton = new ContainerNode("Skeleton", RESID_SKELETON, tree);
		addChild(skeleton);
		for (Joint j : mdl.skeleton.getJoints()){
			if (j.parentName == null){
				skeleton.addChild(new JointNode(j, tree));
			}
		}
		
		mdl.meshes.addListener(new ScenegraphListener<Mesh>(meshListNode) {
			@Override
			protected ScenegraphExplorerNode createNode(Mesh elem) {
				return new MeshNode(elem, tree);
			}
		});
		mdl.materials.addListener(new ScenegraphListener<Material>(materialList) {
			@Override
			protected ScenegraphExplorerNode createNode(Material elem) {
				return new MaterialNode(elem, tree);
			}
		});
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return mdl.name;
	}

	@Override
	public Object getContent() {
		return mdl;
	}
}
