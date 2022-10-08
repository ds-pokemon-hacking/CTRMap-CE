package ctrmap.formats.ntr.common.gfx.texture;

import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import java.io.DataInput;
import java.io.IOException;

public class GETextureCompressed extends GETexture {

	private final byte[] indexData;

	public GETextureCompressed(DataInput in, int width, int height, byte[] indexData) throws IOException {
		super(in, GETextureFormat.IDXCMPR, width, height);
		
		this.indexData = indexData;
	}

	@Override
	public Texture decode(short[] palette) {
		return new Texture(width, height, TextureFormatHandler.RGB5A1, GETextureDecoder.decodeCompressed(width, height, data, indexData, palette));
	}
}
