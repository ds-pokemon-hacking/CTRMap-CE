package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import xstandard.io.InvalidMagicException;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.formats.TextureFormat;
import ctrmap.renderer.util.texture.TextureCodec;
import ctrmap.renderer.scene.texturing.formats.TextureFormatHandler;
import ctrmap.renderer.util.texture.TextureProcessor;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TextureUtil {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Texture", "*.iftx");
	
	public static final String TEXTURE_MAGIC = "IFTX";

	public static void writeTexture(Texture tex, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(TEXTURE_MAGIC);

			TextureUtil.writeTexture(tex, false, dos);

			dos.close();
			f.setBytes(dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public static void writeTexture(Texture t, boolean compressLZSS, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(TEXTURE_MAGIC);
		dos.writeString(t.name);
		dos.writeShort((short) t.width);
		dos.writeShort((short) t.height);

		MetaData metaData = new MetaData(t.metaData);

		if (t.format.originFormat == TextureFormat.ETC1 || t.format.originFormat == TextureFormat.ETC1A4) {
			//If the texture is compressed, store the compressed data for handlers that can directly use it without recompressing
			//(but keep the RGBA data for others)
			MetaDataValue desiredTexFmt = metaData.getValue(ReservedMetaData.DESIRED_TEX_FORMAT);
			if (desiredTexFmt == null || (desiredTexFmt.intValue() == CMIFTextureFormat.COMPRESSED.ordinal())) {
				metaData.putValue(ReservedMetaData.RAW_TEX_DATA, t.data);
				metaData.putValue(ReservedMetaData.RAW_TEX_FMT, t.format.originFormat.name());
			}
		}

		byte[] data = TextureCodec.getRGBA(t, t.format);
		CMIFLZUtil.writeLZ(data, compressLZSS, dos);

		MetaDataUtil.writeMetaData(metaData, dos);
	}

	public static Texture readTexture(File f) throws IOException {
		return readTexture(new DiskFile(f));
	}
	
	public static Texture readTexture(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, TEXTURE_MAGIC);
			
			Texture tex = readTexture(l0.io, l0.fileVersion);
			
			l0.io.close();
			return tex;
		} catch (IOException ex) {
			Logger.getLogger(TextureUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static Texture readTexture(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, TEXTURE_MAGIC)) {
			throw new InvalidMagicException("Invalid texture magic.");
		}
		String name = StringIO.readStringWithAddress(dis);
		int w = dis.readShort();
		int h = dis.readShort();
		byte[] data = CMIFLZUtil.readLZ(dis, fileVersion);
		if (fileVersion < Revisions.REV_FLIP_TEXTURES) {
			data = TextureProcessor.flipImageData(w, h, data, TextureFormatHandler.RGBA8);
		}

		Texture t = new Texture(w, h, data);
		t.name = name;
		if (fileVersion >= Revisions.REV_TEX_META_DATA) {
			t.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		}
		if (t.metaData.hasValue(ReservedMetaData.RAW_TEX_DATA) && t.metaData.hasValue(ReservedMetaData.RAW_TEX_FMT)){
			byte[] rawData = t.metaData.getValue(ReservedMetaData.RAW_TEX_DATA).byteArrValue();
			String rawTexFmt = t.metaData.getValue(ReservedMetaData.RAW_TEX_FMT).stringValue();
			
			TextureFormat rawTF = TextureFormat.valueOfSafe(rawTexFmt);
			if (rawTF != null){
				t.format = TextureFormatHandler.getHandlerForFormat(rawTF);
				t.data = rawData;
			}
		}

		return t;
	}
}
