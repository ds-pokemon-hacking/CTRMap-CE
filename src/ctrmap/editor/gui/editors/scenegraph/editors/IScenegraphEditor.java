package ctrmap.editor.gui.editors.scenegraph.editors;

import java.awt.Component;

public interface IScenegraphEditor {
	public void load(Object o);
	
	public default Component getGUI() {
		return (Component) this;
	}
}
