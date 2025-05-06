package ctrmap.renderer.scenegraph;

import ctrmap.renderer.backends.RenderAllocator;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.camera.CameraAnimationController;
import ctrmap.renderer.scene.animation.camera.CameraAnimationFrame;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.animation.material.MatAnimController;
import ctrmap.renderer.scene.animation.skeletal.KinematicsController;
import ctrmap.renderer.scene.animation.skeletal.SkeletalController;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.animation.skeletal.InverseKinematics;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scene.animation.visibility.VisibilityController;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.MeshVisibilityGroup;
import xstandard.math.AABB6f;
import xstandard.math.FAtan;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import java.util.ArrayList;
import java.util.WeakHashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix3f;

public class G3DResourceState {

	public final boolean updateProjMatrix;

	public final Matrix4 localModelMatrix;

	public final Matrix4 cameraMatrix;
	public final Matrix4 modelMatrix;
	public final Matrix4 viewMatrix;

	public final Matrix3f normalMatrix;

	private Matrix4 projectionMatrix = null;

	private Matrix4 modelViewMatrix;

	public final List<Camera> cameras = new ArrayList<>();
	private final List<Matrix4> camMatrices = new ArrayList<>();

	public List<Light> lights = new ArrayList<>();
	public Map<Light, Matrix4> lightMatrices = new WeakHashMap<>();

	public List<MatAnimController> materialAnimations = new ArrayList<>();
	public List<SkeletalController> skeletalAnimations = new ArrayList<>();
	public List<VisibilityController> visibilityAnimations = new ArrayList<>();
	public List<CameraAnimationController> cameraAnimations = new ArrayList<>();
	public List<KinematicsController> kinematics = new ArrayList<>();

	public Map<Skeleton, FastSkeleton> fastSkeletons = new WeakHashMap<>();

	public Map<Joint, Matrix4> animatedTransforms = new WeakHashMap<>();
	private Map<Joint, Matrix4> localBindTransforms = new WeakHashMap<>();
	public Map<Joint, Matrix4> globalBindTransforms = new WeakHashMap<>();

	public Map<String, CameraAnimationFrame> cameraTransforms = new WeakHashMap<>();

	public Map<String, Boolean> meshVisibilities = new WeakHashMap<>();
	public Map<String, Boolean> visGroupVisibilities = new WeakHashMap<>();
	public Map<String, Boolean> jointVisibilities = new WeakHashMap<>();

	public G3DResourceInstance instance;

	public G3DResourceState parent;
	public List<G3DResourceState> children = new ArrayList<>();

	public Map<Model, AABB6f> boundingBoxes = new WeakHashMap<>();

	public G3DResourceState(G3DResourceInstance instance) {
		this(instance, null);
	}

	public G3DResourceState(G3DResourceInstance instance, G3DResourceState parent) {
		this.instance = instance;
		this.parent = parent;

		//Local matrices
		updateProjMatrix = instance.hasProjectionMatrix();
		localModelMatrix = mallocMatrix();
		instance.getModelMatrix(localModelMatrix);
		cameraMatrix = mallocMatrix();
		modelMatrix = mallocMatrix();
		if (parent != null) {
			cameraMatrix.set(parent.cameraMatrix);
			modelMatrix.set(parent.modelMatrix);
		}

		//Local animation controllers
		materialAnimations = instance.getLocalResMatAnimControllers();
		skeletalAnimations = instance.getLocalResSklAnimControllers();
		cameraAnimations = instance.getLocalResCamAnimControllers();
		visibilityAnimations = instance.getLocalResVisAnimControllers();
		kinematics.addAll(instance.resourceKinematicsControllers);
		lights.addAll(instance.lights);

		//Calculate global transform
		if (parent != null) {
			if (instance.parentNodeName != null) {
				for (Map.Entry<Joint, Matrix4> transform : parent.animatedTransforms.entrySet()) {
					Joint j = transform.getKey();
					if (instance.parentNodeName.equals(j.name)) {
						modelMatrix.mul(transform.getValue());
						break;
					}
				}
			}

			switch (instance.parentMode) {
				case ALL:
					break;
				case TRANSLATION:
					Matrix4 temp = mallocMatrix();
					temp.set(parent.localModelMatrix);
					temp.clearTranslation();
					temp.invert();
					modelMatrix.mul(temp);
					RenderAllocator.freeMatrix(temp);
					break;
				case TRANSLATION_AND_ROTATION:
					modelMatrix.scale(parent.instance.getScale().recip());
					break;
				case SKIP:
					if (parent.parent != null) {
						modelMatrix.set(parent.parent.modelMatrix);
					} else {
						modelMatrix.identity();
					}
					break;
			}
		}

		mergeParentSceneData(parent);

		//Build local camera transform map
		for (CameraAnimationController ca : cameraAnimations) {
			for (Map.Entry<String, CameraAnimationFrame> t : ca.transforms.entrySet()) {
				cameraTransforms.put(t.getKey(), t.getValue());
			}
		}

		if (!instance.cameraInstances.isEmpty()) {
			cameras.addAll(instance.cameraInstances);
		} else if (parent != null) {
			cameras.addAll(parent.cameras);
		}

		applyCameraAnimation();

		modelMatrix.mul(localModelMatrix);
		cameraMatrix.multiplyRight(instance.getCameraMatrix(false));
		viewMatrix = mallocMatrix();
		viewMatrix.set(cameraMatrix);
		viewMatrix.invert();
		modelViewMatrix = createModelViewMatrix();
		normalMatrix = RenderAllocator.allocMatrix3f();
		modelViewMatrix.normal(normalMatrix);

		for (Light l : lights) {
			lightMatrices.put(l, modelViewMatrix);
		}

		setDefaultVisgroupStates();
		applyVisibilityAnimation();

		applySkeletalAnimation();

		setupBBoxes();

		for (G3DResourceInstance child : new ArrayList<>(instance.getChildren())) {
			children.add(new G3DResourceState(child, this));
		}
	}

	private Matrix4 mallocMatrix() {
		return RenderAllocator.allocMatrix();
	}

	private Matrix4 cloneMatrix(Matrix4 m) {
		Matrix4 m2 = mallocMatrix();
		m2.set(m);
		return m2;
	}

	private void destroyMatrixMap(Map<?, Matrix4> map) {
		for (Matrix4 m : map.values()) {
			RenderAllocator.freeMatrix(m);
		}
		map.clear();
	}

	public void destroy() {
		for (Camera cam : cameras) {
			cam.rotQuat = null;
		}
		RenderAllocator.freeMatrices(localModelMatrix, cameraMatrix, modelMatrix, viewMatrix, modelViewMatrix, projectionMatrix);
		RenderAllocator.freeMatrix3f(normalMatrix);

		//destroyMatrixMap(lightMatrices); //- light matrices are just copies of the modelview matrix - do not waste time destroying
		destroyMatrixMap(animatedTransforms);
		destroyMatrixMap(globalBindTransforms);
		destroyMatrixMap(localBindTransforms);
		lightMatrices = null;
		animatedTransforms = null;
		globalBindTransforms = null;
		localBindTransforms = null;

		for (SkeletalController sc : skeletalAnimations) {
			sc.freeMatrices();
		}

		for (G3DResourceState ch : children) {
			ch.destroy();
		}
	}

	private void mergeParentSceneData(G3DResourceState parent) {
		if (parent != null) {
			materialAnimations.addAll(parent.materialAnimations);
			if (skeletalAnimations.isEmpty()) {
				skeletalAnimations.addAll(parent.skeletalAnimations); //only add parent skeletal animations if not local animation is bound
			}

			cameraAnimations.addAll(parent.cameraAnimations);
			visibilityAnimations.addAll(parent.visibilityAnimations);
			for (Map.Entry<Joint, Matrix4> e : parent.animatedTransforms.entrySet()) {
				putAnimatedTransform(e.getKey(), e.getValue());
			}
			for (Map.Entry<String, CameraAnimationFrame> e : parent.cameraTransforms.entrySet()) {
				cameraTransforms.put(e.getKey(), e.getValue());
			}
			kinematics.addAll(parent.kinematics);
			lights.addAll(parent.lights);
			lightMatrices.putAll(parent.lightMatrices);
		}
	}

	public boolean getMeshVisibility(Model model, Mesh mesh) {
		if (!meshVisibilities.getOrDefault(mesh.name, true)) {
			return false;
		}
		for (Joint j : model.skeleton) {
			if (jointVisibilities.containsKey(j.name)) {
				//Joints override visgroups
				//The model will be forced to visible and visibility will be applied per-joint
				return true;
			}
		}
		if (mesh.visGroupName != null) {
			if (!visGroupVisibilities.getOrDefault(mesh.visGroupName, true)) {
				return false;
			}
		}
		return true;
	}

	private void applyCameraAnimation() {
		for (Camera cam : instance.cameraInstances) {
			if (cameraTransforms.containsKey(cam.name)) {
				cameraTransforms.get(cam.name).applyToCamera(cam);
			} else {
				Camera defaultCam = (Camera) instance.resource.getNamedResource(cam.name, G3DResourceType.CAMERA);
				if (defaultCam != null) {
					cam.copy(defaultCam);
				}
			}
			cam.calcTransformMatrix();
		}
	}

	private void setDefaultVisgroupStates() {
		if (instance != null && instance.resource != null) {
			for (MeshVisibilityGroup visgroup : instance.resource.visGroups()) {
				if (visGroupVisibilities.getOrDefault(visgroup.name, true)) {
					visGroupVisibilities.put(visgroup.name, visgroup.isVisible);
				}
			}
		}
	}

	private void applyVisibilityAnimation() {
		for (VisibilityController vis : visibilityAnimations) {
			if (vis.anim == null) {
				continue;
			}
			for (VisibilityBoneTransform trk : ((VisibilityAnimation) vis.anim).tracks) {
				boolean visible = vis.nodeVisibilities.getOrDefault(trk.name, true);
				switch (trk.target) {
					case JOINT:
						jointVisibilities.put(trk.name, visible);
						break;
					case MESH:
						meshVisibilities.put(trk.name, visible);
						break;
					case VISGROUP:
						visGroupVisibilities.put(trk.name, visible);
						break;
				}
			}
		}
	}

	private void applySkeletalAnimation() {
		for (SkeletalController sc : skeletalAnimations) {
			if (instance.resource != null) {
				for (Model mdl : instance.resource.models) {
					sc.makeAnimationMatrices(sc.frame, mdl.skeleton, true);
				}
			}
		}
		if (instance.resource != null) {
			for (Model mdl : instance.resource.models) {
				if (mdl.isVisible) {
					FastSkeleton fs = new FastSkeleton(mdl.skeleton);
					fastSkeletons.put(mdl.skeleton, fs);
					for (Joint j : mdl.skeleton) {
						if (j.parentName == null) {
							buildMatrices(j, fs);
						}
					}
				}
			}
		}
	}

	public final Matrix4 createModelViewMatrix() {
		Matrix4 mv = mallocMatrix();
		mv.set(viewMatrix);
		mv.mul(modelMatrix);
		modelViewMatrix = mv;
		return mv;
	}

	public final void setupBBoxes() {
		if (instance.resource != null) {
			Matrix4 mv = modelViewMatrix;
			for (Model mdl : instance.resource.models) {
				AABB6f aabb = new AABB6f(mdl.boundingBox);

				aabb.min.mulPosition(mv);
				aabb.max.mulPosition(mv);

				boundingBoxes.put(mdl, aabb);
			}
		}
	}

	public Matrix4 getProjectionMatrix() {
		if (projectionMatrix == null) {
			if (updateProjMatrix) {
				projectionMatrix = mallocMatrix();
				instance.getAbsoluteProjectionMatrix(projectionMatrix);
			} else {
				if (parent != null) {
					projectionMatrix = parent.getProjectionMatrix();
				} else {
					projectionMatrix = mallocMatrix();
				}
			}
		}
		return projectionMatrix;
	}

	private void buildMatrices(Joint j, FastSkeleton skl) {
		getAnimatedJointMatrix(j, skl);
		getGlobalJointMatrix(j, skl);
		getLocalJointMatrix(j);
		for (Joint ch : skl.getChildrenOf(j)) {
			buildMatrices(ch, skl);
		}
	}

	public FastSkeleton getFastSkeleton(Skeleton skl) {
		FastSkeleton fs = fastSkeletons.get(skl);
		if (fs == null) {
			fs = new FastSkeleton(skl);
			fastSkeletons.put(skl, fs);
		}
		return fs;
	}

	public Matrix4 getAnimatedJointMatrix(Joint modelBone, FastSkeleton modelSkeleton) {
		Matrix4 mtx = animatedTransforms.get(modelBone);
		Joint parentJoint;
		if (mtx == null) {
			mtx = mallocMatrix();
			if (modelBone != null) {
				boolean isAnimated = false;
				List<LocRotScaleSet> processedLRS = new ArrayList<>();
				LocRotScaleSet bindLRS = new LocRotScaleSet(getLocalJointMatrix(modelBone));
				processedLRS.add(bindLRS);
				parentJoint = modelBone.getParent();
				if (parentJoint != null) {
					mtx.mul(getAnimatedJointMatrix(parentJoint, modelSkeleton));
				}
				MainLoop:
				for (SkeletalController ctrl : skeletalAnimations) {
					FastSkeleton skeleton = modelSkeleton;

					if (ctrl.getSkeleton() != null) {
						skeleton = getFastSkeleton(ctrl.getSkeleton());
					}
					Joint motionBone = skeleton.getJoint(modelBone.name);
					if (motionBone == null) {
						continue;
					}
					Matrix4 animatedTransform = ctrl.getAnimatedTransform(skeleton.source, motionBone.name);

					if (animatedTransform != null) {
						animatedTransform = cloneMatrix(animatedTransform);

						if (modelBone.isScaleCompensate()) {
							if (motionBone.parentName != null) {
								Matrix4 parentAnmTrans = ctrl.getAnimatedTransform(skeleton.source, motionBone.parentName);
								if (parentAnmTrans == null) {
									parentAnmTrans = getLocalJointMatrix(parentJoint);
								}
								if (parentAnmTrans != null) {
									animatedTransform.scale(parentAnmTrans.getScale().recip());
								}
							}
						}

						LocRotScaleSet lrs = new LocRotScaleSet(animatedTransform);
						for (LocRotScaleSet p : processedLRS) {
							if (p.equals(lrs)) {
								RenderAllocator.freeMatrix3f(lrs.rotationMatrix);
								RenderAllocator.freeMatrix(animatedTransform);
								continue MainLoop;
							}
						}
						processedLRS.add(lrs);

						switch (motionBone.kinematicsRole) {
							default:
								mtx.mul(animatedTransform);
								break;
							case CHAIN:
								//Start calculating IKs at chain bone
								mtx.mul(animatedTransform);
								Joint jointBone = motionBone.getChildByType(Skeleton.KinematicsRole.JOINT);
								if (jointBone != null) {
									Joint effectorBone = jointBone.getChildByType(Skeleton.KinematicsRole.EFFECTOR);
									if (effectorBone != null) {
										InverseKinematics.IKInput input = new InverseKinematics.IKInput();
										input.chain = motionBone;	//The chain bone
										input.joint = jointBone;	//The joint bone
										input.effector = effectorBone;	//The effector bone
										input.globalChainMatrix = mtx;	//The global transform of the chain. Has to be calculated.
										input.localJointMatrix = getLocalAnimatedTransform(jointBone, skeleton.source, ctrl); //Animated transform of the joint

										Matrix4 animatedEffectorMatrix = ctrl.getAnimatedTransform(skeleton.source, effectorBone.name); //Animated transform of the effector
										if (animatedEffectorMatrix != null) {
											Vec3f effectorScale = animatedEffectorMatrix.getScale();
											Vec3f effectorRotation = animatedEffectorMatrix.getRotation(effectorScale);
											Vec3f effectorPosition = animatedEffectorMatrix.getTranslation();

											input.globalEffectorMatrix = Matrix4.createTranslation(effectorPosition); //Global effector matrix - only global translation of the effector.

											for (KinematicsController kc : kinematics) {
												if (kc.enabled) {
													if (kc.targetJointName.equals(effectorBone.name)) {
														kc.applyToMatrix(input.globalEffectorMatrix);
													}
												}
											}
											input.localEffectorMatrix = Matrix4.createTranslation(effectorBone.position);
											input.localEffectorMatrix.rotate(effectorRotation);
											input.localEffectorMatrix.scale(effectorScale.x, effectorScale.y, effectorScale.z); //Local effector matrix - animated SR of the effector with the bind pose local translation.

											InverseKinematics.IKOutput out = InverseKinematics.transformIK(input);

											mtx.set(out.chainMatrix);
											removeChildMatrices(modelBone, modelSkeleton);
											putAnimatedTransform(jointBone.name, modelSkeleton, cloneMatrix(out.jointMatrix));
											putAnimatedTransform(effectorBone.name, modelSkeleton, cloneMatrix(out.effectorMatrix));
											putAnimatedTransform(modelBone, mtx);
											calculateChildMatrices(modelBone, modelSkeleton);
										}//otherwise bind pose effector - transforms are local (non-IK)
									}
								}
								break;
						}
						RenderAllocator.freeMatrix(animatedTransform);
						isAnimated = true;
					}
				}
				if (!isAnimated) {
					mtx.mul(getLocalJointMatrix(modelBone));
				}
				for (LocRotScaleSet lrs : processedLRS) {
					RenderAllocator.freeMatrix3f(lrs.rotationMatrix);
				}
			}
			if (modelBone != null && modelBone.flags != 0) {
				if (modelBone.isBillboard()) {
					//Calculate billboard rotation
					Quaternion q = new Quaternion();
					Matrix3f normalizedGlobalRotation = RenderAllocator.allocMatrix3f();
					modelMatrix.normalize3x3(normalizedGlobalRotation);
					normalizedGlobalRotation.getNormalizedRotation(q).invert();
					RenderAllocator.freeMatrix3f(normalizedGlobalRotation);
					if (!modelBone.isBBAim()) {
						Vec3f rot = cameraMatrix.getRotation();

						if (modelBone.isBBZ()) {
							q.rotateZ(rot.z);
						}
						if (modelBone.isBBY()) {
							q.rotateY(rot.y);
						}
						if (modelBone.isBBX()) {
							q.rotateX(rot.x);
						}
					} else {
						Vec3f camPos = cameraMatrix.getTranslation();
						Vec3f modelPos = new Vec3f();
						Matrix4 temp = cloneMatrix(modelMatrix);
						temp.mul(mtx);
						temp.getTranslation(modelPos);
						RenderAllocator.freeMatrix(temp);
						modelPos.sub(camPos);

						if (modelBone.isBBY()) {
							q.rotateY((float) FAtan.atan2(-modelPos.x, -modelPos.z) - modelBone.rotation.y);
						}
						if (modelBone.isBBX()) {
							q.rotateX((float) FAtan.atan2(modelPos.y, (float) Math.hypot(modelPos.z, modelPos.x)) - modelBone.rotation.x);
						}
						if (modelBone.isBBZ()) {
							//UNIMPLEMENTED
						}
					}

					Vec3f tra = mtx.getTranslation();
					mtx.rotateAroundLocal(q, tra.x, tra.y, tra.z);
				}
			}

			putAnimatedTransform(modelBone, mtx);
		}
		return mtx;
	}

	private void putAnimatedTransform(String jointName, FastSkeleton skeleton, Matrix4 matrix) {
		Joint j = skeleton.getJoint(jointName);
		if (j != null) {
			Matrix4 exist = animatedTransforms.get(j);
			if (exist != null) {
				RenderAllocator.freeMatrix(exist);
			}
			animatedTransforms.put(j, matrix);
		}
	}

	private void putAnimatedTransform(Joint j, Matrix4 matrix) {
		animatedTransforms.put(j, matrix);
	}

	public Matrix4 getLocalAnimatedTransform(Joint j, Skeleton skeleton, SkeletalController controller) {
		if (controller == null) {
			return getLocalJointMatrix(j);
		}
		Matrix4 mtx = controller.getAnimatedTransform(skeleton, j.name);
		if (mtx == null) {
			mtx = getLocalJointMatrix(j);
		}
		return mtx;
	}

	private void calculateChildMatrices(Joint j, FastSkeleton skl) {
		getAnimatedJointMatrix(j, skl);
		for (Joint ch : skl.getChildrenOf(j)) {
			calculateChildMatrices(ch, skl);
		}
	}

	private void removeChildMatrices(Joint j, FastSkeleton skl) {
		animatedTransforms.remove(j);
		for (Joint ch : skl.getChildrenOf(j)) {
			removeChildMatrices(ch, skl);
		}
	}

	private static class LocRotScaleSet {

		Vec3f p;
		Vec3f s;
		Matrix3f rotationMatrix = RenderAllocator.allocMatrix3f();

		public LocRotScaleSet(Matrix4 mtx) {
			p = mtx.getTranslation();
			s = mtx.getScale();
			mtx.get3x3(rotationMatrix);
			/*float f2p = (float) (2 * Math.PI);
			r.x %= f2p;
			r.y %= f2p;
			r.z %= f2p;*/
		}

		@Override
		public String toString() {
			Matrix4 rm = new Matrix4();
			rotationMatrix.get(rm);
			return p.toString() + rm.getRotation() + s.toString();
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof LocRotScaleSet) {
				LocRotScaleSet set = (LocRotScaleSet) o;
				return set.p.equals(p, 0.00001f) && set.rotationMatrix.equals(rotationMatrix, 0.00001f) && set.s.equals(s, 0.00001f);
			}
			return false;
		}
	}

	public Matrix4 getGlobalJointMatrix(Joint j, FastSkeleton skl) {
		Matrix4 mtx = globalBindTransforms.get(j);
		if (mtx == null) {
			mtx = mallocMatrix();
			if (j != null) {
				Joint parentJoint = skl.getJoint(j.parentName);
				mtx.set(getGlobalJointMatrix(parentJoint, skl));
				Matrix4 local = getLocalJointMatrix(j);
				mtx.mul(local);
			}
			globalBindTransforms.put(j, mtx);
		}
		return mtx;
	}

	public Matrix4 getLocalJointMatrix(Joint j) {
		Matrix4 bt = localBindTransforms.get(j);
		if (bt == null) {
			bt = mallocMatrix();
			j.getLocalMatrix(bt);
			localBindTransforms.put(j, bt);
		}
		return bt;
	}

	public static class FastSkeleton {

		public final Skeleton source;

		private Map<String, Joint> joints;

		private Map<String, List<Joint>> children;

		public FastSkeleton(Skeleton skl) {
			this.source = skl;
			joints = new WeakHashMap<>(skl.getJointCount());
			children = new WeakHashMap<>(joints.size());
			for (Joint j : skl.getJoints()) {
				joints.put(j.name, j);
				List<Joint> childList = children.get(j.parentName);
				if (childList == null) {
					childList = new ArrayList<>();
					children.put(j.parentName, childList);
				}
				childList.add(j);
				if (!children.containsKey(j.name)) {
					children.put(j.name, new ArrayList<>());
				}
			}
		}

		public List<Joint> getChildrenOf(Joint j) {
			return children.getOrDefault(j.name, new ArrayList<>());
		}

		public Joint getJoint(String name) {
			return joints.get(name);
		}
	}
}
