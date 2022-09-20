package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class TextureNode extends CSNode {

	public static final int RESID = 0x420108;

	private Texture tex;

	public TextureNode(Texture tex, CSJTree tree) {
		super(tree);
		this.tex = tex;
	}
	
	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.textureEditor;
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return tex.name;
	}

	@Override
	public NamedResource getContent() {
		return tex;
	}

	@Override
	public ListenableList getParentList() {
		return getCS().getTextures();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.TEXTURE;
	}

	@Override
	public void setContent(NamedResource cnt) {
		tex = (Texture) cnt;
	}

	@Override
	public void putForExport(G3DResource rsc) {
		rsc.addTexture(tex);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(rsc.textures);
	}
	
	@Override
	public void onReplaceFinish(Object oldCnt) {
		Texture oldTex = (Texture)oldCnt;
		tex.metaData.putValues(oldTex.metaData.getValues());
		getCS().reloadEditor();
	}
}
