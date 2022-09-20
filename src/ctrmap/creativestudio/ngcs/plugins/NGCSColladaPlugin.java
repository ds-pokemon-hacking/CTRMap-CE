package ctrmap.creativestudio.ngcs.plugins;

import ctrmap.creativestudio.dialogs.DAEExportDialog;
import ctrmap.creativestudio.dialogs.DAESimpleExportDialog;
import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.creativestudio.ngcs.io.G3DIOProvider;
import ctrmap.creativestudio.ngcs.io.IG3DFormatExHandler;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_CAMERA;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_MODEL;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_EXPORT_HAS_EXCONFIG;
import static ctrmap.creativestudio.ngcs.io.IG3DFormatHandler.G3DFMT_IMPORT;
import ctrmap.creativestudio.ngcs.rtldr.NGCSContentAccessor;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.creativestudio.ngcs.rtldr.NGCSUIManager;
import ctrmap.formats.generic.collada.DAE;
import ctrmap.formats.generic.collada.DAEExportSettings;
import ctrmap.formats.generic.collada.structs.DAEAnimation;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.util.ArraysEx;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.JMenuItem;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;
import ctrmap.formats.common.FormatIOExConfig;

public class NGCSColladaPlugin implements INGCSPlugin {

	public static final IG3DFormatHandler COLLADA = new DefaultG3DFormatHandler(DAE.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new DAE(fsf).toGeneric();
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			new DAE(res).writeToFile(target, null);
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return false;
		}
	};

	private static abstract class ColladaExporterExBase implements IG3DFormatExHandler<FormatIOExConfig, DAEExportSettings> {

		@Override
		public FormatIOExConfig popupImportExConfigDialog(Frame parent) {
			return null;
		}

		@Override
		public DAEExportSettings popupExportExConfigDialog(Frame parent) {
			DAESimpleExportDialog dlg = new DAESimpleExportDialog(parent, true);
			dlg.setVisible(true);
			return dlg.getResult();
		}

		@Override
		public G3DResource importFileEx(FSFile file, G3DIOProvider exData, FormatIOExConfig config) {
			return new DAE(file).toGeneric();
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return false;
		}

		@Override
		public ExtensionFilter getExtensionFilter() {
			return DAE.EXTENSION_FILTER;
		}
	}

	public static final IG3DFormatHandler COLLADA_ANIMVIS_EX = new ColladaExporterExBase() {

		@Override
		public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, DAEExportSettings config) {
			if (!rsc.visibilityAnimations.isEmpty()) {
				G3DResource mergeRes = new G3DResource();
				mergeRes.merge(rsc);
				mergeRes.addModel(exData.getModel());
				new DAE(mergeRes, config).writeToFile(target, null);
			}
		}

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_EXDATA_NEEDS_MODEL | G3DFMT_EXPORT_HAS_EXCONFIG;
		}
	};

	public static final IG3DFormatHandler COLLADA_ANIMSKL_EX = new ColladaExporterExBase() {

		@Override
		public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, DAEExportSettings config) {
			if (!rsc.skeletalAnimations.isEmpty()) {
				new DAE(rsc, ArraysEx.asList(exData.getSkeleton()), config).writeToFile(target, null);
			}
		}

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_EXDATA_NEEDS_SKELETON | G3DFMT_EXPORT_HAS_EXCONFIG;
		}
	};

	public static final IG3DFormatHandler COLLADA_ANIMCAM_EX = new ColladaExporterExBase() {

		@Override
		public void exportResourceEx(G3DResource rsc, G3DIOProvider exData, FSFile target, DAEExportSettings config) {
			if (!rsc.cameraAnimations.isEmpty()) {
				G3DResource mergeRsc = new G3DResource();
				mergeRsc.merge(rsc);
				mergeRsc.addCamera(exData.getCameraByName(getExportTargetName(rsc)));
				new DAE(rsc, config).writeToFile(target, null);
			}
		}

		@Override
		public String getExportTargetName(G3DResource rsc) {
			if (!rsc.cameraAnimations.isEmpty()) {
				CameraAnimation anm = rsc.cameraAnimations.get(0);
				if (!anm.transforms.isEmpty()) {
					return anm.transforms.get(0).name;
				}
			}
			return null;
		}

		@Override
		public int getAttributes() {
			return G3DFMT_IMPORT | G3DFMT_EXPORT | G3DFMT_EXPORT_EXDATA_NEEDS_CAMERA | G3DFMT_EXPORT_HAS_EXCONFIG;
		}
	};

	@Override
	public void registerFormats(NGCSJulietIface j) {
		j.registFormatSupport(COLLADA, CSG3DIOContentType.MODEL, CSG3DIOContentType.CAMERA, CSG3DIOContentType.LIGHT, CSG3DIOContentType.ANIMATION_MULTI_EX);
		j.registFormatSupport(COLLADA_ANIMCAM_EX, CSG3DIOContentType.ANIMATION_CAM);
		j.registFormatSupport(COLLADA_ANIMSKL_EX, CSG3DIOContentType.ANIMATION_SKL);
		j.registFormatSupport(COLLADA_ANIMVIS_EX, CSG3DIOContentType.ANIMATION_VIS);
	}

	@Override
	public void registerUI(NGCSJulietIface j, Frame uiParent, NGCSContentAccessor contentAccessor) {
		j.addMenuItem("Import", NGCSUIManager.createSimpleImportMenuItem("COLLADA", DAE.EXTENSION_FILTER, contentAccessor));
		j.addMenuItem("Export", NGCSUIManager.createExportMenuItem("COLLADA", DAE.EXTENSION_FILTER, uiParent, contentAccessor, new ExportUICallback()));
	}

	private static class ExportUICallback implements NGCSUIManager.ExportCallback {

		@Override
		public void export(FSFile dest, Frame uiParent, NGCSContentAccessor contentAccessor) {
			DAEExportDialog dlg = new DAEExportDialog(
				uiParent,
				true,
				contentAccessor.getModels(),
				contentAccessor.getSklAnime(),
				contentAccessor.getCamAnime(),
				contentAccessor.getVisAnime()
			);
			dlg.setVisible(true);
			DAEExportDialog.CSDAEExportSettings settings = dlg.getResult();
			if (settings != null) {
				G3DResource mainResource = new G3DResource();
				if (settings.exportMdl) {
					mainResource.addModels(settings.modelsToExport);
				}
				if (settings.exportAnm) {
					if (!settings.animeToSeparateFiles) {
						mainResource.addAnimes(settings.animeToExport);
					}
				}
				if (settings.exportTex) {
					if (settings.texMappedOnly && !mainResource.models.isEmpty()) {
						HashSet<String> hasTextures = new HashSet<>();
						List<Texture> texSources = contentAccessor.getTextures();
						List<TextureMapper> texMappers = new ArrayList<>();
						for (Material mat : mainResource.materials()) {
							texMappers.addAll(mat.textures);
							texMappers.addAll(mat.LUTs);
						}
						for (TextureMapper texMapper : texMappers) {
							if (!hasTextures.contains(texMapper.textureName)) {
								Texture tex = Scene.getNamedObject(texMapper.textureName, texSources);
								if (tex != null) {
									mainResource.addTexture(tex);
									hasTextures.add(texMapper.textureName);
								}
							}
						}
					} else {
						mainResource.textures.addAll(contentAccessor.getTextures());
					}
				}
				if (settings.exportCam) {
					mainResource.cameras.addAll(contentAccessor.getCameras());
				}
				if (settings.exportLight) {
					mainResource.lights.addAll(contentAccessor.getLights());
				}

				FSFile texDir;
				if (settings.dirSepTex) {
					texDir = dest.getParent().getChild("Textures");
					texDir.mkdir();
				} else {
					texDir = dest.getParent();
				}

				DAEExportSettings daeSettings = new DAEExportSettings();
				daeSettings.bakeAnimations = settings.anmBake && settings.exportAnm;

				List<Skeleton> skeletons = new ArrayList<>();
				for (Model mdl : contentAccessor.getModels()) {
					skeletons.add(mdl.skeleton);
				}

				DAE mainDAE = new DAE(mainResource, skeletons, daeSettings);
				mainDAE.writeToFile(dest, texDir);

				if (settings.exportAnm && settings.animeToSeparateFiles) {
					FSFile anmDir;
					if (settings.dirSepAnm) {
						anmDir = dest.getParent().getChild("Animations");
						anmDir.mkdir();
					} else {
						anmDir = dest.getParent();
					}

					HashSet<String> usedAnmNames = new HashSet<>();

					for (AbstractAnimation a : settings.animeToExport) {
						String anmFileName = a.name;

						if (usedAnmNames.contains(anmFileName)) {
							anmFileName = anmFileName + "_" + DAEAnimation.getAnmTypeSuffixShort(a);
						}

						String name = anmFileName;

						int index = 2;
						while (usedAnmNames.contains(name)) {
							name = anmFileName + "_" + index;
							index++;
						}

						anmFileName = name;

						usedAnmNames.add(anmFileName);

						DAE anmDAE = new DAE(new G3DResource(a), skeletons, daeSettings);
						anmDAE.writeToFile(anmDir.getChild(anmFileName + DAE.EXTENSION_FILTER.getPrimaryExtension()), texDir);
					}
				}
			}
		}

	}
}
