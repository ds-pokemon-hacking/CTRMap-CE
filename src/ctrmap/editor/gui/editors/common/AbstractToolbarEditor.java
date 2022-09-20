package ctrmap.editor.gui.editors.common;

import java.util.List;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;

public interface AbstractToolbarEditor extends AbstractSubEditor {
	public List<AbstractTool> getTools();
}
