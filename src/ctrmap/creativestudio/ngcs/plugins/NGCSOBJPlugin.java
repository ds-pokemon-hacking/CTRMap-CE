package ctrmap.creativestudio.ngcs.plugins;

import ctrmap.creativestudio.dialogs.NGOBJExportDialog;
import ctrmap.creativestudio.dialogs.OBJExportDialog;
import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.creativestudio.ngcs.io.G3DIOProvider;
import ctrmap.creativestudio.ngcs.io.IG3DFormatExHandler;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_HAS_EXCONFIG;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_IMPORT;
import ctrmap.creativestudio.ngcs.rtldr.NGCSContentAccessor;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.creativestudio.ngcs.rtldr.NGCSUIManager;
import ctrmap.formats.generic.xobj.OBJExportSettings;
import ctrmap.formats.generic.xobj.OBJFile;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import java.awt.Frame;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;
import ctrmap.formats.common.FormatIOExConfig;

public class NGCSOBJPlugin implements INGCSPlugin {

	public static final IG3DFormatHandler WAVEFRONT_OBJ = new IG3DFormatExHandler<FormatIOExConfig, OBJExportSettings>() {
		@Override
		public FormatIOExConfig popupImportExConfigDialog(Frame parent) {
			throw new UnsupportedOperationException();
		}

		@Override
		public OBJExportSettings popupExportExConfigDialog(Frame parent) {
			NGOBJExportDialog dlg = new NGOBJExportDialog(parent, true);
			dlg.setVisible(true);
			return dlg.getResult();
		}

		@Override
		public G3DResource importFile(FSFile file, G3DIOProvider exData) {
			return importFileEx(file, exData, null);
		}

		@Override
		public G3DResource importFileEx(FSFile file, G3DIOProvider exData, FormatIOExConfig config) {
			return new OBJFile(file).toGeneric();
		}

		@Override
		public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, OBJExportSettings config) {
			new OBJFile(rsc).write(target, config);
		}

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_HAS_EXCONFIG;
		}

		@Override
		public ExtensionFilter getExtensionFilter() {
			return OBJFile.EXTENSION_FILTER;
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return false;
		}
	};

	@Override
	public void registerFormats(NGCSJulietIface j) {
		j.registFormatSupport(WAVEFRONT_OBJ, CSG3DIOContentType.MODEL);
	}

	@Override
	public void registerUI(NGCSJulietIface j, Frame uiParent, NGCSContentAccessor contentAccessor) {
		j.addMenuItem("Export", NGCSUIManager.createExportMenuItem("Wavefront OBJ", OBJFile.EXTENSION_FILTER, uiParent, contentAccessor, new NGCSUIManager.ExportCallback() {
			@Override
			public void export(FSFile dest, Frame uiParent, NGCSContentAccessor contentAccessor) {
				new OBJExportDialog(uiParent, true, contentAccessor.getResource(), dest).setVisible(true);
			}
		}));
	}
}
