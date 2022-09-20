
package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class SceneTemplateNode extends CSNode {

	public static final int RESID = 0x42010B;
	
	private G3DSceneTemplate template;

	public SceneTemplateNode(G3DSceneTemplate template, CSJTree tree) {
		super(tree);
		this.template = template;
		for (G3DSceneTemplate.G3DSceneTemplateNode child : template.root.children){
			addChild(new SceneTemplateNodeNode(child, tree));
		}
		template.root.children.addListener(new CSNodeListener<G3DSceneTemplate.G3DSceneTemplateNode>(this) {
			@Override
			protected CSNode createNode(G3DSceneTemplate.G3DSceneTemplateNode elem) {
				return new SceneTemplateNodeNode(elem, tree);
			}
		});
		
		registerActionPrepend("Add node", this::callAdd);
	}
	
	private void callAdd() {
		G3DSceneTemplate.G3DSceneTemplateNode newElem = new G3DSceneTemplate.G3DSceneTemplateNode();

		newElem.setName("Node");
		template.root.addChild(newElem);
		setExpansionState(true);
	}
	
	@Override
	public void onNodeSelected(){
		getCS().showSceneTemplate(template);
	}
	
	@Override
	public void onNodeRemoved() {
		NGCS cs = getCS();
		if (cs.currentTemplateSceneTemplate == template) {
			cs.stopShowingSceneTemplate();
		}
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
		return template.getName();
	}

	@Override
	public NamedResource getContent() {
		return template;
	}

	@Override
	public ListenableList getParentList() {
		return getCS().getSceneTemplates();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.SCENE_TEMPLATE;
	}

	@Override
	public void setContent(NamedResource cnt) {
		template = (G3DSceneTemplate) cnt;
	}
	
	@Override
	public void putForExport(G3DResource rsc) {
		rsc.addSceneTemplate(template);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(rsc.sceneTemplates);
	}
}
