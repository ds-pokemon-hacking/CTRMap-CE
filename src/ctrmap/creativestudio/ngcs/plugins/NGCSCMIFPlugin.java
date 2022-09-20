package ctrmap.creativestudio.ngcs.plugins;

import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.DefaultG3DFormatHandler;
import ctrmap.creativestudio.ngcs.io.FormatDetectorInput;
import ctrmap.creativestudio.ngcs.io.G3DIOProvider;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import ctrmap.creativestudio.ngcs.rtldr.NGCSJulietIface;
import ctrmap.formats.generic.interchange.AnimeUtil;
import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.formats.generic.interchange.CameraUtil;
import ctrmap.formats.generic.interchange.LightUtil;
import ctrmap.formats.generic.interchange.MaterialUtil;
import ctrmap.formats.generic.interchange.MeshUtil;
import ctrmap.formats.generic.interchange.ModelUtil;
import ctrmap.formats.generic.interchange.SceneTemplateUtil;
import ctrmap.formats.generic.interchange.TextureUtil;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import ctrmap.creativestudio.ngcs.rtldr.INGCSPlugin;

public class NGCSCMIFPlugin implements INGCSPlugin {

	public static final IG3DFormatHandler IFF_TEXTURE = new DefaultG3DFormatHandler(TextureUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(TextureUtil.readTexture(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.textures.isEmpty()) {
				TextureUtil.writeTexture(res.textures.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(TextureUtil.TEXTURE_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_SCENE = new DefaultG3DFormatHandler(CMIFFile.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new CMIFFile(fsf).toGeneric();
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			new CMIFFile(res).write(target);
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(CMIFFile.MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_ANM_SKL = new DefaultG3DFormatHandler(AnimeUtil.SA_EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(AnimeUtil.readSkeletalAnime(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.skeletalAnimations.isEmpty()) {
				AnimeUtil.writeSklAnime(res.skeletalAnimations.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(AnimeUtil.SKL_ANIME_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_ANM_MAT = new DefaultG3DFormatHandler(AnimeUtil.MA_EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(AnimeUtil.readMaterialAnime(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.materialAnimations.isEmpty()) {
				AnimeUtil.writeMatAnime(res.materialAnimations.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(AnimeUtil.MAT_ANIME_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_ANM_CAM = new DefaultG3DFormatHandler(AnimeUtil.CA_EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(AnimeUtil.readCameraAnime(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.cameraAnimations.isEmpty()) {
				AnimeUtil.writeCameraAnime(res.cameraAnimations.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(AnimeUtil.CAM_ANIME_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_CAM = new DefaultG3DFormatHandler(CameraUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(CameraUtil.readCamera(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.cameras.isEmpty()) {
				CameraUtil.writeCamera(res.cameras.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(CameraUtil.CAMERA_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_MODEL = new DefaultG3DFormatHandler(ModelUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(ModelUtil.readModel(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.models.isEmpty()) {
				ModelUtil.writeModel(res.models.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(ModelUtil.MODEL_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_MATERIAL = new DefaultG3DFormatHandler(MaterialUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			G3DResource res = new G3DResource();
			Material mat = MaterialUtil.readMaterial(fsf);
			if (mat != null) {
				Model mdl = new Model();
				mdl.name = "Model";
				mdl.addMaterial(mat);
				res.addModel(mdl);
			}
			return res;
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.models.isEmpty()) {
				Model mdl = res.models.get(0);
				if (mdl != null) {
					MaterialUtil.writeMaterial(mdl.materials.get(0), target);
				}
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(MaterialUtil.MATERIAL_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_MESH = new DefaultG3DFormatHandler(MeshUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			G3DResource res = new G3DResource();
			Mesh poly = MeshUtil.readMesh(fsf);
			if (poly != null) {
				Model mdl = new Model();
				mdl.name = "Model";
				mdl.addMesh(poly);
				res.addModel(mdl);
			}
			return res;
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.models.isEmpty()) {
				Model mdl = res.models.get(0);
				if (mdl != null) {
					MeshUtil.writeMesh(mdl.meshes.get(0), target);
				}
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(MeshUtil.MESH_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_LIGHT = new DefaultG3DFormatHandler(LightUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(LightUtil.readLight(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.lights.isEmpty()) {
				LightUtil.writeLight(res.lights.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(LightUtil.LIGHT_MAGIC);
		}
	};

	public static final IG3DFormatHandler IFF_SCENE_TEMPLATE = new DefaultG3DFormatHandler(SceneTemplateUtil.EXTENSION_FILTER) {
		@Override
		public G3DResource importFile(FSFile fsf, G3DIOProvider exData) {
			return new G3DResource(SceneTemplateUtil.readSceneTemplate(fsf));
		}

		@Override
		public void exportResource(G3DResource res, FSFile target, G3DIOProvider exData) {
			if (!res.sceneTemplates.isEmpty()) {
				SceneTemplateUtil.writeSceneTemplate(res.sceneTemplates.get(0), target);
			}
		}

		@Override
		public boolean detectInternals(FormatDetectorInput input) {
			return input.isMagic4Str(SceneTemplateUtil.SCENE_TEMPLATE_MAGIC);
		}
	};

	@Override
	public void registerFormats(NGCSJulietIface j) {
		j.registFormatSupport(CSG3DIOContentType.MULTI_EX, IFF_SCENE);
		j.registFormatSupport(CSG3DIOContentType.MODEL, IFF_MODEL);
		j.registFormatSupport(CSG3DIOContentType.MESH, IFF_MESH);
		j.registFormatSupport(CSG3DIOContentType.LIGHT, IFF_LIGHT);
		j.registFormatSupport(CSG3DIOContentType.CAMERA, IFF_CAM);
		j.registFormatSupport(CSG3DIOContentType.TEXTURE, IFF_TEXTURE);
		j.registFormatSupport(CSG3DIOContentType.MATERIAL, IFF_MATERIAL);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_CAM, IFF_ANM_CAM);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_MAT, IFF_ANM_MAT);
		j.registFormatSupport(CSG3DIOContentType.ANIMATION_SKL, IFF_ANM_SKL);
		j.registFormatSupport(CSG3DIOContentType.SCENE_TEMPLATE, IFF_SCENE_TEMPLATE);
	}
}
