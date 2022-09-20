package ctrmap.creativestudio.ngcs.plugins;

import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.creativestudio.ngcs.io.G3DIOProvider;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_IMPORT;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.FSFile;
import xstandard.gui.file.CommonExtensionFilters;
import xstandard.gui.file.ExtensionFilter;
import java.util.List;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;

public class NGCSStandardIOPlugin implements INGCSPlugin {

	public static IG3DFormatHandler UNKNOWN_FORMAT = new IG3DFormatHandler() {
		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT;
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return true;
		}

		@Override
		public ExtensionFilter getExtensionFilter() {
			return CommonExtensionFilters.ALL;
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			G3DResource res = new G3DResource();
			res.metaData.putValue(fsf.getName(), fsf.getBytes());
			return res;
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			List<MetaDataValue> l = res.metaData.getValues();
			for (MetaDataValue v : l) {
				if (v.getType() == MetaDataValue.Type.RAW_BYTES) {
					target.setBytes(v.byteArrValue());
					return;
				}
			}
		}
	};

	private static class IIOFmtHandler extends DefaultG3DFormatHandler {

		private static final int PNG_SIGNATURE = 0x474E5089;
		private static final String JFIF_SIGNATURE = "JFIF";
		private static final int BM_SIGNATURE = 0x4D42;

		private String formatNameIIO;

		public IIOFmtHandler(ExtensionFilter filter, String formatNameIIO) {
			super(filter);
			this.formatNameIIO = formatNameIIO;
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(TextureConverter.readTextureFromFile(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.textures.isEmpty()) {
				TextureConverter.writeTextureToFile(target, formatNameIIO, res.textures.get(0));
			}
		}

		private static boolean detectJPEG(byte[] fileStart) {
			return new String(fileStart, 6, 4).equals(JFIF_SIGNATURE);
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.magic4int == PNG_SIGNATURE || detectJPEG(input.first16Bytes);
		}

	}

	public static final IG3DFormatHandler PNG = new IIOFmtHandler(CommonExtensionFilters.PNG, "png");
	public static final IG3DFormatHandler JPG = new IIOFmtHandler(CommonExtensionFilters.JPG, "jpg");
	public static final IG3DFormatHandler BMP = new IIOFmtHandler(CommonExtensionFilters.BMP, "bmp");

	@Override
	public void registerFormats(NGCSJulietIface j) {
		j.registFormatSupport(CSG3DIOContentType.UNKNOWN, UNKNOWN_FORMAT);
		j.registFormatSupport(CSG3DIOContentType.TEXTURE, PNG, JPG, BMP);
	}
}
