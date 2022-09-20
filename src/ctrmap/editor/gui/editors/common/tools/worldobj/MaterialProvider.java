package ctrmap.editor.gui.editors.common.tools.worldobj;

import ctrmap.formats.pokemon.WorldObject;
import ctrmap.renderer.scene.texturing.Material;

/**
 *
 */
public interface MaterialProvider {

	public Material getSelectionMaterial();

	public Material getLineMaterial();

	public Material getFillMaterial();

	public Material getDimGizmoMaterial();
	
	public ObjectIDMetaDataValue createObjIdMDV(WorldObject obj);
}
