package ctrmap.creativestudio.ngcs.io;

import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import java.awt.Frame;
import ctrmap.formats.common.FormatIOExConfig;

public interface IG3DFormatExHandler<I extends FormatIOExConfig, E extends FormatIOExConfig> extends IG3DFormatHandler {
	
	public I popupImportExConfigDialog(Frame parent);
	public E popupExportExConfigDialog(Frame parent);
	
	public G3DResource importFileEx(FSFile file, G3DIOProvider exData, I config);
	public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, E config);
	
	@Override
	public default G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
		return importFileEx(fsf, null, popupImportExConfigDialog(null));
	}
	
	@Override
	public default void exportResource(G3DResource rsc, FSFile target, G3DIOProvider exData) {
		exportResourceEx(rsc, null, target, popupExportExConfigDialog(null));
	}
}
