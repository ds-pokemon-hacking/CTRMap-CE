
package ctrmap.util.gui;

import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.util.gui.BufferedImagePreview;

/**
 *
 */
public class TexturePreview extends BufferedImagePreview {
	private Texture t;
	
	public void showTexture(Texture t){
		if (this.t == t){
			return;
		}
		this.t = t;
		loadImage(t == null ? null : t.getBufferedImage());
	}
}
