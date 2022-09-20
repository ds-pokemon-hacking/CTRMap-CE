package ctrmap.creativestudio.ngcs.io;

import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;

public interface IG3DFormatHandler {

	public static final int G3DFMT_IMPORT = (1 << 0);
	public static final int G3DFMT_IMPORT_HAS_EXCONFIG = (1 << 1);
	public static final int G3DFMT_IMPORT_EXDATA_NEEDS_SKELETON = (1 << 2);
	public static final int G3DFMT_IMPORT_EXDATA_NEEDS_CAMERA = (1 << 3);
	public static final int G3DFMT_IMPORT_EXDATA_NEEDS_MODEL = (1 << 4);

	public static final int G3DFMT_EXPORT = (1 << 16);
	public static final int G3DFMT_EXPORT_HAS_EXCONFIG = (1 << 17);
	public static final int G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON = (1 << 18);
	public static final int G3DFMT_EXPORT_EXDATA_NEEDS_CAMERA = (1 << 19);
	public static final int G3DFMT_EXPORT_EXDATA_NEEDS_MODEL = (1 << 20);

	public static IG3DFormatHandler findByFilter(ExtensionFilter filter, int needCheckAttrib, IG3DFormatHandler... handlers) {
		for (IG3DFormatHandler h : handlers) {
			if (needCheckAttrib == 0 || h.checkAttribute(needCheckAttrib)) {
				if (h.getExtensionFilter() == filter) {
					return h;
				}
			}
		}
		return null;
	}

	public int getAttributes();

	public default String getExportTargetName(G3DResource rsc) {
		return null;
	}

	public default boolean checkAttribute(int attr) {
		return (getAttributes() & attr) != 0;
	}

	public default boolean canImport() {
		return checkAttribute(G3DFMT_IMPORT);
	}

	public default boolean canExport() {
		return checkAttribute(G3DFMT_EXPORT);
	}

	public default boolean detectByExtension(FormatDetectorInput input) {
		boolean accept = getExtensionFilter().accepts(input.fileName);
		return accept;
	}

	public boolean detectInternals(FormatDetectorInput input);

	public ExtensionFilter getExtensionFilter();

	public G3DResource importFile(FSFile fsf, G3DIOProvider exData);

	public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData);
}
