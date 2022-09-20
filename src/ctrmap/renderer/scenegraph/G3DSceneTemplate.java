package ctrmap.renderer.scenegraph;

import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.metadata.MetaData;
import xstandard.math.vec.Vec3f;
import xstandard.util.ArraysEx;
import xstandard.util.ListenableList;

public class G3DSceneTemplate implements NamedResource {

	public String name;

	public G3DSceneTemplateNode root;

	public G3DSceneTemplate() {
		root = new G3DSceneTemplateNode();
	}

	public G3DSceneTemplate(Scene source, G3DResource resStorage) {
		name = source.name;
		root = new G3DSceneTemplateNode(source, resStorage);
	}

	public Scene createScene(G3DResource resource) {
		Scene scene = new Scene(name);

		if (root != null) {
			root.setupG3DResInstance(scene, resource);
		}

		return scene;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
		root.name = name;
	}

	public static class G3DSceneTemplateNode implements NamedResource {

		public G3DResourceInstance.ParentMode parentMode = G3DResourceInstance.ParentMode.ALL;
		public String parentAttachmentNode;

		public G3DSceneTemplateNode parent;
		public ListenableList<G3DSceneTemplateNode> children = new ListenableList<>();

		public String name;

		public Vec3f location = new Vec3f();
		public Vec3f rotationDeg = new Vec3f();
		public Vec3f scale = new Vec3f();

		public ListenableList<G3DSceneTemplateResourceLink> resourceLinks = new ListenableList<>();
		public ListenableList<G3DSceneTemplateControllerLink> controllerLinks = new ListenableList<>();

		public MetaData metaData = new MetaData();

		public G3DSceneTemplateNode() {
			children.addListener(new ListenableList.ElementChangeListener() {
				@Override
				public void onEntityChange(ListenableList.ElementChangeEvent evt) {
					G3DSceneTemplateNode n = (G3DSceneTemplateNode) evt.element;
					if (n != null) {
						switch (evt.type) {
							case ADD:
								n.parent = G3DSceneTemplateNode.this;
								break;
							case REMOVE:
								n.parent = null;
								break;
						}
					}
				}
			});
		}

		public G3DSceneTemplateNode(Scene inst, G3DResource outMainRes) {
			this((G3DResourceInstance) inst, outMainRes);
			name = inst.name;
		}

		public G3DSceneTemplateNode(G3DResourceInstance inst, G3DResource outMainRes) {
			this();
			location.set(inst.getPosition());
			rotationDeg.set(inst.getRotation());
			scale.set(inst.getScale());
			parentAttachmentNode = inst.parentNodeName;
			parentMode = inst.parentMode;

			if (inst.resource != null) {
				outMainRes.merge(inst.resource);

				for (NamedResource res : inst.resource.getAllResources()) {
					G3DSceneTemplateResourceLink link = new G3DSceneTemplateResourceLink();
					link.type = G3DResourceType.get(res);
					link.name = res.getName();
					if (link.type != null && link.name != null) {
						resourceLinks.add(link);
					}
				}
			}

			for (AbstractAnimationController ctrl : inst.resourceAnimControllers) {
				if (ctrl.anim != null) {
					G3DSceneTemplateControllerLink ctrlLink = new G3DSceneTemplateControllerLink();
					ctrlLink.frame = ctrl.frame;
					ctrlLink.type = G3DResourceType.get(ctrl.anim);
					if (ctrlLink.type != null) {
						ctrlLink.name = ctrl.anim.name;
						controllerLinks.add(ctrlLink);
					}
					outMainRes.addResource(ctrl.anim);
				}
			}

			for (Camera camInst : inst.cameraInstances) {
				G3DSceneTemplateControllerLink link = new G3DSceneTemplateControllerLink();
				link.type = G3DResourceType.CAMERA;
				link.name = camInst.name;
				controllerLinks.add(link);
				outMainRes.addCamera(camInst);
			}

			for (Light lightInst : inst.lights) {
				G3DSceneTemplateControllerLink link = new G3DSceneTemplateControllerLink();
				link.type = G3DResourceType.LIGHT;
				link.name = lightInst.name;
				controllerLinks.add(link);
				outMainRes.addLight(lightInst);
			}

			for (G3DResourceInstance child : inst.getChildrenSync()) {
				G3DSceneTemplateNode childNode;
				if (child instanceof Scene) {
					childNode = new G3DSceneTemplateNode((Scene) child, outMainRes);
				} else {
					childNode = new G3DSceneTemplateNode(child, outMainRes);
				}
				addChild(childNode);
			}
		}

		public final void addChild(G3DSceneTemplateNode child) {
			ArraysEx.addIfNotNullOrContains(children, child);
		}

		public void setupG3DResInstance(G3DResourceInstance inst, G3DResource mainRes) {
			inst.parentMode = parentMode;
			inst.parentNodeName = parentAttachmentNode;

			inst.p.set(location);
			inst.r.set(rotationDeg);
			inst.s.set(scale);

			inst.metaData.putValues(metaData.getValues());

			for (G3DSceneTemplateResourceLink resLink : resourceLinks) {
				NamedResource res = mainRes.getNamedResource(resLink.name, resLink.type);
				if (res != null) {
					inst.resource.addResource(res);
				}
			}
			for (G3DSceneTemplateControllerLink ctrlLink : controllerLinks) {
				NamedResource res = mainRes.getNamedResource(ctrlLink.name, ctrlLink.type);
				if (res != null) {
					if (res instanceof AbstractAnimation) {
						AbstractAnimationController c = inst.bindAnimationAutodetect((AbstractAnimation) res);
						if (c != null) {
							c.frame = ctrlLink.frame;
						}
					} else if (res instanceof Camera) {
						inst.instantiateCamera((Camera) res);
					} else if (res instanceof Light) {
						inst.instantiateLight((Light) res);
					}
				}
			}

			for (G3DSceneTemplateNode child : children) {
				G3DResourceInstance childInst;
				if (child.name != null) {
					childInst = new Scene(child.name);
				} else {
					childInst = new G3DResourceInstance();
				}
				child.setupG3DResInstance(childInst, mainRes);
				inst.addChild(childInst);
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void setName(String name) {
			this.name = name;
		}
	}

	public static class G3DSceneTemplateResourceLink {

		public G3DResourceType type;
		public String name;
	}

	public static class G3DSceneTemplateControllerLink extends G3DSceneTemplateResourceLink {

		public float frame;
	}
}
