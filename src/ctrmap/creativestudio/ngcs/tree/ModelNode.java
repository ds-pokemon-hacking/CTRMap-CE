package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class ModelNode extends CSNode {

	public static final int RESID = 0x420100;
	public static final int RESID_SKELETON = 0x421101;

	private Model mdl;

	private ContainerNode meshListNode;
	private ContainerNode materialList;
	private ContainerNode visgroupList;
	private ContainerNode skeleton;

	private CSNodeListener<Mesh> meshListener;
	private CSNodeListener<Material> materialListener;
	private CSNodeListener<MeshVisibilityGroup> visgroupListener;
	private CSNodeListener<Joint> skelListener;

	public ModelNode(Model mdl, CSJTree tree) {
		super(tree);
		this.mdl = mdl;
		mdl.isVisible = getCS().isAllModelsVisible();

		meshListNode = new ContainerNode("Meshes", CSNodeContentType.MESH, mdl.meshes, -1, tree);
		addChild(meshListNode);

		materialList = new ContainerNode("Materials", CSNodeContentType.MATERIAL, mdl.materials, -1, tree);
		addChild(materialList);

		visgroupList = new ContainerNode("Visibility Groups", CSNodeContentType.VISGROUP, mdl.visGroups, -1, tree);
		addChild(visgroupList);

		skeleton = new ContainerNode("Skeleton", CSNodeContentType.JOINT, mdl.skeleton.getJoints(), RESID_SKELETON, tree);
		addChild(skeleton);
		
		meshListener = new CSNodeListener<Mesh>(meshListNode) {
			@Override
			protected CSNode createNode(Mesh elem) {
				return new MeshNode(elem, tree);
			}
		};
		
		materialListener = new CSNodeListener<Material>(materialList) {
			@Override
			protected CSNode createNode(Material elem) {
				return new MaterialNode(elem, tree);
			}
		};
		
		skelListener = new CSNodeListener<Joint>(skeleton) {
			@Override
			protected boolean isAllowEntityChange(Joint elem) {
				return elem.parentName == null;
			}

			@Override
			protected CSNode createNode(Joint elem) {
				return new JointNode(elem, tree);
			}
		};
		
		visgroupListener = new CSNodeListener<MeshVisibilityGroup>(visgroupList) {
			@Override
			protected CSNode createNode(MeshVisibilityGroup elem) {
				return new VisGroupNode(mdl, elem, tree);
			}
		};
		
		rebuildSubNodes();
	}

	protected final void rebuildSubNodes() {
		meshListNode.removeAllChildren();
		materialList.removeAllChildren();
		visgroupList.removeAllChildren();
		skeleton.removeAllChildren();
		for (Mesh mesh : mdl.meshes) {
			meshListNode.addChild(new MeshNode(mesh, tree));
		}
		for (Material mat : mdl.materials) {
			materialList.addChild(new MaterialNode(mat, tree));
		}
		for (MeshVisibilityGroup visgroup : mdl.visGroups) {
			visgroupList.addChild(new VisGroupNode(mdl, visgroup, tree));
		}
		for (Joint j : mdl.skeleton.getJoints()) {
			if (j.parentName == null) {
				skeleton.addChild(new JointNode(j, tree));
			}
		}
		mdl.meshes.addListener(meshListener);
		mdl.materials.addListener(materialListener);
		mdl.visGroups.addListener(visgroupListener);
		mdl.skeleton.getJoints().addListener(skelListener);
	}

	@Override
	public void onNodeRemoved() {
		mdl.meshes.removeListener(meshListener);
		mdl.materials.removeListener(materialListener);
		mdl.visGroups.removeListener(visgroupListener);
		mdl.skeleton.getJoints().removeListener(skelListener);
	}

	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.modelEditor;
	}

	@Override
	public void onNodeSelected() {
		getCS().showLoadedModel(mdl);
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
	public Model getContent() {
		return mdl;
	}

	@Override
	public ListenableList getParentList() {
		return getCS().getModels();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.MODEL;
	}

	@Override
	public void setContent(NamedResource cnt) {
		((Model) cnt).isVisible = mdl.isVisible;
		mdl = (Model) cnt;
	}

	@Override
	public void onReplaceFinish(Object oldContent) {
		rebuildSubNodes();
		NGCS cs = getCS();
		if (cs.currentModel == oldContent) {
			mdl.isVisible = cs.isAllModelsVisible();
			cs.showLoadedModel(mdl);
		}
	}

	@Override
	public void putForExport(G3DResource rsc) {
		rsc.addModel(mdl);
		rsc.addTextures(getCS().getTextures()); //textures for model export
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(rsc.models);
	}
}
