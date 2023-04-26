package ctrmap.formats.ntr.common.gfx.texture;

import ctrmap.formats.generic.interchange.CMIFTextureFormat;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import java.io.DataInput;
import java.io.IOException;

public class GETextureDirect extends GETexture {

	public GETextureDirect(DataInput in, int width, int height) throws IOException {
		super(in, GETextureFormat.RGB5A1, width, height);
	}

	public Texture decode() {
		Texture tex = new Texture(width, height, TextureFormatHandler.RGB5A1, GETextureDecoder.decodeRGB5A1(width, height, data));
		tex.metaData.putValue(ReservedMetaData.DESIRED_TEX_FORMAT, CMIFTextureFormat.FULL_COLOR);
		return tex;
	}

	@Override
	public Texture decode(short[] palette) {
		return decode();
	}
}
