package ctrmap.formats.ntr.common.gfx.texture;

import ctrmap.formats.generic.interchange.CMIFTextureFormat;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import java.io.DataInput;
import java.io.IOException;

public class GETextureIndexed extends GETexture {

	public final boolean firstIndexAlpha;

	public GETextureIndexed(DataInput in, GETextureFormat format, int width, int height) throws IOException {
		this(in, format, width, height, false);
	}
	
	public GETextureIndexed(DataInput in, GETextureFormat format, int width, int height, boolean firstIndexAlpha) throws IOException {
		super(in, format, width, height);
		this.firstIndexAlpha = firstIndexAlpha;
	}

	@Override
	public Texture decode(short[] palette) {
		Texture tex;
		if (format == GETextureFormat.A3I5 || format == GETextureFormat.A5I3) {
			tex = new Texture(width, height, TextureFormatHandler.RGBA8, GETextureDecoder.decodeAlpha(width, height, format, data, palette));
		} else {
			tex = new Texture(width, height, TextureFormatHandler.RGB5A1, GETextureDecoder.decodeIndexed(width, height, data, format, palette, firstIndexAlpha));
		}
		tex.metaData.putValue(ReservedMetaData.DESIRED_TEX_FORMAT, CMIFTextureFormat.REDUCED_COLOR);
		return tex;
	}
}
