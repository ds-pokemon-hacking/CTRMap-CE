package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.animation.skeletal.KinematicsController;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import xstandard.util.ListenableList;

public class ResourceInstanceNode extends ScenegraphExplorerNode {

	public static final int RESID = 0x420001;

	private G3DResourceInstance instance;
	
	private G3DResourceInstance.ResourceChangeListener resChgListener;
	private ListenableList.ElementChangeListener childrenListener;
	private ListenableList.ElementChangeListener animListener;
	private ListenableList.ElementChangeListener lightListener;
	private ListenableList.ElementChangeListener camListener;
	private ListenableList.ElementChangeListener kinematicsListener;

	public ResourceInstanceNode(G3DResourceInstance instance, ScenegraphJTree tree) {
		super(tree);
		this.instance = instance;

		if (instance.resource != null) {
			addChild(new ResourceNode(instance.resource, tree));
		}
		instance.addResourceChangeListener(resChgListener = new G3DResourceInstance.ResourceChangeListener() {
			@Override
			public void onResourceChanged(G3DResourceInstance instance, G3DResource oldRes, G3DResource newRes) {
				removeChild(getChildByContent(oldRes));
				if (newRes != null) {
					addChild(new ResourceNode(newRes, tree));
				}
			}
		});
		
		ContainerNode animeControllers = new ContainerNode("Animation controllers", -1, tree);
		addChild(animeControllers);
		ContainerNode ikControllers = new ContainerNode("IK controllers", -1, tree);
		addChild(ikControllers);
		ContainerNode lights = new ContainerNode("Lights", -1, tree);
		addChild(lights);

		for (Camera cam : instance.cameraInstances) {
			addChild(new CameraNode(cam, tree));
		}

		for (AbstractAnimationController anm : instance.resourceAnimControllers) {
			animeControllers.addChild(new AnimationControllerNode(anm, tree));
		}

		for (KinematicsController ik : instance.resourceKinematicsControllers) {
			ikControllers.addChild(new KinematicsControllerNode(ik, tree));
		}
		
		for (Light lt : instance.lights) {
			lights.addChild(new LightNode(lt, tree));
		}

		for (G3DResourceInstance child : instance.getChildrenSync()) {
			addChild(new ResourceInstanceNode(child, tree));
		}

		instance.getChildren().addListener(childrenListener = new ScenegraphListener<G3DResourceInstance>(this) {
			@Override
			protected ScenegraphExplorerNode createNode(G3DResourceInstance elem) {
				return new ResourceInstanceNode(elem, tree);
			}
		});
		
		instance.resourceAnimControllers.addListener(animListener = new ScenegraphListener<AbstractAnimationController>(animeControllers) {
			@Override
			protected ScenegraphExplorerNode createNode(AbstractAnimationController elem) {
				return new AnimationControllerNode(elem, tree);
			}
		});
		
		instance.resourceKinematicsControllers.addListener(kinematicsListener = new ScenegraphListener<KinematicsController>(ikControllers) {
			@Override
			protected ScenegraphExplorerNode createNode(KinematicsController elem) {
				return new KinematicsControllerNode(elem, tree);
			}
		});
		
		instance.cameraInstances.addListener(camListener = new ScenegraphListener<Camera>(this) {
			@Override
			protected ScenegraphExplorerNode createNode(Camera elem) {
				return new CameraNode(elem, tree);
			}
		});
		
		instance.lights.addListener(lightListener = new ScenegraphListener<Light>(this) {
			@Override
			protected ScenegraphExplorerNode createNode(Light elem) {
				return new LightNode(elem, tree);
			}
		});
	}
	
	@Override
	protected void freeListeners() {
		instance.getChildren().removeListener(childrenListener);
		instance.lights.removeListener(lightListener);
		instance.cameraInstances.removeListener(camListener);
		instance.resourceKinematicsControllers.removeListener(kinematicsListener);
		instance.resourceAnimControllers.removeListener(animListener);
		instance.removeResourceChangeListener(resChgListener);
		childrenListener = null;
		lightListener = null;
		camListener = null;
		kinematicsListener = null;
		animListener = null;
		resChgListener = null;
	}
	
	@Override
	public void onSelected(ScenegraphExplorer gui){
		gui.loadObjToEditor(gui.instanceEditor, instance);
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		if (instance instanceof Scene) {
			return ((Scene) instance).name;
		}
		if (instance.resource != null) {
			if (instance.resource.models.size() == 1) {
				return instance.resource.models.get(0).name + " (Instance)";
			}
		}
		return "Instance node";
	}

	@Override
	public Object getContent() {
		return instance;
	}

}
