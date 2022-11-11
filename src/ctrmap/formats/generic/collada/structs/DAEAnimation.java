package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAEConvMemory;
import ctrmap.formats.generic.collada.DAEExportSettings;
import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraLookAtBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.InverseKinematics;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationFrame;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationTransformRequest;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.util.CameraFrameProcessor;
import ctrmap.renderer.util.camcvt.IKBakery;
import ctrmap.renderer.util.camcvt.SkeletalMatrixBakery;
import xstandard.math.vec.Matrix4;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEAnimation implements DAEIDAble, DAESerializable {

	public String name;
	public String id;

	DAEDict<DAEAnimation> subAnimations = new DAEDict<>();
	DAEDict<DAEChannel> channels = new DAEDict<>();

	private DAEAnimation() {

	}

	public DAEAnimation(Element elem) {
		this.name = elem.getAttribute("name");
		this.id = elem.getAttribute("id");

		List<Element> srcList = XmlFormat.getElementsByTagName(elem, "source");
		List<Element> channelList = XmlFormat.getElementsByTagName(elem, "channel");
		List<Element> samplerList = XmlFormat.getElementsByTagName(elem, "sampler");

		DAEDict<DAESource> sources = new DAEDict<>();
		DAEDict<DAESampler> samplers = new DAEDict<>();

		for (Element src : srcList) {
			sources.putNode(new DAESource(src));
		}
		for (Element samp : samplerList) {
			samplers.putNode(new DAESampler(samp));
		}
		for (Element channel : channelList) {
			channels.putNode(new DAEChannel(channel, sources, samplers));
		}

		for (Element subAnim : XmlFormat.getElementsByTagName(elem, "animation")) {
			subAnimations.putNode(new DAEAnimation(subAnim));
		}
	}

	public DAEAnimation(VisibilityAnimation anm, List<Mesh> meshes, Skeleton skl, DAEConvMemory<AbstractAnimation, DAEAnimation> anmConvMem, DAEConvMemory<Joint, DAENode> jointConvMem, Map<Mesh, DAENode> meshRootNodes) {
		name = XmlFormat.sanitizeName(anm.name);
		anmConvMem.put(anm, this);

		for (VisibilityBoneTransform bt : anm.tracks) {
			DAENode node = null;

			if (bt.target == VisibilityBoneTransform.Target.MESH) {
				Mesh mesh = Scene.getNamedObject(bt.name, meshes);
				if (mesh != null) {
					node = meshRootNodes.get(mesh);
				}
			} else {
				Joint jnt = skl.getJoint(bt.name);
				if (jnt != null) {
					node = jointConvMem.findByInput(jnt);
				}
			}

			if (node != null) {
				KeyFrameList inv = new KeyFrameList(bt.isVisible, true);
				for (KeyFrame kf : inv) {
					if (kf.value == 0f) {
						kf.value = 1f;
					} else {
						kf.value = 0f;
					}
				}

				//blender visibility node transform
				putSubanimIfNonempty(inv, node.getID(), "", false, "X", "Y", "Z");
			}
		}
	}

	public DAEAnimation(CameraAnimation anm, List<Camera> cameras, DAEConvMemory<Camera, DAECamera> camConvMem, Map<Camera, DAENode> camNodes, DAEConvMemory<AbstractAnimation, DAEAnimation> anmConvMem, DAEExportSettings settings) {
		this.name = XmlFormat.sanitizeName(anm.name);
		anmConvMem.put(anm, this);

		int frameCount = (int) anm.getFrameCountMaxTime();

		for (CameraBoneTransform bt : anm.transforms) {
			Camera cam = Scene.getNamedObject(bt.name, cameras);

			if (cam != null) {
				DAENode node = camNodes.get(cam);
				if (node != null) {
					String camId = camConvMem.findByInput(cam).getID();
					String nodeId = node.getID();

					if (!settings.bakeTransforms) {
						CameraViewpointBoneTransform vp = null;
						CameraLookAtBoneTransform la = null;

						if (bt instanceof CameraViewpointBoneTransform) {
							vp = (CameraViewpointBoneTransform) bt;
						} else if (bt instanceof CameraLookAtBoneTransform) {
							if (settings.doNotUseLookAt) {
								vp = CameraFrameProcessor.lookatToViewpoint((CameraLookAtBoneTransform) bt, cam, frameCount);
							} else {
								la = (CameraLookAtBoneTransform) bt;
							}
						}

						if (vp != null) {
							putSubanimIfNonempty(bt.tx, nodeId, "location.X", false, "X");
							putSubanimIfNonempty(bt.ty, nodeId, "location.Y", false, "Y");
							putSubanimIfNonempty(bt.tz, nodeId, "location.Z", false, "Z");

							putSubanimIfNonempty(vp.rx, nodeId, "rotationX.ANGLE", vp.isRadians, "ANGLE");
							putSubanimIfNonempty(vp.ry, nodeId, "rotationY.ANGLE", vp.isRadians, "ANGLE");
							putSubanimIfNonempty(vp.rz, nodeId, "rotationZ.ANGLE", vp.isRadians, "ANGLE");
						}
						else if (la != null) {
							//I don't think anyone has ever used this part of the COLLADA schema,
							//but it should be up to the spec.
							putSubanimIfNonempty(bt.tx, nodeId, "lookat.Px", false, "Px");
							putSubanimIfNonempty(bt.ty, nodeId, "lookat.Py", false, "Py");
							putSubanimIfNonempty(bt.tz, nodeId, "lookat.Pz", false, "Pz");
							
							putSubanimIfNonempty(la.targetTX, nodeId, "lookat.Ix", false, "Ix");
							putSubanimIfNonempty(la.targetTY, nodeId, "lookat.Iy", false, "Iy");
							putSubanimIfNonempty(la.targetTZ, nodeId, "lookat.Iz", false, "Iz");
							
							putSubanimIfNonempty(la.upX, nodeId, "lookat.UPx", false, "UPx");
							putSubanimIfNonempty(la.upY, nodeId, "lookat.UPy", false, "UPy");
							putSubanimIfNonempty(la.upZ, nodeId, "lookat.UPz", false, "UPz");
						}
					} else {
						Camera dmyAnimCam = new Camera(cam);

						Matrix4[] matrices = new Matrix4[frameCount + 1];

						for (int frame = 0; frame <= frameCount; frame++) {
							bt.getFrame(frame).applyToCamera(dmyAnimCam);
							matrices[frame] = dmyAnimCam.getTransformMatrix(false);
						}

						putMtxSubanim(nodeId, matrices);
					}

					putSubanimIfNonempty(bt.fov, camId, "yfov", bt.isRadians, "X", "Y", "Z"); //blender again being weird, writing floats as XYZ
					putSubanimIfNonempty(bt.zNear, camId, "znear", false, "X", "Y", "Z");
					putSubanimIfNonempty(bt.zFar, camId, "zfar", false, "X", "Y", "Z");
				}
			}
		}
	}

	public DAEAnimation(SkeletalAnimation anm, Skeleton skl, DAEConvMemory<AbstractAnimation, DAEAnimation> anmConvMem, DAEConvMemory<Joint, DAENode> jointConvMem, DAEExportSettings settings) {
		this.name = XmlFormat.sanitizeName(anm.name);
		anmConvMem.put(anm, this); //generate ID

		int frameCount = (int) anm.getFrameCountMaxTime();

		for (SkeletalBoneTransform bt : anm.bones) {
			Joint joint = skl.getJoint(bt.name);
			DAENode node = jointConvMem.findByInput(joint);
			if (node != null) {
				String btId = node.getID();

				if (!settings.bakeTransforms && joint.kinematicsRole == Skeleton.KinematicsRole.NONE) {
					putSubanimIfNonempty(bt.tx, btId, "location.X", false, "X");
					putSubanimIfNonempty(bt.ty, btId, "location.Y", false, "Y");
					putSubanimIfNonempty(bt.tz, btId, "location.Z", false, "Z");

					putSubanimIfNonempty(bt.rx, btId, "rotationX.ANGLE", true, "ANGLE");
					putSubanimIfNonempty(bt.ry, btId, "rotationY.ANGLE", true, "ANGLE");
					putSubanimIfNonempty(bt.rz, btId, "rotationZ.ANGLE", true, "ANGLE");

					putSubanimIfNonempty(bt.sx, btId, "scale.X", false, "X");
					putSubanimIfNonempty(bt.sy, btId, "scale.Y", false, "Y");
					putSubanimIfNonempty(bt.sz, btId, "scale.Z", false, "Z");
				} else {
					switch (joint.kinematicsRole) {
						case NONE:
							Matrix4[] matrices = new Matrix4[(int) frameCount + 1];

							SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(0f);
							SkeletalAnimationTransformRequest parentScaleReq = new SkeletalAnimationTransformRequest(0f);
							parentScaleReq.translation = false;
							parentScaleReq.rotation = false;

							req.bindJoint = skl.getJoint(bt.name);
							if (req.bindJoint != null) {
								Joint parentJoint = skl.getJoint(req.bindJoint.parentName);
								parentScaleReq.bindJoint = parentJoint;
								SkeletalBoneTransform parentBT = (SkeletalBoneTransform) anm.getBoneTransform(req.bindJoint.parentName);

								for (int frame = 0; frame <= frameCount; frame++) {
									req.frame = frame;
									matrices[frame] = bt.getTransformMatrix(req);
									if (req.bindJoint.isScaleCompensate() && parentJoint != null) {
										if (parentBT == null) {
											matrices[frame].invScale(parentJoint.scale);
										} else {
											parentScaleReq.frame = frame;
											matrices[frame].invScale(parentBT.getFrame(parentScaleReq).getScale());
										}
									}
								}

								putMtxSubanim(btId, matrices);
							}
							break;
						case CHAIN:
							Joint jointIK = joint.getChildByType(Skeleton.KinematicsRole.JOINT);
							if (jointIK != null) {
								Joint effector = jointIK.getChildByType(Skeleton.KinematicsRole.EFFECTOR);
								if (effector != null) {
									SkeletalBoneTransform jbt = (SkeletalBoneTransform) anm.getBoneTransform(jointIK.name);
									SkeletalBoneTransform ebt = (SkeletalBoneTransform) anm.getBoneTransform(effector.name);

									if (ebt != null) {
										if (jbt == null) {
											jbt = new SkeletalBoneTransform();
										}
										SkeletalMatrixBakery chainBakery = new SkeletalMatrixBakery(anm, skl, joint);

										Matrix4[] matricesChain = new Matrix4[(int) frameCount + 1];
										Matrix4[] matricesJoint = new Matrix4[matricesChain.length];
										Matrix4[] matricesEffector = new Matrix4[matricesChain.length];

										for (int frame = 0; frame <= frameCount; frame++) {
											InverseKinematics.IKOutput out = IKBakery.calcIKMatrices(frame, skl, chainBakery, bt, jbt, ebt);
											out.effectorMatrix.mulLocal(out.jointMatrix.getInverseMatrix());
											out.jointMatrix.mulLocal(out.chainMatrix.invert());
											matricesChain[frame] = chainBakery.manualBakeLocal(frame);
											matricesJoint[frame] = out.jointMatrix;
											matricesEffector[frame] = out.effectorMatrix;
										}

										putMtxSubanim(btId, matricesChain);
										putMtxSubanim(jointConvMem.findByInput(jointIK).getID(), matricesJoint);
										putMtxSubanim(jointConvMem.findByInput(effector).getID(), matricesEffector);
									}
								}
							}
							break;
					}
				}
			} else {
				System.err.println("Could not find node " + bt.name + ", skipping..");
			}
		}
	}

	private void putMtxSubanim(String btId, Matrix4[] matrices) {
		DAEAnimation daeAnmMtx = new DAEAnimation();
		daeAnmMtx.id = btId + "-transform";

		DAEChannel channel = new DAEChannel(matrices, btId, "transform", "TRANSFORM");
		daeAnmMtx.channels.putNode(channel);

		subAnimations.putNode(daeAnmMtx);
	}

	private void putSubanimIfNonempty(KeyFrameList kfl, String targetBone, String targetTransform, boolean toDegrees, String... curveLabels) {
		if (!kfl.isEmpty()) {
			subAnimations.putNode(createSubAnimation(kfl, targetBone, targetTransform, toDegrees, curveLabels));
		}
	}

	private DAEAnimation createSubAnimation(KeyFrameList kfl, String targetBone, String targetTransform, boolean toDegrees, String... curveLabels) {
		DAEAnimation anm = new DAEAnimation();
		anm.id = id + "-" + XmlFormat.sanitizeName(targetBone) + "-" + XmlFormat.sanitizeName(targetTransform);

		DAEChannel channel = new DAEChannel(kfl, targetBone, targetTransform, toDegrees, curveLabels);
		anm.channels.putNode(channel);

		return anm;
	}

	public static String getAnmTypeSuffixShort(AbstractAnimation a) {
		if (a instanceof SkeletalAnimation) {
			return "Skl";
		}
		if (a instanceof CameraAnimation) {
			return "Cam";
		}
		if (a instanceof VisibilityAnimation) {
			return "Vis";
		}
		if (a instanceof MaterialAnimation) {
			return "Mat";
		}
		throw new RuntimeException();
	}

	@Override
	public Element createElement(Document doc) {
		Element elem = doc.createElement("animation");
		elem.setAttribute("id", id);
		XmlFormat.setAttributeNonNull(elem, "name", name);

		List<Element> channelElems = new ArrayList<>();

		for (DAEChannel ch : channels) {
			Element chElem = doc.createElement("channel");

			String daeTgt = "";
			if (ch.targetBone != null) {
				daeTgt += ch.targetBone;
			}
			if (ch.targetTransform != null) {
				daeTgt += "/" + ch.targetTransform;
			}
			chElem.setAttribute("target", daeTgt);
			channelElems.add(chElem);

			DAESampler sampler = new DAESampler();

			DAESource inSrc = new DAESource(ch.frameTimes, "TIME");
			inSrc.setID(XmlFormat.makeSafeId(id + "-input"));
			elem.appendChild(inSrc.createElement(doc));

			DAESource interpSrc = new DAESource(ch.interpolations, "INTERPOLATION");
			interpSrc.setID(XmlFormat.makeSafeId(id + "-interpolation"));
			elem.appendChild(interpSrc.createElement(doc));

			DAESource outSrc = ch.curve;
			outSrc.setID(XmlFormat.makeSafeId(id + "-output"));
			elem.appendChild(outSrc.createElement(doc));

			sampler.setID(XmlFormat.makeSafeId(id + "-sampler"));
			sampler.inputs.putNode(new DAEInput("INPUT", inSrc, -1));
			sampler.inputs.putNode(new DAEInput("OUTPUT", ch.curve, -1));
			sampler.inputs.putNode(new DAEInput("INTERPOLATION", interpSrc, -1));

			if (ch.inTangents != null) {
				DAESource inTanSrc = new DAESource(ch.inTangents, "X", "Y");
				inTanSrc.setID(XmlFormat.makeSafeId(id + "-intangent"));
				elem.appendChild(inTanSrc.createElement(doc));

				sampler.inputs.putNode(new DAEInput("IN_TANGENT", inTanSrc, -1));
			}

			if (ch.outTangents != null) {
				DAESource outTanSrc = new DAESource(ch.outTangents, "X", "Y");
				outTanSrc.setID(XmlFormat.makeSafeId(id + "-outtangent"));
				elem.appendChild(outTanSrc.createElement(doc));

				sampler.inputs.putNode(new DAEInput("OUT_TANGENT", outTanSrc, -1));
			}
			elem.appendChild(sampler.createElement(doc));
			chElem.setAttribute("source", sampler.getURL());
		}

		for (Element ce : channelElems) {
			elem.appendChild(ce);
		}

		for (DAEAnimation childAnm : subAnimations) {
			elem.appendChild(childAnm.createElement(doc));
		}

		return elem;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}
}
