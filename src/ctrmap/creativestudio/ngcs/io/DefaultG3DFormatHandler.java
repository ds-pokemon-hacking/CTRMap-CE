package ctrmap.creativestudio.ngcs.io;

import xstandard.gui.file.ExtensionFilter;

public abstract class DefaultG3DFormatHandler implements IG3DFormatHandler {

	private ExtensionFilter filter;
	private int attributes;

	public DefaultG3DFormatHandler(ExtensionFilter filter, int attributes) {
		this.filter = filter;
		this.attributes = attributes;
	}
	
	public DefaultG3DFormatHandler(ExtensionFilter filter) {
		this(filter, G3DFMT_IMPORT | G3DFMT_EXPORT);
	}	

	@Override
	public int getAttributes() {
		return attributes;
	}

	@Override
	public ExtensionFilter getExtensionFilter() {
		return filter;
	}
}
