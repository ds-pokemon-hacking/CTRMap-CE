package ctrmap.creativestudio.ngcs.io;

import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import ctrmap.formats.common.FormatIOExConfig;

public class G3DIO {

	public static G3DResource readFile(FSFile file, G3DIOProvider provider, IG3DFormatHandler... formatHandlers) {
		try {
			IG3DFormatHandler handler = null;

			FormatDetectorInput fid = new FormatDetectorInput();
			fid.fileName = file.getName();

			for (IG3DFormatHandler hnd : formatHandlers) {
				if (hnd.canImport() && hnd.detectByExtension(fid)) {
					handler = hnd;
					break;
				}
			}

			if (handler == null) {
				DataIOStream stream = file.getDataIOStream();

				fid.magic4int = stream.readInt();
				stream.seek(0);
				fid.magic4str = stream.readPaddedString(4);
				stream.seek(0);
				fid.first16Bytes = stream.readBytes(16);
				fid.stream = stream;

				for (IG3DFormatHandler hnd : formatHandlers) {
					if (hnd.canImport() && hnd.detectInternals(fid)) {
						handler = hnd;
						break;
					}
				}

				stream.close();
			}

			if (handler != null) {
				if (provider != null) {
					provider.resetExdataCache();
				}

				if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_IMPORT_EXDATA_NEEDS_SKELETON)) {
					if (provider == null || provider.getSkeleton() == null) {
						return null;
					}
				}
				if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_IMPORT_EXDATA_NEEDS_MODEL)) {
					if (provider == null || provider.getModel() == null) {
						return null;
					}
				}
				if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_IMPORT_EXDATA_NEEDS_CAMERA)) {
					if (provider == null || provider.getRequestCamera() == null) {
						return null;
					}
				}

				if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_IMPORT_HAS_EXCONFIG)) {
					if (provider != null) {
						IG3DFormatExHandler exHandler = (IG3DFormatExHandler) handler;
						FormatIOExConfig cfg = provider.getExCfg(exHandler, false);
						if (cfg != null) {
							return exHandler.importFileEx(file, provider, cfg);
						} else {
							return null;
						}
					} else {
						return null;
					}
				} else {
					return handler.importFile(file, provider);
				}
			}
		} catch (Exception ex) {
			System.err.println("Error while reading file: " + file);
			ex.printStackTrace();
		}
		return null;
	}

	public static void writeFile(G3DResource resource, FSFile file, G3DIOProvider provider, IG3DFormatHandler handler) {
		if (resource != null && file != null && handler != null && handler.canExport()) {
			if (provider != null) {
				provider.resetExdataCache();
			}
			if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON)) {
				if (provider == null || provider.getSkeleton()== null) {
					return;
				}
			}
			if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_MODEL)) {
				if (provider == null || provider.getModel() == null) {
					return;
				}
			}
			if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_CAMERA)) {
				if (provider == null || provider.getCameraByName(handler.getExportTargetName(resource)) == null) {
					return;
				}
			}

			if (handler.checkAttribute(IG3DFormatHandler.G3DFMT_EXPORT_HAS_EXCONFIG)) {
				if (provider != null) {
					IG3DFormatExHandler exHandler = (IG3DFormatExHandler) handler;
					FormatIOExConfig cfg = provider.getExCfg(exHandler, true);
					if (cfg != null) {
						exHandler.exportResourceEx(resource, provider, file, cfg);
					}
				}
			} else {
				handler.exportResource(resource, file, provider);
			}
		}
	}
}
