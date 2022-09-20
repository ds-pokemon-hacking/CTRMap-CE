package ctrmap.creativestudio.ngcs.io;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceType;
import java.awt.Frame;
import ctrmap.formats.common.FormatIOExConfig;

public interface G3DIOProvider {

	public void resetExdataCache();

	public Skeleton getSkeleton();

	public Model getModel();

	public Camera getRequestCamera();

	public G3DResource getAll();

	public Frame getGUIParent();
	
	public FormatIOExConfig getExCfg(IG3DFormatExHandler exHandler, boolean export);

	public default Camera getCameraByName(String tgtName) {
		Camera cam = null;
		G3DResource all = getAll();
		if (tgtName != null) {
			cam = (Camera) all.getNamedResource(tgtName, G3DResourceType.CAMERA);
		}
		if (cam == null) {
			cam = getRequestCamera();
		}
		return cam;
	}
}
