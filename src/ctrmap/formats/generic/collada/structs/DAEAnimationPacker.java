package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAE;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.util.AnimeProcessor;
import xstandard.math.MathEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DAEAnimationPacker {

	public static final float HALF_PI = (float) Math.PI / 2f;

	public static List<AbstractAnimation> createAnimations(DAE scene, Map<DAEVisualScene, Skeleton> skeletons, DAEPostProcessConfig cfg, List<DAEChannel> channels) {
		List<AbstractAnimation> r = new ArrayList<>();

		CameraAnimation camAnm = new CameraAnimation();
		camAnm.name = "CameraAnimation";

		for (DAECamera cam : scene.cameras) {
			if (cam.valid()) {
				List<DAEChannel> camChannels = getChannelsByBoneId(channels, cam.getAnimationTargetID());
				if (!camChannels.isEmpty()) {
					CameraViewpointBoneTransform bt = new CameraViewpointBoneTransform();
					bt.name = cam.name;

					for (DAEChannel ch : camChannels) {
						if (ch.isCamera()) {
							switch (ch.targetTransform) {
								case "xfov":
									bt.fov.set(ch.camTransform.fov);
									for (KeyFrame kf : bt.fov) {
										kf.value = MathEx.toDegreesf(Camera.fovXToFovY(MathEx.toRadiansf(kf.value), cam.proj.aspect));
									}
									break;
								case "yfov":
									bt.fov.set(ch.camTransform.fov);
									break;
								case "znear":
									bt.zNear.set(ch.camTransform.zNear);
									break;
								case "zfar":
									bt.zFar.set(ch.camTransform.zFar);
									break;
							}
							camAnm.frameCount = Math.max(camAnm.frameCount, ch.timeMax * 30);
						}
					}

					if (bt.exists()) {
						camAnm.transforms.add(bt);
					}
				}
			}
		}

		for (DAEVisualScene vs : scene.visualScenes) {
			SkeletalAnimation sklAnm = new SkeletalAnimation();
			VisibilityAnimation visAnm = new VisibilityAnimation();
			sklAnm.name = vs.name + "_SKLA";
			visAnm.name = vs.name + "_VISA";
			sklAnm.skeleton = skeletons.get(vs);

			Map<SkeletalBoneTransform, CameraViewpointBoneTransform> skel2CamQueue = new HashMap<>();
			Map<SkeletalBoneTransform, Float> skel2XRots = new HashMap<>();

			for (DAENode node : vs.getAllNodes()) {
				List<DAEChannel> boneChannels = getChannelsByBoneId(channels, node.getID());
				if (!boneChannels.isEmpty() || node.parent == null) {
					VisibilityBoneTransform visBT = new VisibilityBoneTransform();
					SkeletalBoneTransform skelBT = new SkeletalBoneTransform();

					boolean hasMatrix = false;

					for (DAEChannel ch : boneChannels) {
						AbstractAnimation anm = null;

						if (ch.isVisibility()) {
							anm = visAnm;

							visBT = ch.visTransform;
						} else if (ch.isCamera()) {

						} else {
							anm = sklAnm;

							SkeletalBoneTransform source = ch.sklTransform;

							switch (ch.targetTransform) {
								case "transform":
								case "matrix":
									skelBT = source;
									hasMatrix = true;
									break;
								case "translate":
									skelBT.tx = source.tx;
									skelBT.ty = source.ty;
									skelBT.tz = source.tz;
									break;
								case "rotate":
									skelBT.rx = source.rx;
									skelBT.ry = source.ry;
									skelBT.rz = source.rz;
									break;
								case "scale":
									skelBT.sx = source.sx;
									skelBT.sy = source.sy;
									skelBT.sz = source.sz;
									break;
								case "rotateX.ANGLE":
								case "rotationX.ANGLE":
									skelBT.rx = source.rx;
									break;
								case "rotateY.ANGLE":
								case "rotationY.ANGLE":
									skelBT.ry = source.ry;
									break;
								case "rotateZ.ANGLE":
								case "rotationZ.ANGLE":
									skelBT.rz = source.rz;
									break;
								case "location.X":
									skelBT.tx = source.tx;
									break;
								case "location.Y":
									skelBT.ty = source.ty;
									break;
								case "location.Z":
									skelBT.tz = source.tz;
									break;
							}
						}
						if (anm != null) {
							anm.frameCount = Math.max(anm.frameCount, ch.timeMax * 30);
						}
					}
					
					boolean isRootAndZUP = node.parent == null && cfg.upAxis == DAEPostProcessConfig.DAEUpAxis.Z_UP;

					if (isRootAndZUP && skelBT.exists()) {
						for (KeyFrame kf : skelBT.rx) {
							kf.value -= HALF_PI;
						}
						KeyFrameList temp = new KeyFrameList(skelBT.ry, false);
						skelBT.ry.set(skelBT.rz);
						skelBT.rz.set(temp);
						for (KeyFrame kf : skelBT.rz) {
							kf.value = -kf.value;
						}
						KeyFrameList newTY = new KeyFrameList(skelBT.tz, false);
						KeyFrameList newTZ = new KeyFrameList(skelBT.ty, false);
						for (KeyFrame kf : newTZ) {
							kf.value = -kf.value;
							kf.inSlope = -kf.inSlope;
							kf.outSlope = -kf.outSlope;
						}
						skelBT.ty = newTY;
						skelBT.tz = newTZ;
						if (skelBT.rx.isEmpty()) {
							skelBT.rx.add(new KeyFrame(0, -HALF_PI));
						}
					}

					skelBT.name = node.name;
					visBT.name = node.name;
					if (skelBT.exists()) {
						sklAnm.bones.add(skelBT);

						for (DAEInstance inst : node.instances) {
							if (inst.type == DAEInstance.InstanceType.CAMERA) {
								//apply transform to instantiated camera animation
								DAECamera cam = scene.cameras.getByUrl(inst.url);
								if (cam != null) {
									CameraViewpointBoneTransform camBT = (CameraViewpointBoneTransform) camAnm.getBoneTransform(cam.name);
									if (camBT == null) {
										camBT = new CameraViewpointBoneTransform();
										camBT.name = cam.name;
										camAnm.transforms.add(camBT);
									}
									skel2CamQueue.put(skelBT, camBT);
									skel2XRots.put(skelBT, hasMatrix || node.parent != null ? 0 : node.r.x); //todo wtf is up with blender 2.9 again
								}
							}
						}
					}
					if (!visBT.isVisible.isEmpty()) {
						visAnm.tracks.add(visBT);
					}
				}
			}
			if (!sklAnm.bones.isEmpty()) {
				r.add(sklAnm);
			}
			if (!visAnm.tracks.isEmpty()) {
				r.add(visAnm);
			}

			//process skel2cam
			Skeleton skl = skeletons.get(vs);

			if (!skel2CamQueue.isEmpty()) {
				camAnm.frameCount = Math.max(camAnm.frameCount, sklAnm.frameCount);

				for (Map.Entry<SkeletalBoneTransform, CameraViewpointBoneTransform> s2c : skel2CamQueue.entrySet()) {
					AnimeProcessor.skeletalToCamera(sklAnm, s2c.getKey().name, skl, true, skel2XRots.get(s2c.getKey()), s2c.getValue());
				}
			}
		}

		if (!camAnm.transforms.isEmpty()) {
			r.add(camAnm);
		}
		return r;
	}

	public static List<AbstractAnimation> createAnimations(DAE scene, Map<DAEVisualScene, Skeleton> skeletons, DAEPostProcessConfig cfg) {
		//Top-level animations
		List<DAEChannel> channels = new ArrayList<>();
		for (DAEAnimation a : scene.animations) {
			channels.addAll(a.channels.content);
		}

		List<AbstractAnimation> list = new ArrayList<>();

		if (!channels.isEmpty()) {
			list.addAll(createAnimations(scene, skeletons, cfg, channels));
		}

		for (DAEAnimation a : scene.animations) {
			List<DAEChannel> subChannels = new ArrayList<>();
			for (DAEAnimation sub : a.subAnimations) {
				subChannels.addAll(sub.channels.content);
			}

			if (!subChannels.isEmpty()) {
				List<AbstractAnimation> subAnm = createAnimations(scene, skeletons, cfg, subChannels);
				if (a.name != null && !a.name.isEmpty()) {
					for (AbstractAnimation anm : subAnm) {
						anm.name = a.name;
					}
				}
				list.addAll(subAnm);
			}
		}

		return list;
	}
	
	private static String getAnmTypeSuffix(AbstractAnimation anm) {
		if (anm instanceof SkeletalAnimation) {
			return "_SKLA";
		}
		if (anm instanceof VisibilityAnimation) {
			return "_VISA";
		}
		if (anm instanceof CameraAnimation) {
			return "_CAM";
		}
		return "";
	}

	public static List<DAEChannel> getChannelsByBoneId(List<DAEChannel> channels, String boneId) {
		List<DAEChannel> r = new ArrayList<>();
		for (DAEChannel ch : channels) {
			if (ch.targetBone.equals(boneId)) {
				r.add(ch);
			}
		}
		return r;
	}
}
