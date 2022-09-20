package ctrmap.editor.gui.editors.common.tools;

import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;

public abstract class BaseTool implements AbstractTool{
	
	@Override
	public abstract AbstractToolbarEditor getEditor();

	@Override
	public abstract JComponent getGUI();

	@Override
	public void onToolInit() {
	}

	@Override
	public abstract String getFriendlyName();

	@Override
	public void onToolShutdown() {
	}

	@Override
	public void fireCancel() {
	}

	@Override
	public boolean getSelectorEnabled() {
		return false;
	}

	@Override
	public boolean getNaviEnabled() {
		return false;
	}

	@Override
	public void onTileClick(MouseEvent e) {
	}

	@Override
	public void onTileMouseDown(MouseEvent e) {
	}

	@Override
	public void onTileMouseUp(MouseEvent e) {
	}

	@Override
	public void onTileMouseMoved(MouseEvent e) {
	}

	@Override
	public void onTileMouseDragged(MouseEvent e) {
	}
	
	@Override
	public void updateComponents(){
		
	}
	
	@Override
	public void onViewportSwitch(boolean isOrtho) {
		
	}
}
