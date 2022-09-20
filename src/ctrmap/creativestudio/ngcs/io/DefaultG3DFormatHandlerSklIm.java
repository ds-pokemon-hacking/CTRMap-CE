package ctrmap.creativestudio.ngcs.io;

import xstandard.gui.file.ExtensionFilter;

public abstract class DefaultG3DFormatHandlerSklIm extends DefaultG3DFormatHandler {

	public DefaultG3DFormatHandlerSklIm(ExtensionFilter filter, int attributes) {
		super(filter, attributes);
	}

	public DefaultG3DFormatHandlerSklIm(ExtensionFilter filter) {
		super(filter, G3DFMT_EXPORT | G3DFMT_IMPORT | G3DFMT_IMPORT_EXDATA_NEEDS_SKELETON);
	}
}
