package ctrmap.creativestudio.ngcs.io;

import xstandard.gui.file.ExtensionFilter;

public abstract class DefaultG3DFormatHandlerSklEx extends DefaultG3DFormatHandler {

	public DefaultG3DFormatHandlerSklEx(ExtensionFilter filter, int extraAttributes) {
		super(filter, G3DFMT_EXPORT | G3DFMT_IMPORT | G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON | extraAttributes);
	}

	public DefaultG3DFormatHandlerSklEx(ExtensionFilter filter) {
		this(filter, 0);
	}
}
