
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
import xstandard.util.ListenableList;

public class ResourceNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420002;
	
	private G3DResource rsc;
	
	private ListenableList.ElementChangeListener mdlListener;
	private ListenableList.ElementChangeListener texListener;
	private ListenableList.ElementChangeListener anmSklListener;
	private ListenableList.ElementChangeListener anmMatListener;
	private ListenableList.ElementChangeListener anmVisListener;
	private ListenableList.ElementChangeListener anmCamListener;
	private ListenableList.ElementChangeListener camListener;
	private ListenableList.ElementChangeListener lightListener;

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
		
		rsc.models.addListener(mdlListener = new ScenegraphListener<Model>(models) {
			@Override
			protected ScenegraphExplorerNode createNode(Model elem) {
				return new ModelNode(elem, tree);
			}
		});
		rsc.textures.addListener(texListener = new ScenegraphListener<Texture>(textures) {
			@Override
			protected ScenegraphExplorerNode createNode(Texture elem) {
				return new TextureNode(elem, tree);
			}
		});
		rsc.cameras.addListener(camListener = new ScenegraphListener<Camera>(cameras) {
			@Override
			protected ScenegraphExplorerNode createNode(Camera elem) {
				return new CameraNode(elem, tree);
			}
		});
		rsc.lights.addListener(lightListener = new ScenegraphListener<Light>(lights) {
			@Override
			protected ScenegraphExplorerNode createNode(Light elem) {
				return new LightNode(elem, tree);
			}
		});
		rsc.skeletalAnimations.addListener(anmSklListener = new ScenegraphListener<SkeletalAnimation>(animationsS) {
			@Override
			protected ScenegraphExplorerNode createNode(SkeletalAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
		rsc.materialAnimations.addListener(anmMatListener = new ScenegraphListener<MaterialAnimation>(animationsM) {
			@Override
			protected ScenegraphExplorerNode createNode(MaterialAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
		rsc.visibilityAnimations.addListener(anmVisListener = new ScenegraphListener<VisibilityAnimation>(animationsV) {
			@Override
			protected ScenegraphExplorerNode createNode(VisibilityAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
		rsc.cameraAnimations.addListener(anmCamListener = new ScenegraphListener<CameraAnimation>(animationsC) {
			@Override
			protected ScenegraphExplorerNode createNode(CameraAnimation elem) {
				return new AnimationNode(elem, tree);
			}
		});
	}
	
	@Override
	protected void freeListeners() {
		rsc.models.removeListener(mdlListener);
		rsc.textures.removeListener(texListener);
		rsc.cameras.removeListener(camListener);
		rsc.lights.removeListener(lightListener);
		rsc.skeletalAnimations.removeListener(anmSklListener);
		rsc.materialAnimations.removeListener(anmMatListener);
		rsc.visibilityAnimations.removeListener(anmVisListener);
		rsc.cameraAnimations.removeListener(anmCamListener);
		mdlListener = null;
		texListener = null;
		camListener = null;
		lightListener = null;
		anmCamListener = null;
		anmMatListener = null;
		anmSklListener = null;
		anmVisListener = null;
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
