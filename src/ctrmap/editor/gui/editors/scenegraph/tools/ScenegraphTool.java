package ctrmap.editor.gui.editors.scenegraph.tools;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.Scene;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;
import ctrmap.editor.gui.editors.common.tools.BaseTool;

public class ScenegraphTool extends BaseTool {

	private ScenegraphExplorer exp;
	
	public ScenegraphTool(ScenegraphExplorer exp) {
		this.exp = exp;
	}

	@Override
	public JComponent getGUI() {
		return exp;
	}

	@Override
	public String getFriendlyName() {
		return "Scenegraph Explorer";
	}

	@Override
	public AbstractToolbarEditor getEditor() {
		return exp;
	}

	@Override
	public String getResGroup() {
		return "scenegraph";
	}

	@Override
	public Scene getG3DEx() {
		return null;
	}

	@Override
	public void onViewportSwitch(boolean isOrtho) {
	}
}
