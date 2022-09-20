package ctrmap.renderer.scenegraph;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.base.ViewportInfo;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraAnimationController;
import ctrmap.renderer.scene.animation.material.MatAnimController;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.KinematicsController;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalController;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityController;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.math.MathEx;
import xstandard.math.MatrixUtil;
import xstandard.math.vec.Matrix4;
import xstandard.util.ArraysEx;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class G3DResourceInstance {

	public G3DResourceInstance parent;
	public String parentNodeName;
	protected G3DResInstanceList children;

	public ParentMode parentMode = ParentMode.ALL;

	public G3DResource resource = new G3DResource();
	public MetaData metaData = new MetaData();

	private List<SceneAnimationCallback> sceneAnimationListeners = new ArrayList<>();
	private List<ResourceChangeListener> resourceChangeListeners = new ArrayList<>();

	public ListenableList<AbstractAnimationController> resourceAnimControllers = new ListenableList<>();
	public ListenableList<KinematicsController> resourceKinematicsControllers = new ListenableList<>();
	public ListenableList<Camera> cameraInstances = new ListenableList<>();
	public ListenableList<Light> lights = new ListenableList<>();

	public Vec3f p = new Vec3f();
	public Vec3f r = new Vec3f();
	public Vec3f s = new Vec3f(1, 1, 1);

	public boolean persistent = false;

	public boolean visible = true;

	public boolean enableAnimations = true;
	public boolean enableAnimationCallbacks = true;

	public G3DResourceInstance() {
		children = new G3DResInstanceList(this);
	}

	public ListenableList<G3DResourceInstance> getChildren() {
		return children;
	}

	public ArrayList<G3DResourceInstance> getChildrenSync() {
		return new ArrayList<>(children);
	}

	public void addResourceChangeListener(ResourceChangeListener l) {
		ArraysEx.addIfNotNullOrContains(resourceChangeListeners, l);
	}

	public void addSceneAnimationCallback(SceneAnimationCallback l) {
		if (ArraysEx.addIfNotNullOrContains(sceneAnimationListeners, l)) {
			//System.out.println("Added SceneAnimationCallback " + l);
		}
	}

	public void removeSceneAnimationCallback(SceneAnimationCallback l) {
		sceneAnimationListeners.remove(l);
	}
	
	public void setChildren(G3DResInstanceList l) {
		children = l;
		if (children != null) {
			children.setParent(this);
		}
	}

	public int getChildrenCount() {
		int c = children.size();
		for (G3DResourceInstance ch : getChildren()) {
			c += ch.getChildrenCount();
		}
		return c;
	}

	public Vec3f getPosition() {
		return p;
	}

	public Vec3f getRotation() {
		return r;
	}

	public Vec3f getScale() {
		return s;
	}

	public Vec3f calcMinVector() {
		Vec3f min = new Vec3f(Float.MAX_VALUE);

		if (resource != null && !resource.models.isEmpty()) {
			min.set(resource.minVector);
		}

		for (G3DResourceInstance child : getChildren()) {
			Vec3f childMin = child.calcMinVector();
			if (childMin.x != Float.MAX_VALUE) {
				childMin.add(child.getPosition());
				min.min(childMin);
			}
		}

		return min;
	}

	public Vec3f calcMaxVector() {
		Vec3f max = new Vec3f(-Float.MAX_VALUE);

		if (resource != null && !resource.models.isEmpty()) {
			max.set(resource.maxVector);
		}

		for (G3DResourceInstance child : getChildren()) {
			Vec3f childMax = child.calcMaxVector();
			if (childMax.x != -Float.MAX_VALUE) {
				childMax.add(child.getPosition());
				max.max(childMax);
			}
		}

		return max;
	}

	public void instantiateCamera(Camera cam) {
		ArraysEx.addIfNotNullOrContains(cameraInstances, cam);
	}

	public void instantiateLight(Light light) {
		ArraysEx.addIfNotNullOrContains(lights, light);
	}

	public void instantiateLights(Collection<? extends Light> lights) {
		ArraysEx.addAllIfNotNullOrContains(this.lights, lights);
	}

	public void deinstantiateLight(Light light) {
		lights.remove(light);
	}

	public void deinstantiateCamera(Camera cam) {
		cameraInstances.remove(cam);
	}

	public void deleteAllCamInstances() {
		cameraInstances.clear();
	}

	public void deleteAllLightInstances() {
		lights.clear();
	}

	private static final float DEGREES_TO_RADIANS_F = 0.017453292519943295f;

	private static float toRadians(float deg) {
		return deg * DEGREES_TO_RADIANS_F;
	}

	public Matrix4 getModelMatrix() {
		Matrix4 mtx = new Matrix4();
		Vec3f pos = getPosition();
		Vec3f rot = getRotation();
		Vec3f sca = getScale();
		mtx.translate(pos.x, pos.y, pos.z);
		mtx.rotateZYX(toRadians(rot.z), toRadians(rot.y), toRadians(rot.x));
		mtx.scale(sca.x, sca.y, sca.z);
		return mtx;
	}

	public Matrix4 getCameraMatrix(boolean useCache) {
		Matrix4 mtx = new Matrix4();
		for (Camera cam : cameraInstances) {
			mtx.mul(cam.getTransformMatrix(useCache));
		}
		return mtx;
	}

	public Matrix4 getViewMatrix(boolean useCache) {
		return getCameraMatrix(useCache).invert();
	}

	public Matrix4 getTransformMatrix() {
		Matrix4 mtx = getViewMatrix(false);
		mtx.mul(getModelMatrix());
		return mtx;
	}

	public Matrix4 getRotationMatrix() {
		Vec3f rotVec = getRotation().clone().mul(MathEx.DEGREES_TO_RADIANS);
		return Matrix4.createRotation(rotVec);
	}

	public Matrix4 getAbsoluteModelViewMatrix() {
		Matrix4 mtx = new Matrix4();
		if (parent != null) {
			Matrix4 pm = parent.getAbsoluteModelViewMatrix();
			mtx.mul(pm);

			if (parentNodeName != null) {
				for (Model mdl : parent.resource.models) {
					Joint jnt = mdl.skeleton.getJoint(parentNodeName);
					mtx.mul(mdl.skeleton.getAbsoluteJointBindPoseMatrix(jnt));
					break;
				}
			}

			switch (parentMode) {
				case ALL:
					break;
				case TRANSLATION: {
					Matrix4 initMtx = new Matrix4();
					if (parent.parent != null) {
						initMtx = parent.parent.getAbsoluteModelViewMatrix();
					}
					initMtx.translate(parent.getPosition());
					mtx = initMtx;
				}
				break;
				case TRANSLATION_AND_ROTATION: {
					Matrix4 initMtx = new Matrix4();
					if (parent.parent != null) {
						initMtx = parent.parent.getAbsoluteModelViewMatrix();
					}
					initMtx.translate(parent.getPosition());
					initMtx.rotateZYXDeg(r.z, r.y, r.x);
					mtx = initMtx;
				}
				break;
				case SKIP:
					if (parent.parent != null) {
						mtx = parent.parent.getAbsoluteModelViewMatrix();
					} else {
						mtx = new Matrix4();
					}
					break;
			}
		}
		mtx.mul(getTransformMatrix());
		return mtx;
	}

	public Matrix4 getAbsoluteModelMatrix() {
		Matrix4 mtx = new Matrix4();
		if (parent != null) {
			Matrix4 pm = parent.getAbsoluteModelMatrix();
			mtx.mul(pm);
			switch (parentMode) {
				case ALL:
					break;
				case TRANSLATION: {
					Matrix4 initMtx = new Matrix4();
					if (parent.parent != null) {
						initMtx = parent.parent.getAbsoluteModelMatrix();
					}
					initMtx.translate(parent.getPosition());
					mtx = initMtx;
					break;
				}
				case TRANSLATION_AND_ROTATION: {
					Matrix4 initMtx = new Matrix4();
					if (parent.parent != null) {
						initMtx = parent.parent.getAbsoluteModelMatrix();
					}
					initMtx.translate(parent.getPosition());
					initMtx.rotateZYXDeg(r.z, r.y, r.x);
					mtx = initMtx;
					break;
				}
				case SKIP:
					if (parent.parent != null) {
						mtx = parent.parent.getAbsoluteModelMatrix();
					} else {
						mtx = new Matrix4();
					}
					break;
			}
		}
		mtx.mul(getModelMatrix());
		return mtx;
	}

	public boolean hasProjectionMatrix() {
		return !cameraInstances.isEmpty();
	}

	public Matrix4 getAbsoluteProjectionMatrix(ViewportInfo vi) {
		for (Camera cam : cameraInstances) {
			//There can only be one applied at a time
			return cam.getProjectionMatrix(vi);
		}
		if (parent != null) {
			return parent.getAbsoluteProjectionMatrix(vi);
		}
		return new Matrix4();
	}

	public int getTotalVertexCountVBO() {
		int vcount = 0;
		for (Model mdl : resource.models) {
			vcount += mdl.getTotalVertexCountVBO();
		}
		for (G3DResourceInstance ch : getChildren()) {
			vcount += ch.getTotalVertexCountVBO();
		}
		return vcount;
	}

	//Factors in indices
	public int getTotalVertexCount() {
		int vcount = 0;
		if (resource != null) {
			for (Model mdl : resource.models) {
				vcount += mdl.getTotalVertexCount();
			}
		}
		for (G3DResourceInstance ch : getChildren()) {
			vcount += ch.getTotalVertexCount();
		}
		return vcount;
	}

	public boolean isInstanceOf(G3DResource res) {
		return resource == res;
	}

	public void resetVertexData(Object rendererIdent) {
		if (resource != null) {
			for (Model model : resource.models) {
				//model.setBindPose();
			}
		}
		for (G3DResourceInstance i : getChildren()) {
			i.resetVertexData(rendererIdent);
		}
	}

	public void notifyBeginDraw(RenderSettings settings) {
		for (AbstractAnimationController ctrl : resourceAnimControllers) {
			ctrl.register(settings);
		}
		for (G3DResourceInstance i : getChildrenSync()) {
			i.notifyBeginDraw(settings);
		}
	}

	public void requestAllTextureReupload() {
		if (resource != null) {
			for (Texture t : resource.textures) {
				t.requestReupload();
			}
		}
		for (G3DResourceInstance ch : getChildrenSync()) {
			ch.requestAllTextureReupload();
		}
	}

	public void unmergeResource(G3DResource toDestroy) {
		if (toDestroy != null) {
			if (resource != null) {
				resource.unmerge(toDestroy);
			}
			for (AbstractAnimation a : toDestroy.getAnimations()) {
				removeAnimation(a);
			}
		}
	}

	public void setPersistent(boolean state) {
		persistent = state;
	}

	public void setVisible(boolean state) {
		visible = state;
	}

	public void setEnableAnimationCallbacks(boolean state) {
		enableAnimationCallbacks = state;
	}

	public void setEnableAnimations(boolean state) {
		enableAnimations = state;
	}

	public Vec3f getPositionWorld() {
		return getAbsoluteModelMatrix().getTranslation();
	}

	public Vec3f getPositionAbsolute() {
		return getAbsoluteModelViewMatrix().getTranslation();
	}

	public void setResource(G3DResource res) {
		if (resource != res) {
			G3DResource oldRes = resource;
			resource = res;

			for (AbstractAnimationController ctrl : resourceAnimControllers) {
				if (ctrl instanceof SkeletalController) {
					if (!res.models.isEmpty()) {
						((SkeletalController) ctrl).setSkeleton(res.models.get(0).skeleton);
					}
				}
			}

			for (ResourceChangeListener l : resourceChangeListeners) {
				l.onResourceChanged(this, oldRes, res);
			}
		}
	}

	public final void addChild(G3DResourceInstance ch) {
		if (ch != null) {
			if (ch.parent != this) {
				if (ch.parent != null) {
					ch.parent.children.remove(ch);
				}
				if (ArraysEx.addIfNotNullOrContains(children, ch)) {
					ch.parent = this;
				}
			}
		}
	}

	public void removeChild(int idx) {
		if (idx >= 0 && idx < children.size()) {
			removeChild(getChildren().get(idx));
		}
	}

	public void removeChild(G3DResourceInstance ch) {
		if (children.remove(ch)) {
			ch.parent = null;
		}
	}

	public Texture getResTexture(String name) {
		return (Texture) getNamedResource(name, G3DResourceType.TEXTURE);
	}

	public void clear() {
		clear(false);
	}

	public void merge(G3DResourceInstance source) {
		for (G3DResourceInstance ch : source.getChildren()) {
			addChild(ch);
		}
	}

	public void merge(G3DResource res) {
		resource.merge(res);
	}

	public void clear(boolean force) {
		if (!persistent || force) {
			resource.clear();
		}
		clearChildren(force);
	}

	public void clearChildren(boolean force) {
		List<G3DResourceInstance> chToRemove = new ArrayList<>();
		for (G3DResourceInstance ch : getChildren()) {
			if (!ch.persistent || force) {
				ch.parent = null;
				chToRemove.add(ch);
			}
		}
		children.removeAll(chToRemove);
	}

	public void reapTexturesFromChildren() {
		if (resource != null) {
			for (G3DResourceInstance ch : getChildren()) {
				ch.reapTexturesFromChildren();
				if (ch.resource != null) {
					resource.textures.addAll(ch.resource.textures);
					ch.resource.textures.clear();
				}
			}
		}
	}

	public List<CameraAnimationController> getLocalResCamAnimControllers() {
		List<CameraAnimationController> l = new ArrayList<>();
		if (resource != null) {
			for (AbstractAnimationController ctrl : resourceAnimControllers) {
				if (ctrl instanceof CameraAnimationController) {
					l.add((CameraAnimationController) ctrl);
				}
			}
		}
		return l;
	}

	public List<MatAnimController> getLocalResMatAnimControllers() {
		List<MatAnimController> l = new ArrayList<>();
		if (resource != null) {
			for (AbstractAnimationController ctrl : resourceAnimControllers) {
				if (ctrl instanceof MatAnimController) {
					l.add((MatAnimController) ctrl);
				}
			}
		}
		return l;
	}

	public List<VisibilityController> getLocalResVisAnimControllers() {
		List<VisibilityController> l = new ArrayList<>();
		if (resource != null) {
			for (AbstractAnimationController ctrl : resourceAnimControllers) {
				if (ctrl instanceof VisibilityController) {
					l.add((VisibilityController) ctrl);
				}
			}
		}
		return l;
	}

	public List<SkeletalController> getLocalResSklAnimControllers() {
		List<SkeletalController> l = new ArrayList<>();
		if (resource != null) {
			for (AbstractAnimationController ctrl : resourceAnimControllers) {
				if (ctrl instanceof SkeletalController) {
					l.add((SkeletalController) ctrl);
				}
			}
		}
		return l;
	}

	public List<MatAnimController> getResMatAnimControllers() {
		List<MatAnimController> l = getLocalResMatAnimControllers();
		if (parent != null) {
			l.addAll(parent.getResMatAnimControllers());
		}
		return l;
	}

	public List<VisibilityController> getResVisAnimControllers() {
		List<VisibilityController> l = getLocalResVisAnimControllers();
		if (parent != null) {
			l.addAll(parent.getResVisAnimControllers());
		}
		return l;
	}

	public List<SkeletalController> getResSklAnimControllers() {
		List<SkeletalController> l = getLocalResSklAnimControllers();
		if (parent != null) {
			l.addAll(parent.getResSklAnimControllers());
		}
		return l;
	}

	public List<AbstractAnimationController> getThisAndChildrenAnimationControllers() {
		List<AbstractAnimationController> l = new ArrayList<>();
		l.addAll(resourceAnimControllers);
		for (G3DResourceInstance ch : getChildren()) {
			l.addAll(ch.getThisAndChildrenAnimationControllers());
		}
		return l;
	}

	private NamedResource getNamedResource(String name, G3DResourceType t) {
		NamedResource res = null;
		if (resource != null) {
			res = resource.getNamedResource(name, t);
		}

		if (res == null && parent != null) {
			res = parent.getNamedResource(name, t);
		}
		return res;
	}

	public void advanceAllAnimationCallbacks(float step) {
		if (!enableAnimationCallbacks) {
			step = 0f;
		}

		for (SceneAnimationCallback l : new ArrayList<>(sceneAnimationListeners)) {
			if (l == null) {
				throw new RuntimeException("How the fuck can this element be null.");
			}
			l.run(step);
		}
		for (G3DResourceInstance ch : getChildrenSync()) {
			ch.advanceAllAnimationCallbacks(step);
		}
	}

	public void advanceAllAnimations(float step, RenderSettings settings) {
		if (!enableAnimations) {
			step = 0f;
		}

		for (AbstractAnimationController c : resourceAnimControllers) {
			c.advanceFrame(step, settings);
		}
		for (G3DResourceInstance ch : getChildrenSync()) {
			ch.advanceAllAnimations(step, settings);
		}
	}

	public void runAllCallbacks() {
		List<AbstractAnimationController> callbackBuffer = new ArrayList<>(resourceAnimControllers);
		for (AbstractAnimationController c : callbackBuffer) { //callbacks might remove animations controllers which would result in ConcurrentModificationException
			c.callback();
		}
		for (G3DResourceInstance ch : getChildrenSync()) {
			ch.runAllCallbacks();
		}
	}

	public boolean hasAnimation(AbstractAnimation a) {
		for (AbstractAnimationController c : resourceAnimControllers) {
			if (c.animeList.contains(a) || c.anim == a) {
				return true;
			}
		}
		return false;
	}

	public void stopCameraAnimations() {
		for (int i = 0; i < resourceAnimControllers.size(); i++) {
			if (resourceAnimControllers.get(i) instanceof CameraAnimationController) {
				resourceAnimControllers.get(i).stopAnimation();
				resourceAnimControllers.remove(i);
			}
		}
	}

	public void stopAllAnimations() {
		stopAllAnimations(true);
	}

	public void stopAllAnimations(boolean children) {
		for (AbstractAnimationController c : resourceAnimControllers) {
			c.pauseAnimation();
		}
		if (children) {
			for (G3DResourceInstance ch : getChildren()) {
				ch.stopAllAnimations();
			}
		}
		resourceAnimControllers.clear();
	}

	public void bindAnimController(AbstractAnimationController ctrl) {
		resourceAnimControllers.add(ctrl);
		ctrl.resumeAnimation();
	}

	public void bindSkeletalAnimation(SkeletalAnimation anm, Runnable callback) {
		if (callback != null) {
			for (Model mdl : resource.models) {
				if (anm.hasSkeleton()) {
					resourceAnimControllers.add(new SkeletalController(anm, callback));
				} else {
					if (mdl.skeleton != null && !mdl.skeleton.getJoints().isEmpty()) {
						resourceAnimControllers.add(new SkeletalController(anm, mdl.skeleton, callback));
					}
				}
			}
		} else {
			bindSkeletalAnimation(anm);
		}
	}

	public void bindSkeletalAnimation(SkeletalAnimation anm) {
		bindAnimationAutodetect(anm);
	}

	public void bindSkeletalAnimation(String name) {
		bindSkeletalAnimation(name, null);
	}

	public void bindSkeletalAnimation(String name, Runnable callback) {
		if (resource != null) {
			SkeletalAnimation anime = (SkeletalAnimation) Scene.getNamedObject(name, resource.skeletalAnimations);
			if (anime != null) {
				bindSkeletalAnimation(anime, callback);
			}
		}
	}

	public void bindSkeletalAnimationQueue(String... names) {
		bindSkeletalAnimationQueue(null, names);
	}

	public void bindSkeletalAnimationQueue(Runnable callback, String... names) {
		for (Model model : resource.models) {
			Queue<AbstractAnimation> anmQueue = new LinkedList<>();
			for (String name : names) {
				SkeletalAnimation anime = (SkeletalAnimation) Scene.getNamedObject(name, resource.skeletalAnimations);
				if (anime != null) {
					anmQueue.add(anime);
				}
			}
			resourceAnimControllers.add(new SkeletalController(anmQueue, model.skeleton, callback));
		}
	}

	public void bindSkeletalAnimationQueue(Runnable callback, Collection<SkeletalAnimation> queue) {
		for (Model model : resource.models) {
			Queue<AbstractAnimation> anmQueue = new LinkedList<>(queue);
			resourceAnimControllers.add(new SkeletalController(anmQueue, model.skeleton, callback));
		}
	}

	public AbstractAnimationController getCtrlByAnime(AbstractAnimation anm) {
		if (anm == null) {
			return null;
		}
		for (AbstractAnimationController ctrl : resourceAnimControllers) {
			if (ctrl.anim == anm || ctrl.animeList.contains(anm)) {
				return ctrl;
			}
		}
		return null;
	}

	public void playAnimations(List<? extends AbstractAnimation> l) {
		for (AbstractAnimation a : l) {
			playAnimation(a);
		}
	}

	public AbstractAnimationController playAnimation(AbstractAnimation anm) {
		if (anm != null) {
			for (AbstractAnimationController c : resourceAnimControllers) {
				if (c.anim == anm || c.animeList.contains(anm)) {
					return c;
				}
			}
			return bindAnimationAutodetect(anm);
		}
		return null;
	}

	public AbstractAnimationController bindAnimationAutodetect(String name) {
		AbstractAnimation anime = (AbstractAnimation) Scene.getNamedObject(name, resource.skeletalAnimations);
		if (anime == null) {
			anime = (AbstractAnimation) Scene.getNamedObject(name, resource.materialAnimations);
		}
		if (anime != null) {
			return bindAnimationAutodetect(anime);
		}
		return null;
	}

	public AbstractAnimationController bindAnimationAutodetect(AbstractAnimation anm) {
		if (anm == null) {
			return null;
		}

		AbstractAnimationController ctrl = null;

		if (anm instanceof SkeletalAnimation) {
			ctrl = new SkeletalController((SkeletalAnimation) anm);
		} else if (anm instanceof MaterialAnimation) {
			ctrl = new MatAnimController((MaterialAnimation) anm);
		} else if (anm instanceof CameraAnimation) {
			ctrl = new CameraAnimationController((CameraAnimation) anm);
		} else if (anm instanceof VisibilityAnimation) {
			ctrl = new VisibilityController((VisibilityAnimation) anm);
		}

		ArraysEx.addIfNotNullOrContains(resourceAnimControllers, ctrl);

		return ctrl;
	}

	public List<AbstractAnimationController> getThisAndParentAnimeControllers() {
		List<AbstractAnimationController> l = new ArrayList<>(resourceAnimControllers);
		if (parent != null) {
			l.addAll(parent.getThisAndParentAnimeControllers());
		}
		return l;
	}

	public void bindAnimations(AbstractAnimation... anm) {
		for (AbstractAnimation a : anm) {
			bindAnimationAutodetect(a);
		}
	}

	public void playResourceAnimations() {
		if (resource != null) {
			playAnimations(resource.materialAnimations);
		}
	}

	public G3DResource getAllResources() {
		G3DResource res = new G3DResource();
		res.addModels(getAllModels());
		res.addTextures(getAllTextures());
		res.addCameras(getAllCameras());
		res.addLights(getAllLights());
		res.addAnimes(getAllAnimations());
		res.addSceneTemplates(getAllSceneTemplates());
		return res;
	}

	public List<Model> getAllModels() {
		List<Model> l = new ArrayList<>();

		if (resource != null) {
			l.addAll(resource.models);
		}
		for (G3DResourceInstance ch : getChildren()) {
			ArraysEx.addAllIfNotNullOrContains(l, ch.getAllModels());
		}

		return l;
	}

	public List<Camera> getAllCameras() {
		List<Camera> l = new ArrayList<>();

		if (resource != null) {
			l.addAll(resource.cameras);
		}
		for (Camera actcam : cameraInstances) {
			ArraysEx.addIfNotNullOrContains(l, actcam);
		}

		for (G3DResourceInstance ch : getChildren()) {
			ArraysEx.addAllIfNotNullOrContains(l, ch.getAllCameras());
		}

		return l;
	}

	public List<Light> getAllLights() {
		List<Light> l = new ArrayList<>();

		if (resource != null) {
			l.addAll(resource.lights);
		}
		for (Light actLight : lights) {
			ArraysEx.addIfNotNullOrContains(l, actLight);
		}

		for (G3DResourceInstance ch : getChildren()) {
			ArraysEx.addAllIfNotNullOrContains(l, ch.getAllLights());
		}

		return l;
	}

	public List<G3DSceneTemplate> getAllSceneTemplates() {
		List<G3DSceneTemplate> l = new ArrayList<>();

		if (resource != null) {
			l.addAll(resource.sceneTemplates);
		}

		for (G3DResourceInstance ch : getChildren()) {
			ArraysEx.addAllIfNotNullOrContains(l, ch.getAllSceneTemplates());
		}

		return l;
	}

	public List<Texture> getAllTextures() {
		List<Texture> l = new ArrayList<>();
		if (resource != null) {
			for (Texture t : resource.textures) {
				if (!l.contains(t)) {
					l.add(t);
				}
			}
		}
		for (G3DResourceInstance ch : getChildren()) {
			ArraysEx.addAllIfNotNullOrContains(l, ch.getAllTextures());
		}
		return l;
	}

	public List<AbstractAnimation> getAllAnimations() {
		List<AbstractAnimation> l = new ArrayList<>(getAllSkeletalAnimations());
		l.addAll(getAllMaterialAnimations());
		l.addAll(getAllVisibilityAnimations());
		l.addAll(getAllCameraAnimations());
		return l;
	}

	public List<MaterialAnimation> getAllMaterialAnimations() {
		return getAnimationsImpl(MaterialAnimation.class);
	}

	public List<VisibilityAnimation> getAllVisibilityAnimations() {
		return getAnimationsImpl(VisibilityAnimation.class);
	}

	public List<SkeletalAnimation> getAllSkeletalAnimations() {
		return getAnimationsImpl(SkeletalAnimation.class);
	}

	public List<CameraAnimation> getAllCameraAnimations() {
		return getAnimationsImpl(CameraAnimation.class);
	}

	private List getAnimationsImpl(Class cls) {
		List l = new ArrayList();
		if (resource != null) {
			if (cls == CameraAnimation.class) {
				ArraysEx.addAllIfNotNullOrContains(l, resource.cameraAnimations);
			} else if (cls == SkeletalAnimation.class) {
				ArraysEx.addAllIfNotNullOrContains(l, resource.skeletalAnimations);
			} else if (cls == MaterialAnimation.class) {
				ArraysEx.addAllIfNotNullOrContains(l, resource.materialAnimations);
			} else if (cls == VisibilityAnimation.class) {
				ArraysEx.addAllIfNotNullOrContains(l, resource.visibilityAnimations);
			}
		}
		for (AbstractAnimationController aac : resourceAnimControllers) {
			if (aac.anim != null && aac.anim.getClass().isAssignableFrom(cls)) {
				if (!l.contains(aac.anim)) {
					l.add(aac.anim);
				}
			}
		}
		for (G3DResourceInstance ch : getChildren()) {
			ArraysEx.addAllIfNotNullOrContains(l, ch.getAnimationsImpl(cls));
		}
		return l;
	}

	public boolean removeAnimation(AbstractAnimation anm) {
		for (int i = 0; i < resourceAnimControllers.size(); i++) {
			if (resourceAnimControllers.get(i).anim == anm) {
				resourceAnimControllers.remove(i);
				i--;
				return true; //we can break since playAnimations automatically prohibits duplicate animation controllers
			}
		}
		return false;
	}

	public void setParentMode(ParentMode mode) {
		parentMode = mode;

	}

	public enum ParentMode {
		ALL,
		TRANSLATION,
		TRANSLATION_AND_ROTATION,
		SKIP;

		public static final ParentMode[] VALUES = values();
	}

	public static interface ResourceChangeListener {

		public void onResourceChanged(G3DResourceInstance instance, G3DResource oldRes, G3DResource newRes);
	}
}
