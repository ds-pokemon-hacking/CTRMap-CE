
package ctrmap.editor.gui.editors.common;

import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.editor.gui.editors.common.tools.AbstractTool;

public class EditorScene extends Scene{
	private AbstractPerspective edt;
	
	private Scene currentG3DEx;
		
	public EditorScene(AbstractPerspective editors){
		super("edt");
		edt = editors;
	}
	
	public void onToolChange(AbstractTool tool){
		setG3DEx(tool.getG3DEx());
	}
	
	public void setG3DEx(Scene g3dEx){
		if (currentG3DEx != g3dEx){
			if (currentG3DEx != null){
				removeChild(currentG3DEx);
			}
			addChild(g3dEx);
			currentG3DEx = g3dEx;
		}
	}
	
	private void addOrRemoveSub(G3DResourceInstance sub, boolean shouldExist){
		int idx = children.indexOf(sub);
		if (idx != -1){
			if (!shouldExist){
				removeChild(idx);
			}
		}
		else {
			if (shouldExist){
				addChild(sub);
			}
		}
	}
}
