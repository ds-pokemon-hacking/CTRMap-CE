package ctrmap.formats.generic.collada;

import ctrmap.formats.generic.collada.structs.*;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.model.Joint;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResourceType;
import ctrmap.renderer.util.MeshProcessor;
import ctrmap.renderer.util.PrimitiveConverter;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.math.MathEx;
import xstandard.math.vec.Quaternion;
import xstandard.math.vec.RGBA;
import xstandard.text.StringEx;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAE extends XmlFormat {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("COLLADA 1.4.1", "*.dae");

	private FSFile basePath;

	public DAEDict<DAEImage> images = new DAEDict<>();
	public DAEDict<DAEEffect> effects = new DAEDict<>();
	public DAEDict<DAEMaterial> materials = new DAEDict<>();
	public DAEDict<DAEGeometry> geometries = new DAEDict<>();
	public DAEDict<DAEController> controllers = new DAEDict<>();
	public DAEDict<DAECamera> cameras = new DAEDict<>();
	public DAEDict<DAELight> lights = new DAEDict<>();
	public DAEDict<DAEVisualScene> visualScenes = new DAEDict<>();

	public DAEDict<DAEAnimation> animations = new DAEDict<>();

	public DAEPostProcessConfig cfg = new DAEPostProcessConfig();

	public DAE(G3DResource scene) {
		this(scene, new DAEExportSettings());
	}

	public DAE(G3DResource scene, DAEExportSettings settings) {
		this(scene, new ArrayList<>(), settings);
	}

	public DAE(G3DResource scene, List<Skeleton> anmSkeletons, DAEExportSettings settings) {
		DAEConvState conv = new DAEConvState();

		DAEConvMemory<Texture, DAEImage> imgConv = new DAEConvMemory<>(conv, "image");
		DAEConvMemory<Material, DAEEffect> matEffConv = new DAEConvMemory<>(conv, "effect");
		DAEConvMemory<Material, DAEMaterial> matConv = new DAEConvMemory<>(conv, "material");
		DAEConvMemory<Mesh, DAEGeometry> meshConv = new DAEConvMemory<>(conv, "geometry");
		DAEConvMemory<Mesh, DAEController> meshConvCtrl = new DAEConvMemory<>(conv, "geometry");
		DAEConvMemory<Skeleton, DAEVisualScene> vsConv = new DAEConvMemory<>(conv, "visual_scene");
		DAEConvMemory<Joint, DAENode> skelConv = new DAEConvMemory<>(conv, "joint");
		DAEConvMemory<Camera, DAECamera> camConv = new DAEConvMemory<>(conv, "camera");
		DAEConvMemory<Light, DAELight> lightConvAmb = new DAEConvMemory<>(conv, "light-amb");
		DAEConvMemory<Light, DAELight> lightConvDif = new DAEConvMemory<>(conv, "light-dif");
		DAEConvMemory<Light, DAELight> lightConvSpc = new DAEConvMemory<>(conv, "light-spc");
		DAESIDConvMemory<Joint, DAENode> skelConvSID = new DAESIDConvMemory<>(conv, "joint");

		for (Texture tex : scene.textures) {
			DAEImage imgInfo = new DAEImage(tex);
			imgConv.put(tex, imgInfo);
			images.putNode(imgInfo);
		}

		RGBA blackRef = new RGBA(0, 0, 0, 255);

		for (Light l : scene.lights) {
			if (!l.ambientColor.equals(blackRef)) {
				DAELight amb = new DAELight(l, DAELight.DAELightType.AMBIENT);
				amb.name += "_ambient";
				lights.putNode(amb);
				lightConvAmb.put(l, amb);
			}
			if (!l.diffuseColor.equals(blackRef)) {
				DAELight diff = new DAELight(l, DAELight.DAELightType.DIRECTIONAL);
				diff.name += "_diffuse";
				lights.putNode(diff);
				lightConvDif.put(l, diff);
			}
			if (!l.diffuseColor.equals(blackRef)) {
				DAELight spec = new DAELight(l, DAELight.DAELightType.POINT);
				spec.name += "_specular";
				lights.putNode(spec);
				lightConvSpc.put(l, spec);
			}
		}

		HashSet<Skeleton> allSkeletons = new HashSet<>(anmSkeletons);
		HashSet<Skeleton> convertedSkeletons = new HashSet<>();

		Map<Mesh, DAENode> rootMeshJoints = new HashMap<>();

		for (Model mdl : scene.models) {
			allSkeletons.add(mdl.skeleton);
			convertedSkeletons.add(mdl.skeleton);
			for (Material mat : mdl.materials) {
				DAEEffect eff = new DAEEffect(mat, scene.textures, images, imgConv);

				for (Mesh mesh : mdl.meshes) {
					if (Objects.equals(mesh.materialName, mat.name)) {
						float lw = ReservedMetaData.getLineWidth(mesh.metaData, -1f);
						if (lw != -1f) {
							eff.lineWidth = lw;
							break;
						}
					}
				}

				matEffConv.put(mat, eff);
				DAEMaterial daeMat = new DAEMaterial(mat, eff);
				matConv.put(mat, daeMat);
				effects.putNode(eff);
				materials.putNode(daeMat);
			}

			DAEVisualScene visScene = new DAEVisualScene();
			visScene.name = sanitizeName(mdl.name);

			DAENode meshRoot = null;

			Joint dmyRootJoint = new Joint();
			dmyRootJoint.name = visScene.name;
			for (Joint j : mdl.skeleton.getJoints()) {
				if (j.name.equals(dmyRootJoint.name)) {
					dmyRootJoint.name += "_model";
					break;
				}
			}
			DAENode root = new DAENode(null, dmyRootJoint, skelConv, skelConvSID, settings);
			root.isNode = true;

			addLightsToVisScene(scene.lights, root, lightConvAmb, lightConvDif, lightConvSpc, skelConv, skelConvSID, settings);

			visScene.nodes.add(root);

			meshRoot = root;

			for (Joint j : mdl.skeleton) {
				if (j.parentName == null) {
					//root joint
					DAENode n = new DAENode(mdl.skeleton, j, skelConv, skelConvSID, settings);
					root.children.add(n);
				}
			}

			//Convert meshes
			for (Mesh mesh : mdl.meshes) {
				Mesh keyMesh = mesh;
				Joint dmyMeshJnt = new Joint();
				dmyMeshJnt.name = mesh.name;
				DAENode meshNode = new DAENode(mdl.skeleton, dmyMeshJnt, skelConv, skelConvSID, settings);
				meshNode.isNode = true;
				root.children.add(meshNode);
				rootMeshJoints.put(mesh, meshNode);

				if (mesh.skinningType == Mesh.SkinningType.RIGID) {
					mesh = new Mesh(mesh);
					MeshProcessor.transformRigidSkinningToSmooth(mesh, mdl.skeleton);
				}
				if (!DAEGeometry.isPrimitiveTypeCOLLADACompatible(mesh.primitiveType)) {
					//quadstrips - only primitive type not supported by COLLADA
					mesh = PrimitiveConverter.getTriOrQuadMesh(mesh);
				}
				DAEGeometry geom = new DAEGeometry(mesh);
				meshConv.put(mesh, geom);
				geometries.putNode(geom);
				if (mesh.hasBoneIndices) {
					DAEController controller = new DAEController(mesh, mdl.skeleton, geom, meshNode, skelConvSID);
					meshConvCtrl.put(keyMesh, controller);
					controllers.putNode(controller);
				}
			}

			//Instantiate meshes
			for (Mesh mesh : mdl.meshes) {
				DAENode rootMeshNode = rootMeshJoints.get(mesh);
				if (!mesh.hasBoneIndices) {
					//instantiate meshes as geometry
					rootMeshNode.instances.add(instantiateGeometryMesh(mesh, matConv, meshConv));
				} else {
					rootMeshNode.instances.add(instantiateControllerMesh(mesh, matConv, meshConvCtrl, meshRoot.getURL()));
				}
			}

			vsConv.put(mdl.skeleton, visScene);
			visualScenes.putNode(visScene);
		}

		Map<Camera, DAENode> camNodes = new HashMap<>();

		boolean needsLightScene = visualScenes.content.isEmpty() && !scene.lights.isEmpty();

		if (!scene.cameras.isEmpty()) {
			Skeleton camSceneSkl = new Skeleton();

			DAEVisualScene camScene = new DAEVisualScene();
			camScene.name = "CameraScene";
			vsConv.put(camSceneSkl, camScene);

			Joint camRootJnt = new Joint();
			camRootJnt.name = "CameraROOT";

			//We have to create a root camera joint to satisfy shitty software (e.g. Blender) that half-asses Y-up to retard-up conversion by rotating root nodes 90 degrees
			DAENode cameraROOT = new DAENode(camSceneSkl, camRootJnt, skelConv, skelConvSID, settings);
			camScene.nodes.add(cameraROOT);
			cameraROOT.isNode = true;

			if (needsLightScene) {
				addLightsToVisScene(scene.lights, cameraROOT, lightConvAmb, lightConvDif, lightConvSpc, skelConv, skelConvSID, settings);
				needsLightScene = false;
			}

			for (Camera cam : scene.cameras) {
				DAECamera daecam = new DAECamera(cam);
				camConv.put(cam, daecam);
				cameras.putNode(daecam);

				//create camera node
				Joint camJnt = new Joint();
				camJnt.name = cam.name;
				camJnt.position = cam.translation.clone();
				camJnt.rotation = cam.getRotation();
				camJnt.rotation.mul(MathEx.DEGREES_TO_RADIANS);
				camSceneSkl.addJoint(camJnt);
				DAENode camNode = new DAENode(camSceneSkl, camJnt, skelConv, skelConvSID, settings);
				camNode.isNode = true;
				if (!settings.doNotUseLookAt && cam.mode == Camera.Mode.LOOKAT) {
					//Blender: |!     LOOKAT and SKEW transformations are not supported yet.\n
					//The result of this is that the matrix is multiplied by a zero matrix (not identity)
					//Which messes up all of the camera's transforms
					camNode.lookAt = new Vec3f[]{
						cam.translation.clone(),
						cam.lookAtTarget.clone(),
						cam.lookAtUpVec.clone()
					};
				}
				camNodes.put(cam, camNode);

				//instantiate camera at node
				DAEInstance camInstance = new DAEInstance(DAEInstance.InstanceType.CAMERA, daecam);
				camNode.instances.add(camInstance);

				cameraROOT.children.add(camNode);
			}
			visualScenes.putNode(camScene);
		}

		DAEConvMemory<AbstractAnimation, DAEAnimation> anmConv = new DAEConvMemory<>(conv, "animation");

		for (SkeletalAnimation sklAnm : scene.skeletalAnimations) {
			Skeleton skl = sklAnm.skeleton;
			if (skl == null || allSkeletons.size() == 1) {
				//If there is just one skeleton, use it instead of the motion skeleton
				skl = findBestMatchingSkeleton(sklAnm, allSkeletons);
			}
			if (skl != null) {
				if (!convertedSkeletons.contains(skl)) {
					DAEVisualScene anmScene = new DAEVisualScene();
					anmScene.name = sanitizeName(sklAnm.name + "_skeleton");
					for (Joint j : skl) {
						if (j.parentName == null) {
							anmScene.nodes.add(new DAENode(skl, j, skelConv, skelConvSID, settings));
						}
					}
					vsConv.put(skl, anmScene);
					visualScenes.putNode(anmScene);
					convertedSkeletons.add(skl);
				}

				animations.putNode(new DAEAnimation(sklAnm, skl, anmConv, skelConv, settings));
			}
		}

		for (CameraAnimation camAnm : scene.cameraAnimations) {
			DAEAnimation daeAnm = new DAEAnimation(camAnm, scene.cameras, camConv, camNodes, anmConv, settings);
			animations.putNode(daeAnm);
		}

		for (VisibilityAnimation visAnm : scene.visibilityAnimations) {
			Model bestMatchModel = findBestMatchingModel(visAnm, scene.models);
			if (bestMatchModel != null) {
				DAEAnimation daeAnm = new DAEAnimation(visAnm, bestMatchModel.meshes, bestMatchModel.skeleton, anmConv, skelConv, rootMeshJoints);
				animations.putNode(daeAnm);
			}
		}

		if (needsLightScene) {
			Skeleton lightSceneSkl = new Skeleton();
			DAEVisualScene lightScene = new DAEVisualScene();
			lightScene.name = "Lights";

			Joint lightsRootJoint = new Joint();
			lightsRootJoint.name = "LightsROOT";

			DAENode rootNode = new DAENode(lightSceneSkl, lightsRootJoint, skelConv, skelConvSID, settings);
			rootNode.isNode = true;
			addLightsToVisScene(scene.lights, rootNode, lightConvAmb, lightConvDif, lightConvSpc, skelConv, skelConvSID, settings);

			lightScene.nodes.add(rootNode);

			vsConv.put(lightSceneSkl, lightScene);
			visualScenes.putNode(lightScene);
		}
	}

	private static void addLightsToVisScene(
		List<Light> lights,
		DAENode root,
		DAEConvMemory<Light, DAELight> lightConvAmb,
		DAEConvMemory<Light, DAELight> lightConvDif,
		DAEConvMemory<Light, DAELight> lightConvSpc,
		DAEConvMemory<Joint, DAENode> skelConv,
		DAESIDConvMemory<Joint, DAENode> skelConvSID,
		DAEExportSettings settings
	) {
		for (Light l : lights) {
			//instantiate lights
			DAELight amb = lightConvAmb.findByInput(l);
			DAELight dif = lightConvDif.findByInput(l);
			DAELight spc = lightConvSpc.findByInput(l);
			if (amb != null || dif != null || spc != null) {
				Joint lightNode = new Joint();
				lightNode.name = l.name;
				lightNode.position = l.position;
				Quaternion lightRotQuat = new Quaternion();
				lightRotQuat.rotationTo(new Vec3f(0f, 0f, -1f), l.direction);
				lightNode.rotation = lightRotQuat.getEulerRotation();

				DAENode lightNodeDae = new DAENode(null, lightNode, skelConv, skelConvSID, settings);
				lightNodeDae.isNode = true;

				if (amb != null) {
					lightNodeDae.instances.add(new DAEInstance(DAEInstance.InstanceType.LIGHT, amb));
				}
				if (dif != null) {
					lightNodeDae.instances.add(new DAEInstance(DAEInstance.InstanceType.LIGHT, dif));
				}
				if (spc != null) {
					lightNodeDae.instances.add(new DAEInstance(DAEInstance.InstanceType.LIGHT, spc));
				}

				root.children.add(lightNodeDae);
			}
		}

	}

	private static Model findBestMatchingModel(VisibilityAnimation anm, Iterable<Model> models) {
		int bestMatch = -1;
		Model result = null;
		List<String> namesJnt = new ArrayList<>();
		List<String> namesMesh = new ArrayList<>();
		for (VisibilityBoneTransform bt : anm.tracks) {
			if (bt.target == VisibilityBoneTransform.Target.MESH) {
				namesMesh.add(bt.name);
			} else {
				namesJnt.add(bt.name);
			}
		}
		for (Model mdl : models) {
			int match = 0;
			for (Joint j : mdl.skeleton) {
				if (namesJnt.contains(j.name)) {
					match++;
				}
			}
			for (Mesh mesh : mdl.meshes) {
				if (namesMesh.contains(mesh.name)) {
					match++;
				}
			}
			if (match > bestMatch) {
				result = mdl;
				bestMatch = match;
			}
		}
		return result;
	}

	private static Skeleton findBestMatchingSkeleton(SkeletalAnimation anm, Collection<Skeleton> skeletons) {
		int bestMatch = -1;
		Skeleton result = null;
		List<String> names = new ArrayList<>();
		for (SkeletalBoneTransform bt : anm.bones) {
			names.add(bt.name);
		}
		for (Skeleton skl : skeletons) {
			int match = 0;
			for (Joint j : skl) {
				if (names.contains(j.name)) {
					match++;
				}
			}
			if (match > bestMatch) {
				result = skl;
				bestMatch = match;
			}
		}
		return result;
	}

	private DAEInstance instantiateGeometryMesh(Mesh mesh, DAEConvMemory<Material, DAEMaterial> matConv, DAEConvMemory<Mesh, DAEGeometry> meshConv) {
		DAEInstance inst_geometry = new DAEInstance(DAEInstance.InstanceType.GEOMETRY, meshConv.findByInput(mesh));
		DAEMaterial mat = matConv.findByInputName(mesh.materialName);
		if (mat != null) {
			inst_geometry.binds.add(createMatBind(mesh, mat));
		}
		return inst_geometry;
	}

	private DAEBind createMatBind(Mesh mesh, DAEMaterial mat) {
		DAEBind matBind = new DAEBind(DAEBind.BindType.MATERIAL);
		DAEInstance inst_material = new DAEInstance(DAEInstance.InstanceType.MATERIAL, mat);
		inst_material.targetSymbol = mesh.materialName;
		inst_material.symbolReplacement = mat.getURL();
		if (mesh.getActiveUVLayerCount() > 1) {
			for (int i = 0; i < mesh.hasUV.length; i++) {
				if (mesh.hasUV(i)) {
					DAEBind vertInputBind = new DAEBind(DAEBind.BindType.VERTEX_INPUT);

					vertInputBind.bindName = "UVSet" + i;
					vertInputBind.targetBindInputSemantic = "TEXCOORD";
					vertInputBind.targetBindInputSetNo = i;

					inst_material.binds.add(vertInputBind);
				}
			}
		}
		matBind.subInstances.add(inst_material);
		return matBind;
	}

	private DAEInstance instantiateControllerMesh(Mesh mesh, DAEConvMemory<Material, DAEMaterial> matConv, DAEConvMemory<Mesh, DAEController> meshConv, String sklRootName) {
		DAEInstance inst_controller = new DAEInstance(DAEInstance.InstanceType.CONTROLLER, meshConv.findByInput(mesh), sklRootName);
		DAEMaterial mat = matConv.findByInputName(mesh.materialName);
		if (mat != null) {
			inst_controller.binds.add(createMatBind(mesh, mat));
		}
		return inst_controller;
	}

	public DAE(File f) {
		this(new DiskFile(f));
	}

	public DAE(FSFile f) {
		basePath = f.getParent();
		Element root = getNormalizedRoot(f);

		Element asset = getParamElement(root, "asset");
		if (asset != null) {
			String upAxis = getParamNodeValue(asset, "up_axis");
			if (upAxis != null) {
				switch (upAxis) {
					case "Y_UP":
						cfg.upAxis = DAEPostProcessConfig.DAEUpAxis.Y_UP;
						break;
					case "Z_UP":
						cfg.upAxis = DAEPostProcessConfig.DAEUpAxis.Z_UP;
						break;
				}
			}
			String authoring_tool = getParamNodeValue(asset, "contributor", "authoring_tool");
			if (authoring_tool != null && authoring_tool.startsWith("Blender")) {
				cfg.isBlenderAny = true;
				int versionStart = StringEx.indexOfFirstNonWhitespaceAfterWhitespace(authoring_tool, "Blender".length());
				if (versionStart != -1) {
					int versionEnd = StringEx.indexOfFirstWhitespace(authoring_tool, versionStart);
					if (versionEnd == -1) {
						versionEnd = authoring_tool.length();
					}
					String[] versions = authoring_tool.substring(versionStart, versionEnd).split("\\.");

					if (versions.length > 0) {
						boolean isShitBlender = false;

						int major = Integer.parseInt(versions[0]);
						if (major == 2) {
							String minorSrc = versions[1];
							if (minorSrc.length() == 1) {
								minorSrc += "0";
							}
							int minorVersion = Integer.parseInt(minorSrc);
							if (minorVersion >= 80) {
								/*
								Blender 2.8+ have yet again fucked up the COLLADA exporter. So bad that it, in fact, imports wrong to Blender 2.79, their own program.
								We can work around it by forcing the model not to rotate to Y up (the skeleton, however, should be rotated).
								 */
								isShitBlender = true;
							}
						}
						else if (major == 3) {
							isShitBlender = true;
						}

						cfg.isShitBlender = isShitBlender;
					}
				}
			}
		}

		List<Element> imageElems = getLibraryContentDataElems(root, "library_images", "image");
		for (Element image : imageElems) {
			images.putNode(new DAEImage(image));
		}

		List<Element> effectElems = getLibraryContentDataElems(root, "library_effects", "effect");
		for (Element eff : effectElems) {
			effects.putNode(new DAEEffect(eff));
		}

		List<Element> materialElems = getLibraryContentDataElems(root, "library_materials", "material");
		for (Element mat : materialElems) {
			materials.putNode(new DAEMaterial(mat));
		}

		List<Element> geomElems = getLibraryContentDataElems(root, "library_geometries", "geometry");
		for (Element geom : geomElems) {
			geometries.putNode(new DAEGeometry(geom));
		}

		List<Element> vsElems = getLibraryContentDataElems(root, "library_visual_scenes", "visual_scene");
		for (Element vs : vsElems) {
			DAEVisualScene dvs = new DAEVisualScene(vs);
			dvs.name = FSUtil.getFileNameWithoutExtension(f.getName() + "_" + dvs.name);
			visualScenes.putNode(dvs);
		}

		List<Element> conElems = getLibraryContentDataElems(root, "library_controllers", "controller");
		for (Element con : conElems) {
			controllers.putNode(new DAEController(con));
		}

		List<Element> animeElems = getLibraryContentDataElems(root, "library_animations", "animation");
		for (Element anm : animeElems) {
			animations.putNode(new DAEAnimation(anm));
		}

		List<Element> camElems = getLibraryContentDataElems(root, "library_cameras", "camera");
		for (Element cam : camElems) {
			cameras.putNode(new DAECamera(cam, cfg.isBlenderAny));
		}

		List<Element> lightElems = getLibraryContentDataElems(root, "library_lights", "light");
		for (Element light : lightElems) {
			lights.putNode(new DAELight(light));
		}
	}

	public static String idFromUrl(String url) {
		if (url.startsWith("#")) {
			url = url.substring(1);
		}
		return url;
	}

	public static void toYUp(Vec3f... zUp) {
		for (Vec3f v : zUp) {
			float y = v.z;
			v.z = -v.y;
			v.y = y;
		}
	}

	public G3DResource toGeneric() {
		//Textures
		G3DResource res = new G3DResource();

		for (DAEImage image : images) {
			res.addTexture(image.toTexture(basePath));
		}

		Map<DAEVisualScene, Skeleton> skeletons = new HashMap<>();

		//Models... sigh		
		for (DAEVisualScene scn : visualScenes) {
			Model mdl = scn.getModel(this, cfg);
			if (!mdl.meshes.isEmpty()) {
				res.addModel(mdl);
			}

			res.addCameras(scn.getCameras(this, mdl.skeleton, cfg));
			res.addLights(scn.getLights(this, mdl.skeleton, cfg));

			skeletons.put(scn, mdl.skeleton);
		}

		res.addAnimes(DAEAnimationPacker.createAnimations(this, skeletons, cfg));

		for (Model model : res.models) {
			for (Mesh mesh : model.meshes) {
				Material mat = mesh.getMaterial(model);
				if (mat != null) {
					for (TextureMapper m : mat.textures) {
						Texture tex = (Texture) res.getNamedResource(m.textureName, G3DResourceType.TEXTURE);
						if (tex != null) {
							if (tex.format.hasAlpha() && !mat.alphaTest.enabled) {
								//The texture converter automatically creates RGB8 textures when no alpha is present
								mat.alphaTest.enabled = true;
								mat.alphaTest.reference = 0;
								mat.alphaTest.testFunction = MaterialParams.TestFunction.GREATER;
							}
						}
					}
				}
			}
		}

		return res;
	}

	public void writeToFile(FSFile dest, FSFile textureDir) {
		Document doc = createDocument();
		Element root = doc.createElementNS("http://www.collada.org/2005/11/COLLADASchema", "COLLADA");
		root.setAttribute("version", "1.4.1");
		doc.appendChild(root);

		Element assetElem = doc.createElement("asset");

		Element contributor = doc.createElement("contributor");

		contributor.appendChild(createSimpleTextContentElem(doc, "author", System.getProperty("user.name")));
		contributor.appendChild(createSimpleTextContentElem(doc, "authoring_tool", "CreativeStudio | NGCS-COLLADA 1.0"));

		assetElem.appendChild(contributor);
		assetElem.appendChild(createSimpleTextContentElem(doc, "up_axis", "Y_UP"));

		root.appendChild(assetElem);

		if (textureDir != null) {
			String destAsNoDir = dest.getPath();
			if (destAsNoDir.endsWith("/")) {
				//trim trailing slash for correct path relativization
				//(nonexistent file is treated as dir by windows)
				destAsNoDir = destAsNoDir.substring(0, destAsNoDir.length() - 1);
			}

			textureDir.mkdirs();

			for (DAEImage imgTex : images) {
				if (imgTex.texture != null) {
					FSFile texDest = textureDir.getChild(FSUtil.getFileName(imgTex.initFrom));
					imgTex.initFrom = FSFile.getPathRelativeTo(texDest.getPath(), destAsNoDir);
					TextureConverter.writeTextureToFile(texDest, "png", imgTex.texture);
				}
			}
		}

		root.appendChild(createLibrary(doc, "library_cameras", cameras));
		root.appendChild(createLibrary(doc, "library_lights", lights));

		root.appendChild(createLibrary(doc, "library_images", images));

		root.appendChild(createLibrary(doc, "library_effects", effects));
		root.appendChild(createLibrary(doc, "library_materials", materials));

		Element libGeom = doc.createElement("library_geometries");
		Element libCtrl = doc.createElement("library_controllers");

		HashSet<DAEGeometry> skinnedGeometries = new HashSet<>();

		for (DAEController c : controllers) {
			DAEGeometry geom = geometries.getByUrl(c.meshUrl);
			if (geom != null) {
				List<DAEGeometry.DAEVertex> verts = new ArrayList<>();
				libGeom.appendChild(geom.createElement(doc, verts));
				libCtrl.appendChild(c.createElement(doc, verts));
				skinnedGeometries.add(geom);
			}
		}

		for (DAEGeometry g : geometries) {
			if (!skinnedGeometries.contains(g)) {
				libGeom.appendChild(g.createElement(doc, null));
			}
		}

		root.appendChild(libGeom);
		root.appendChild(libCtrl);

		root.appendChild(createLibrary(doc, "library_animations", animations));

		root.appendChild(createLibrary(doc, "library_visual_scenes", visualScenes));

		Element scene = doc.createElement("scene");

		for (DAEVisualScene vs : visualScenes) {
			DAEInstance inst = new DAEInstance(DAEInstance.InstanceType.VISUAL_SCENE, vs);
			scene.appendChild(inst.createElement(doc));
		}

		root.appendChild(scene);

		writeDocumentToFile(doc, dest);
	}

	private Element createLibrary(Document doc, String libName, DAEDict<? extends DAESerializable> dict) {
		Element elem = doc.createElement(libName);

		for (DAESerializable e : dict) {
			elem.appendChild(e.createElement(doc));
		}

		return elem;
	}
}
