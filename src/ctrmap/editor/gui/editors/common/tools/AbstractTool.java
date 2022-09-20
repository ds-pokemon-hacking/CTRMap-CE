
package ctrmap.editor.gui.editors.common.tools;

import ctrmap.renderer.scene.Scene;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import ctrmap.editor.gui.editors.common.AbstractToolbarEditor;

/**
 *
 */
public interface AbstractTool {
	
	public AbstractToolbarEditor getEditor();
	
	public JComponent getGUI();
	
	public Scene getG3DEx();

	public void onToolInit();

	public String getFriendlyName();
	
	public String getResGroup();
	
	public void onToolShutdown();
	
	public void onViewportSwitch(boolean isOrtho);

	public void fireCancel();

	public abstract boolean getSelectorEnabled();

	public abstract boolean getNaviEnabled();

	public abstract void onTileClick(MouseEvent e);

	public abstract void onTileMouseDown(MouseEvent e);

	public abstract void onTileMouseUp(MouseEvent e);

	public abstract void onTileMouseMoved(MouseEvent e);
	
	public abstract void onTileMouseDragged(MouseEvent e);
	
	public abstract void updateComponents();
}
