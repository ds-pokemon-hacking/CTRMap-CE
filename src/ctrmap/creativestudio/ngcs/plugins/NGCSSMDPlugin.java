package ctrmap.creativestudio.ngcs.plugins;

import ctrmap.creativestudio.dialogs.SMDExportDialog;
import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandlerSklEx;
import ctrmap.creativestudio.ngcs.io.DefaultG3DImportOnlyFormatHandler;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.creativestudio.ngcs.io.G3DIOProvider;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import ctrmap.creativestudio.ngcs.rtldr.NGCSContentAccessor;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.creativestudio.ngcs.rtldr.NGCSUIManager;
import ctrmap.formats.generic.source.SMD;
import ctrmap.formats.generic.xobj.OBJFile;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import java.awt.Frame;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;

public class NGCSSMDPlugin implements INGCSPlugin {

	public static final IG3DFormatHandler STUDIOMDL_IMPORT_BOTH = new DefaultG3DImportOnlyFormatHandler(SMD.EXTENSION_FILTER) {

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new SMD(fsf).toGeneric();
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return false;
		}
	};

	public static final IG3DFormatHandler STUDIOMDL_MDL = new DefaultG3DFormatHandler(SMD.EXTENSION_FILTER) {
		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			new SMD(res.models.get(0)).writeToFile(target);
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new SMD(fsf).toGeneric();
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return false;
		}
	};

	public static final IG3DFormatHandler STUDIOMDL_ANM = new DefaultG3DFormatHandlerSklEx(SMD.EXTENSION_FILTER) {
		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.skeletalAnimations.isEmpty()) {
				new SMD(exData.getSkeleton(), res.skeletalAnimations.get(0)).writeToFile(target);
			}
		}

		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new SMD(fsf).toGeneric();
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return false;
		}
	};

	@Override
	public void registerFormats(NGCSJulietIface j) {
		j.registFormatSupport(CSG3DIOContentType.MULTI_EX, STUDIOMDL_IMPORT_BOTH);
		j.registFormatSupport(CSG3DIOContentType.MODEL, STUDIOMDL_MDL);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_SKL, STUDIOMDL_ANM);
	}

	@Override
	public void registerUI(NGCSJulietIface j, Frame uiParent, NGCSContentAccessor contentAccessor) {
		j.addMenuItem("Export", NGCSUIManager.createExportMenuItem("Valve StudioMdl", SMD.EXTENSION_FILTER, uiParent, contentAccessor, new NGCSUIManager.ExportCallback() {
			@Override
			public void export(FSFile dest, Frame uiParent, NGCSContentAccessor contentAccessor) {
				SMDExportDialog dlg = new SMDExportDialog(uiParent, true, contentAccessor.getModels());
				dlg.setVisible(true);
				SMDExportDialog.CSSMDExportSettings result = dlg.getResult();
				if (result != null) {
					FSFile texDir = null;

					if (result.exportTextures) {
						texDir = dest.getParent();
						if (result.separateTextures) {
							texDir = texDir.getChild("Textures");
							texDir.mkdir();
						}
					}

					SMD smd = new SMD(result.models, contentAccessor.getTextures());
					smd.writeToFile(dest, texDir);
				}
			}
		}));
	}
}
