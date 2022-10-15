package ctrmap.creativestudio.ngcs;

import ctrmap.creativestudio.editors.*;

public class NGEditorController {

	private final NGCS cs;

	public final ModelEditor modelEditor = new ModelEditor();
	public final JointEditor jointEditor;
	public final MaterialEditor materialEditor;
	public final MeshEditor meshEditor = new MeshEditor();
	public final CameraEditor cameraEditor = new CameraEditor();
	public final SkeletalEditor sklAnmEditor = new SkeletalEditor();
	public final DefaultINamedEditor defaultEditor = new DefaultINamedEditor();
	public final MatAnimEditor matAnimEditor = new MatAnimEditor();
	public final TextureEditor textureEditor = new TextureEditor();
	public final CameraAnimeEditor cameraAnimeEditor = new CameraAnimeEditor();
	public final CameraTransformEditor cameraTransformEditor = new CameraTransformEditor();
	public final LightEditor lightEditor = new LightEditor();

	private IEditor currentEditor = null;

	public NGEditorController(NGCS cs) {
		this.cs = cs;
		materialEditor = new MaterialEditor(cs.getScene());
		jointEditor = new JointEditor(cs);
	}

	public void switchEditorOpenObject(IEditor editor, Object o) {
		IEditor oldEditor = currentEditor;
		if (editor == null) {
			currentEditor = defaultEditor;
		} else {
			currentEditor = editor;
		}
		if (oldEditor != currentEditor) {
			if (oldEditor != null) {
				oldEditor.save();
				oldEditor.handleObject(null);
			}
			currentEditor.handleObject(o);
			cs.switchEditorUI(currentEditor);
		}
		else {
			currentEditor.handleObject(o);
		}
	}

	public void save() {
		currentEditor.save();
	}
}
