
package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
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

public class ResourceNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420002;
	
	private G3DResource rsc;

	public ResourceNode(G3DResource rsc, ScenegraphJTree tree) {
		super(tree);
		this.rsc = rsc;
		
		ContainerNode models = new ContainerNode("Models", -1, tree);
		addChild(models);
		
		ContainerNode textures = new ContainerNode("Textures", -1, tree);
		addChild(textures);
		
		ContainerNode animationsS = new ContainerNode("Animations (Skeletal)", -1, tree);
		addChild(animationsS);
		
		ContainerNode animationsM = new ContainerNode("Animations (Material)", -1, tree);
		addChild(animationsM);
		
		ContainerNode animationsV = new ContainerNode("Animations (Visibility)", -1, tree);
		addChild(animationsV);
		
		ContainerNode animationsC = new ContainerNode("Animations (Camera)", -1, tree);
		addChild(animationsC);
		
		ContainerNode cameras = new ContainerNode("Cameras", -1, tree);
		addChild(cameras);
		
		ContainerNode lights = new ContainerNode("Lights", -1, tree);
		addChild(lights);
		
		for (Model mdl : rsc.models){
			models.addChild(new ModelNode(mdl, tree));
		}
		
		for (Texture tex : rsc.textures){
			textures.addChild(new TextureNode(tex, tree));
		}
		
		for (AbstractAnimation a : rsc.skeletalAnimations){
			animationsS.addChild(new AnimationNode(a, tree));
		}
		
		for (AbstractAnimation a : rsc.materialAnimations){
			animationsM.addChild(new AnimationNode(a, tree));
		}
		
		for (AbstractAnimation a : rsc.visibilityAnimations){
			animationsV.addChild(new AnimationNode(a, tree));
		}
		
		for (AbstractAnimation a : rsc.cameraAnimations){
			animationsC.addChild(new AnimationNode(a, tree));
		}
		
		for (Camera cam : rsc.cameras){
			cameras.addChild(new CameraNode(cam, tree));
		}
		
		for (Light l : rsc.lights){
			lights.addChild(new LightNode(l, tree));
		}
		
		rsc.models.addListener(new ScenegraphListener<Model>(models) {
			@Override
			protected ScenegraphExplorerNode createNode(Model elem) {
				return new ModelNode(elem, tree);
			}
		});
		rsc.textures.addListener(new ScenegraphListener<Texture>(textures) {
			@Override
			protected ScenegraphExplorerNode createNode(Texture elem) {
				return new TextureNode(elem, tree);
			}
		});
		rsc.cameras.addListener(new ScenegraphListener<Camera>(cameras) {
			@Override
			protected ScenegraphExplorerNode createNode(Camera elem) {
				return new CameraNode(elem, tree);
			}
		});
		rsc.lights.addListener(new ScenegraphListener<Light>(lights) {
			@Override
			protected ScenegraphExplorerNode createNode(Light elem) {
				return new LightNode(elem, tree);
			}
		});
		rsc.skeletalAnimations.addListener(new ScenegraphListener<SkeletalAnimation>(animationsS) {
			@Override
			protected ScenegraphExplorerNode createNode(SkeletalAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
		rsc.materialAnimations.addListener(new ScenegraphListener<MaterialAnimation>(animationsM) {
			@Override
			protected ScenegraphExplorerNode createNode(MaterialAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
		rsc.visibilityAnimations.addListener(new ScenegraphListener<VisibilityAnimation>(animationsV) {
			@Override
			protected ScenegraphExplorerNode createNode(VisibilityAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
		rsc.cameraAnimations.addListener(new ScenegraphListener<CameraAnimation>(animationsC) {
			@Override
			protected ScenegraphExplorerNode createNode(CameraAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.resViewer, rsc);
	}
	
	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		if (rsc.models.size() == 1){
			return rsc.models.get(0).name;
		}
		return "Resource";
	}

	@Override
	public Object getContent() {
		return rsc;
	}

}
