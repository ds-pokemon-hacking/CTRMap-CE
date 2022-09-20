package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class CSRootSceneNode extends CSNode {

	public static final int RESID = 0x420002;

	private G3DResource rsc;

	public CSRootSceneNode(NGCS cs, CSJTree tree) {
		super(tree);
		this.rsc = cs.getScene().resource;

		ContainerNode models = new ContainerNode("Models", CSNodeContentType.MODEL, rsc.models, -1, tree);
		addChild(models);

		ContainerNode textures = new ContainerNode("Textures", CSNodeContentType.TEXTURE, rsc.textures, -1, tree);
		addChild(textures);

		ContainerNode animationsS = new ContainerNode("Skeletal Animations", CSNodeContentType.ANIMATION_S, rsc.skeletalAnimations, -1, tree);
		addChild(animationsS);

		ContainerNode animationsM = new ContainerNode("Material Animations", CSNodeContentType.ANIMATION_M, rsc.materialAnimations, -1, tree);
		addChild(animationsM);

		ContainerNode animationsV = new ContainerNode("Visibility Animations", CSNodeContentType.ANIMATION_V, rsc.visibilityAnimations, -1, tree);
		addChild(animationsV);

		ContainerNode animationsC = new ContainerNode("Camera Animations", CSNodeContentType.ANIMATION_C, rsc.cameraAnimations, -1, tree);
		addChild(animationsC);

		ContainerNode cameras = new ContainerNode("Cameras", CSNodeContentType.CAMERA, rsc.cameras, -1, tree);
		addChild(cameras);

		ContainerNode lights = new ContainerNode("Lights", CSNodeContentType.LIGHT, rsc.lights, -1, tree);
		addChild(lights);

		ContainerNode scenes = new ContainerNode("Scenes", CSNodeContentType.SCENE_TEMPLATE, rsc.sceneTemplates, -1, tree);
		addChild(scenes);

		ContainerNode others = new ContainerNode("Other files", CSNodeContentType.OTHER, cs.getOthers(), -1, tree);
		addChild(others);

		for (Model mdl : rsc.models) {
			models.addChild(new ModelNode(mdl, tree));
		}

		for (Texture tex : rsc.textures) {
			textures.addChild(new TextureNode(tex, tree));
		}

		for (AbstractAnimation a : rsc.skeletalAnimations) {
			animationsS.addChild(new AnimationNode(rsc.skeletalAnimations, a, tree));
		}

		for (AbstractAnimation a : rsc.materialAnimations) {
			animationsM.addChild(new AnimationNode(rsc.materialAnimations, a, tree));
		}

		for (AbstractAnimation a : rsc.visibilityAnimations) {
			animationsV.addChild(new AnimationNode(rsc.visibilityAnimations, a, tree));
		}

		for (AbstractAnimation a : rsc.cameraAnimations) {
			animationsC.addChild(new AnimationNode(rsc.cameraAnimations, a, tree));
		}

		for (Camera cam : rsc.cameras) {
			cameras.addChild(new CameraNode(cam, tree));
		}

		for (Light l : rsc.lights) {
			lights.addChild(new LightNode(l, tree));
		}

		for (G3DSceneTemplate t : rsc.sceneTemplates) {
			scenes.addChild(new SceneTemplateNode(t, tree));
		}

		for (CMIFFile.OtherFile o : cs.getOthers()) {
			others.addChild(new OtherFileNode(o, tree));
		}

		rsc.models.addListener(new CSNodeListener<Model>(models) {
			@Override
			protected CSNode createNode(Model elem) {
				return new ModelNode(elem, tree);
			}
		});
		rsc.textures.addListener(new CSNodeListener<Texture>(textures) {
			@Override
			protected CSNode createNode(Texture elem) {
				return new TextureNode(elem, tree);
			}
		});
		rsc.cameras.addListener(new CSNodeListener<Camera>(cameras) {
			@Override
			protected CSNode createNode(Camera elem) {
				return new CameraNode(elem, tree);
			}
		});
		rsc.lights.addListener(new CSNodeListener<Light>(lights) {
			@Override
			protected CSNode createNode(Light elem) {
				return new LightNode(elem, tree);
			}
		});
		rsc.sceneTemplates.addListener(new CSNodeListener<G3DSceneTemplate>(scenes) {
			@Override
			protected CSNode createNode(G3DSceneTemplate elem) {
				return new SceneTemplateNode(elem, tree);
			}
		});
		cs.getOthers().addListener(new CSNodeListener<CMIFFile.OtherFile>(others) {
			@Override
			protected CSNode createNode(CMIFFile.OtherFile elem) {
				return new OtherFileNode(elem, tree);
			}
		});
		rsc.skeletalAnimations.addListener(new CSNodeListener<SkeletalAnimation>(animationsS) {
			@Override
			protected CSNode createNode(SkeletalAnimation elem) {
				return new AnimationNode(rsc.skeletalAnimations, elem, tree);
			}
		});
		rsc.materialAnimations.addListener(new CSNodeListener<MaterialAnimation>(animationsM) {
			@Override
			protected CSNode createNode(MaterialAnimation elem) {
				return new AnimationNode(rsc.materialAnimations, elem, tree);
			}
		});
		rsc.visibilityAnimations.addListener(new CSNodeListener<VisibilityAnimation>(animationsV) {
			@Override
			protected CSNode createNode(VisibilityAnimation elem) {
				return new AnimationNode(rsc.visibilityAnimations, elem, tree);
			}
		});
		rsc.cameraAnimations.addListener(new CSNodeListener<CameraAnimation>(animationsC) {
			@Override
			protected CSNode createNode(CameraAnimation elem) {
				return new AnimationNode(rsc.cameraAnimations, elem, tree);
			}
		});
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return "Creative Studio";
	}

	@Override
	public NamedResource getContent() {
		return null;
	}

	@Override
	public ListenableList getParentList() {
		return new ListenableList();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.OTHER;
	}

	@Override
	public void setContent(NamedResource cnt) {

	}

}
