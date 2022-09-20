package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class SceneTemplateNodeNode extends CSNode {

	public static final int RESID = 0x42010B;

	private G3DSceneTemplate.G3DSceneTemplateNode templateNode;

	public SceneTemplateNodeNode(G3DSceneTemplate.G3DSceneTemplateNode node, CSJTree tree) {
		super(tree);
		this.templateNode = node;

		for (G3DSceneTemplate.G3DSceneTemplateNode child : node.children) {
			addChild(new SceneTemplateNodeNode(child, tree));
		}
		node.children.addListener(new CSNodeListener<G3DSceneTemplate.G3DSceneTemplateNode>(this) {
			@Override
			protected CSNode createNode(G3DSceneTemplate.G3DSceneTemplateNode elem) {
				return new SceneTemplateNodeNode(elem, tree);
			}
		});
		registerActionPrepend("Add child", this::callAddChild);
	}

	private void callAddChild() {
		G3DSceneTemplate.G3DSceneTemplateNode newElem = new G3DSceneTemplate.G3DSceneTemplateNode();

		newElem.setName("Node");
		templateNode.addChild(newElem);
		setExpansionState(true);
	}

	@Override
	public void onNodeSelected() {

	}

	@Override
	public IEditor getEditor(NGEditorController editors) {
		return null;
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		String name = templateNode.getName();
		if (name == null) {
			name = "Node";
		}
		return name;
	}

	@Override
	public NamedResource getContent() {
		return templateNode;
	}

	@Override
	public ListenableList getParentList() {
		if (templateNode.parent == null) {
			return new ListenableList();
		}
		return templateNode.parent.children;
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.SCENE_TEMPLATE_NODE;
	}

	@Override
	public void setContent(NamedResource cnt) {
		templateNode = (G3DSceneTemplate.G3DSceneTemplateNode) cnt;
	}

	@Override
	public void putForExport(G3DResource rsc) {

	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return null;
	}
}
