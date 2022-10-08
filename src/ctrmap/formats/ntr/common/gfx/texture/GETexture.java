package ctrmap.formats.ntr.common.gfx.texture;

import ctrmap.renderer.scene.texturing.Texture;
import java.io.DataInput;
import java.io.IOException;

public abstract class GETexture {

	public final GETextureFormat format;

	public final int width;
	public final int height;

	public byte[] data;

	public GETexture(DataInput in, GETextureFormat format, int width, int height) throws IOException {
		this.format = format;
		this.width = width;
		this.height = height;
		int dataSize = (width * height * format.bpp) >> 3;
		data = new byte[dataSize];
		in.readFully(data);
	}
	
	public abstract Texture decode(short[] palette);
}
