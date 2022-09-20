package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.gui.components.tree.CheckboxTreeCell;
import xstandard.util.ListenableList;

public class LightNode extends CSNode {

	public static final int RESID = 0x420109;

	private Light light;
	private CheckboxTreeCell checkbox = new CheckboxTreeCell();

	public LightNode(Light light, CSJTree tree) {
		super(tree);
		setTreeCellComponent(checkbox);
		this.light = light;
		checkbox.setChecked(false);
		checkbox.addActionListener(((e) -> {
			getCS().setCustomLightEnable(light, checkbox.isChecked());
		}));
	}

	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.lightEditor;
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return light.name;
	}

	@Override
	public NamedResource getContent() {
		return light;
	}

	@Override
	public boolean getShouldResetLights() {
		return false;
	}

	@Override
	public ListenableList getParentList() {
		return getCS().getLights();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.LIGHT;
	}

	@Override
	public void setContent(NamedResource cnt) {
		light = (Light) cnt;
	}

	@Override
	public void putForExport(G3DResource rsc) {
		rsc.lights.add(light);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(rsc.lights);
	}
}
