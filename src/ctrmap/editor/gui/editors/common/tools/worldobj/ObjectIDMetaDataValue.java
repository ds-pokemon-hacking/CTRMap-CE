
package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.formats.pokemon.WorldObject;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.metadata.uniforms.CustomUniformIntArr;
import ctrmap.renderer.util.ObjectSelection;
import java.util.List;

public class ObjectIDMetaDataValue extends CustomUniformIntArr {
	private List l;
	private WorldObject obj;
	
	public Enum type;
	
	public ObjectIDMetaDataValue(List<? extends WorldObject> list, WorldObject obj){
		super("objectId");
		l = list;
		this.obj = obj;
	}
	
	@Override
	public MetaDataValue.Type getType(){
		return Type.INT;
	}
	
	@Override
	public int[] intValues(){
		return new int[]{ObjectSelection.makeSelectionOBJID(l.indexOf(obj), type)};
	}
	
	@Override
	public int getValueCount(){
		return 1;
	}
}
