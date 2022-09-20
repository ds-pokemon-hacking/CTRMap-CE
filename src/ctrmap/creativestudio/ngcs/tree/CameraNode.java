
package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class CameraNode extends CSNode {

	public static final int RESID = 0x420003;
	
	private Camera cam;

	public CameraNode(Camera cam, CSJTree tree) {
		super(tree);
		this.cam = cam;
	}
	
	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.cameraEditor;
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return cam.name;
	}

	@Override
	public NamedResource getContent() {
		return cam;
	}
	
	@Override
	public void onNodeSelected() {
		getCS().changeCamera(cam);
	}
	
	@Override
	public boolean getShouldResetCamera() {
		return false;
	}

	@Override
	public ListenableList getParentList() {
		return getCS().getCameras();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.CAMERA;
	}

	@Override
	public void setContent(NamedResource cnt) {
		cam = (Camera) cnt;
	}
	
	@Override
	public void putForExport(G3DResource rsc) {
		rsc.cameras.add(cam);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(rsc.cameras);
	}
}
