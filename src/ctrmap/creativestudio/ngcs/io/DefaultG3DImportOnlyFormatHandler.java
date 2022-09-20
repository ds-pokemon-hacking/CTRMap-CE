package ctrmap.creativestudio.ngcs.io;

import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;

public abstract class DefaultG3DImportOnlyFormatHandler extends DefaultG3DFormatHandler {

	public DefaultG3DImportOnlyFormatHandler(ExtensionFilter filter, int attributes) {
		super(filter, attributes);
	}
	
	public DefaultG3DImportOnlyFormatHandler(ExtensionFilter filter) {
		this(filter, G3DFMT_IMPORT);
	}

	@Override
	public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
