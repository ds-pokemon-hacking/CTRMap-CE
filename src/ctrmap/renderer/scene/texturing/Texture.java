package ctrmap.renderer.scene.texturing;

import ctrmap.renderer.backends.DriverBool;
import ctrmap.renderer.backends.DriverHandle;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.util.texture.TextureCodec;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.util.texture.TextureProcessor;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class Texture implements NamedResource {

	public String name;

	public int width;
	public int height;
	public TextureFormatHandler format = TextureFormatHandler.RGBA8;
	public byte[] data;
	
	public MetaData metaData = new MetaData();

	@Override
	public String getName() {
		return name;
	}
	
	protected Texture(){
		
	}

	public Texture(int w, int h, byte[] data) {
		this(w, h, TextureFormatHandler.RGBA8, data);
	}
	
	public Texture(int w, int h) {
		this(w, h, TextureFormatHandler.RGBA8);
	}
	
	public Texture(int w, int h, TextureFormatHandler format) {
		this(w, h, format, new byte[format.getDataSizeForDimensions(w, h)]);
	}
	
	public Texture(int w, int h, TextureFormatHandler format, byte[] data) {
		this(null, w, h, format, data);
	}
	
	public Texture(String name, int w, int h, TextureFormatHandler format, byte[] data) {
		this.name = name;
		width = w;
		height = h;
		this.format = format;
		this.data = data;
	}

	public BufferedImage getBufferedImage() {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		byte[] imgData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		byte[] input = TextureCodec.getRGBA(this, format);
		//Convert to ABGR
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = y * width * 4 + x * 4;
				byte red = input[offset];
				byte green = input[offset + 1];
				byte blue = input[offset + 2];
				byte alpha = input[offset + 3];
				imgData[offset] = alpha;
				imgData[offset + 1] = blue;
				imgData[offset + 2] = green;
				imgData[offset + 3] = red;
			}
		}
		return image;
	}

	//Rendering
	private DriverHandle rendererPointers = new DriverHandle();
	public DriverBool requestedReupload = new DriverBool();

	public void requestReupload() {
		requestedReupload.resetAll();
	}

	public boolean checkRequiredGeneration(IRenderDriver driver) {
		return rendererPointers.get(driver) == -1;
	}

	public void registerRenderer(IRenderDriver renderer, int texPointer) {
		rendererPointers.set(renderer, texPointer);
	}
	
	public int getPointer(IRenderDriver driver){
		return rendererPointers.get(driver);
	}
	
	public void deletePointer(IRenderDriver driver) {
		rendererPointers.remove(driver);
		requestedReupload.remove(driver);
	}

	public boolean checkRequestedDataReupload(IRenderDriver driver) {
		return requestedReupload.get(driver);
	}

	public ByteBuffer getRenderableData() {
		return ByteBuffer.wrap(TextureProcessor.flipImageData(width, height, format.getOnTheFlyNativeConvTexData(this), format));
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
