
package ctrmap.editor.system.script;

import ctrmap.formats.pokemon.IScriptObject;

/**
 *
 */
public class ExtraScrObj implements IScriptObject {

	public final ScriptObjManager.ScriptObjBinding binding;

	public ExtraScrObj(ScriptObjManager.ScriptObjBinding binding) {
		this.binding = binding;
	}

	@Override
	public int getObjectTypeID() {
		return ScriptObjManager.EXTRA_SCRID_TYPEID;
	}

	@Override
	public int getSCRID() {
		return binding.objectSCRID;
	}

	@Override
	public void setSCRID(int SCRID) {
		binding.objectSCRID = SCRID;
	}

}
