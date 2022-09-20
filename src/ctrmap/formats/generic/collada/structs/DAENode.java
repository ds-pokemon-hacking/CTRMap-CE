package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAE;
import ctrmap.formats.generic.collada.DAEConvMemory;
import ctrmap.formats.generic.collada.DAEExportSettings;
import ctrmap.formats.generic.collada.DAESIDConvMemory;
import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import xstandard.math.MatrixUtil;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAENode implements DAEIDAble, DAESerializable, DAESIDAble {

	private String id;
	private String sid;

	public String name;

	public DAENode parent;
	public List<DAENode> children = new ArrayList<>();

	public Vec3f t = new Vec3f();
	public Vec3f r = new Vec3f();
	public Vec3f s = new Vec3f(1f, 1f, 1f);

	public Vec3f[] lookAt;

	public List<DAEInstance> instances = new ArrayList<>();

	public boolean isNode = false;

	public boolean bake = false;

	public DAENode(Element node) {
		this(node, null);
	}

	public DAENode(Skeleton skl, Joint j, DAEConvMemory<Joint, DAENode> conv, DAESIDConvMemory<Joint, DAENode> convSID, DAEExportSettings settings) {
		name = XmlFormat.sanitizeName(j.name);
		bake = settings.bakeAnimations || j.kinematicsRole != Skeleton.KinematicsRole.NONE;
		t = j.position;
		r = j.rotation;
		s = j.scale;
		conv.put(j, this);
		convSID.put(j, this);
		if (skl != null) {
			parent = conv.findByInput(skl.getJoint(j.parentName));
			for (Joint child : skl.getChildrenOf(j)) {
				DAENode chNode = new DAENode(skl, child, conv, convSID, settings);
				children.add(chNode);
			}
		}
	}

	public DAENode(Element node, DAENode parent) {
		id = node.getAttribute("id");
		name = node.getAttribute("name");
		sid = node.getAttribute("sid");
		isNode = Objects.equals("NODE", node.getAttribute("type"));
		this.parent = parent;

		List<Element> childNodes = XmlFormat.getElementsByTagName(node, "node");
		for (Element child : childNodes) {
			children.add(new DAENode(child, this));
		}

		boolean hasTransform = false;

		List<Element> matrices = XmlFormat.getElementsByTagName(node, "matrix");
		for (Element matrixElem : matrices) {
			if (!matrixElem.hasAttribute("sid") || matrixElem.getAttribute("sid").equals("transform") || matrixElem.getAttribute("sid").equals("matrix")) {
				Matrix4 mtx = Matrix4.createRowMajor(XmlFormat.getFloatArrayValue(matrixElem));

				t = mtx.getTranslation();
				s = mtx.getScale();
				r = mtx.getRotation(s);

				hasTransform = true;
				break;
			}
		}

		Element la = XmlFormat.getElementByPath(node, "lookat");
		if (la != null) {
			float[] floats = XmlFormat.getFloatArrayValue(la);
			lookAt = new Vec3f[3];
			for (int i = 0, off = 0; i < 3; i++, off += 3) {
				lookAt[i] = new Vec3f(floats[off], floats[off + 1], floats[off + 2]);
			}
		}

		if (!hasTransform) {
			List<Element> tra = XmlFormat.getElementsByTagName(node, "translate");
			List<Element> rot = XmlFormat.getElementsByTagName(node, "rotate");
			List<Element> sca = XmlFormat.getElementsByTagName(node, "scale");

			Element se = XmlFormat.getByAttribute(sca, "sid", "scale");
			Element te = XmlFormat.getByAttribute(tra, "sid", "location");
			Element re = XmlFormat.getByAttribute(tra, "sid", "rotation");

			if (se != null) {
				s = new Vec3f(XmlFormat.getFloatArrayValue(se));
			}
			if (te != null) {
				t = new Vec3f(XmlFormat.getFloatArrayValue(te));
			}
			if (re != null) {
				r = new Vec3f(XmlFormat.getFloatArrayValue(re));
			} else {
				Matrix4 mtx = new Matrix4();
				for (Element ra : rot) {
					Vec4f rotAxis = new Vec4f(XmlFormat.getFloatArrayValue(ra));
					mtx.rotate((float) Math.toRadians(rotAxis.w), rotAxis.x, rotAxis.y, rotAxis.z);
				}
				this.r = mtx.getRotation();
			}
		}

		/*if (cfg.isZUp) {
			Matrix4 localMatrix = new Matrix4();
			localMatrix.translate(t.x, t.y, t.z);
			localMatrix.multMatrix(MatrixUtil.createRotation(r.x, r.y, r.z));
			localMatrix.scale(s.x, s.y, s.z);
			t = MatrixUtil.translationFromMatrix(localMatrix);
			s = MatrixUtil.scaleFromMatrix(localMatrix);
			r = MatrixUtil.rotationFromMatrix(localMatrix, s);
		}*/
		List<Element> instanceElems = XmlFormat.getElementList(XmlFormat.nodeListToListOfNodes(node.getChildNodes()));
		for (Element inst : instanceElems) {
			if (inst.getTagName().contains("instance")) {
				instances.add(new DAEInstance(inst));
			}
		}
	}

	public Object findInstantiatedObject(DAE dae, DAEInstance.InstanceType type, String id) {
		for (DAEInstance inst : instances) {
			if (inst.type == type) {
				DAEDict dict = null;

				switch (type) {
					case CONTROLLER:
						dict = dae.controllers;
						break;
					case GEOMETRY:
						dict = dae.geometries;
						break;
					case EFFECT:
						dict = dae.effects;
						break;
					case MATERIAL:
						dict = dae.materials;
						break;
					case CAMERA:
						dict = dae.cameras;
						break;
				}

				if (dict != null) {
					DAEIDAble elem = dict.getByUrl(inst.url);

					if (elem != null) {
						if (elem.getID().equals(id)) {
							return elem;
						}
						if (type == DAEInstance.InstanceType.CAMERA) {
							DAECamera cam = (DAECamera) elem;
							//Blender fix
							if (cam.getAnimationTargetID().equals(id)) {
								return cam;
							}
						}
					}
				}
			}
		}
		return null;
	}

	public Matrix4 getAbsoluteTransform() {
		Matrix4 mtx;
		if (parent != null) {
			mtx = parent.getAbsoluteTransform();
		} else {
			mtx = new Matrix4();
		}
		mtx.mul(getRelativeTransform());
		return mtx;
	}

	public Matrix4 getRelativeTransform() {
		Matrix4 mtx = new Matrix4();
		mtx.translate(t);
		mtx.rotate(r);
		mtx.scale(s);
		return mtx;
	}

	private boolean isPointlessNode() {
		if (isNode) {
			for (DAEInstance inst : instances) {
				if (inst.type != DAEInstance.InstanceType.GEOMETRY && inst.type != DAEInstance.InstanceType.CONTROLLER) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	public List<Joint> toSklJoints(DAEPostProcessConfig cfg) {
		List<Joint> list = new ArrayList<>();

		if (!isPointlessNode()) {
			Joint j = new Joint();
			j.name = name;
			j.parentName = parent == null || parent.isPointlessNode() ? null : parent.name;
			j.position = t;
			j.scale = s;
			j.rotation = r;

			if (parent == null && cfg.upAxis == DAEPostProcessConfig.DAEUpAxis.Z_UP) {
				Quaternion q = new Quaternion(j.rotation);

				j.position.rotate(q); //the other handed coordinate system has rotations before translations

				q.rotateLocalX(MathEx.HALF_PI_NEG);
				j.rotation = q.getEulerRotation();
				adjustVecToPPCfg(j.position, cfg);
			}

			list.add(j);
		}
		for (DAENode node : children) {
			list.addAll(node.toSklJoints(cfg));
		}

		return list;
	}

	public List<DAENode> getNodes() {
		List<DAENode> l = new ArrayList<>();
		l.add(this);
		for (DAENode ch : children) {
			l.addAll(ch.getNodes());
		}
		return l;
	}

	public DAENode getNodeBySID(String id) {
		if (id.equals(this.sid)) {
			return this;
		} else {
			DAENode result = null;
			for (DAENode ch : children) {
				result = ch.getNodeBySID(id);
				if (result != null) {
					break;
				}
			}
			return result;
		}
	}

	public List<Camera> getUnderlyingCameras(DAE scene, Skeleton skl, DAEPostProcessConfig ppCfg) {
		Joint bindJoint = skl.getJoint(name);

		List<Camera> l = new ArrayList<>();

		if (bindJoint != null) {
			Matrix4 absMtx = skl.getAbsoluteJointBindPoseMatrix(bindJoint);
			Vec3f posAbs = absMtx.getTranslation();
			Vec3f rotAbs = absMtx.getRotation();
			rotAbs.mul(MathEx.RADIANS_TO_DEGREES);

			for (DAEInstance instance : instances) {
				if (instance.type == DAEInstance.InstanceType.CAMERA) {
					DAECamera daeCam = scene.cameras.getByUrl(instance.url);
					if (daeCam != null && daeCam.valid()) {
						Camera cam = new Camera();
						cam.name = daeCam.name;
						daeCam.proj.setCamera(cam);

						if (lookAt != null) {
							cam.mode = Camera.Mode.LOOKAT;

							cam.translation.set(lookAt[0]);
							adjustVecToPPCfg(cam.translation, ppCfg);
							cam.lookAtTarget.set(lookAt[1]);
							adjustVecToPPCfg(cam.lookAtTarget, ppCfg);
							cam.lookAtUpVec.set(lookAt[2]);
							adjustVecToPPCfg(cam.lookAtUpVec, ppCfg);
						} else {
							cam.translation.set(posAbs);
							cam.rotation.set(rotAbs);
						}
						l.add(cam);
					}
				}
			}
		}

		for (DAENode child : children) {
			l.addAll(child.getUnderlyingCameras(scene, skl, ppCfg));
		}

		return l;
	}

	public List<Light> getUnderlyingLights(DAE scene, Skeleton skl, DAEPostProcessConfig ppCfg) {
		Joint bindJoint = skl.getJoint(name);

		List<Light> l = new ArrayList<>();

		if (bindJoint != null) {
			Matrix4 absMtx = skl.getAbsoluteJointBindPoseMatrix(bindJoint);
			Vec3f posAbs = absMtx.getTranslation();

			for (DAEInstance instance : instances) {
				if (instance.type == DAEInstance.InstanceType.LIGHT) {
					DAELight daeLight = scene.lights.getByUrl(instance.url);
					if (daeLight != null && (daeLight.type == DAELight.DAELightType.AMBIENT || daeLight.type == DAELight.DAELightType.DIRECTIONAL || daeLight.type == DAELight.DAELightType.POINT)) {
						Light light = new Light(daeLight.name);
						light.position = posAbs;
						switch (daeLight.type) {
							case AMBIENT:
								light.ambientColor = daeLight.color;
								light.diffuseColor.set(0f, 0f, 0f, 1f);
								light.specular1Color.set(0f, 0f, 0f, 1f);
								light.directional = true;
								break;
							case DIRECTIONAL:
								light.ambientColor.set(0f, 0f, 0f, 1f);
								light.diffuseColor = daeLight.color;
								light.specular1Color.set(0f, 0f, 0f, 1f);
								light.directional = true;
								break;
							case POINT:
								light.ambientColor.set(0f, 0f, 0f, 1f);
								light.diffuseColor.set(0f, 0f, 0f, 1f);
								light.specular1Color = daeLight.color;
								light.directional = false;
								break;
						}
						light.direction = calcLightDirectionByRotation(absMtx, ppCfg);
						l.add(light);
					}
				}
			}
		}

		for (DAENode child : children) {
			l.addAll(child.getUnderlyingLights(scene, skl, ppCfg));
		}

		return l;
	}

	private Vec3f calcLightDirectionByRotation(Matrix4 matrix, DAEPostProcessConfig ppCfg) {
		matrix = matrix.clone();
		matrix.clearTranslation();
		Vec3f vec = new Vec3f(0f, 0f, -1f);
		vec.mulDirection(matrix);
		vec.normalize();
		return vec;
	}

	public static void adjustVecToPPCfg(Vec3f vec, DAEPostProcessConfig pp) {
		if (pp.upAxis == DAEPostProcessConfig.DAEUpAxis.Z_UP) {
			float temp = vec.y;
			vec.y = vec.z;
			vec.z = -temp;
		}
	}

	public static final String DAE_MESH_MAT_URL_META = "DAEMeshMatURL";

	public List<Mesh> getUnderlyingMeshes(DAE scene, DAEVisualScene vs, Skeleton skl, DAEPostProcessConfig ppCfg) {
		List<Mesh> meshes = new ArrayList<>();

		Matrix4 transform = getAbsoluteTransform();

		for (DAEInstance instance : instances) {
			List<Mesh> instanceMeshes = new ArrayList<>();
			if (instance.type == null) {
				continue;
			}
			switch (instance.type) {
				case GEOMETRY:
					DAEGeometry geo = scene.geometries.getByUrl(instance.url);
					instanceMeshes.addAll(geo.getMeshes(transform, ppCfg));
					break;
				case CONTROLLER:
					DAEController ctrl = scene.controllers.getByUrl(instance.url);
					instanceMeshes.addAll(ctrl.toMeshes(skl, vs, scene.geometries, ppCfg));
					break;
			}
			//Instantiate the meshes' material names

			for (DAEBind bind : instance.binds) {
				if (bind.type == DAEBind.BindType.MATERIAL) {
					for (DAEInstance matInstance : bind.subInstances) {
						String smb = matInstance.targetSymbol;
						String smbR = matInstance.symbolReplacement;

						List<MetaDataValue> uvSetNoBinds = new ArrayList<>();

						for (DAEBind matInstanceBind : matInstance.binds) {
							if (matInstanceBind.type == DAEBind.BindType.VERTEX_INPUT) {
								switch (matInstanceBind.targetBindInputSemantic) {
									case "TEXCOORD":
										int setNo = matInstanceBind.targetBindInputSetNo;
										if (setNo == -1) {
											setNo = 0;
										}
										uvSetNoBinds.add(new MetaDataValue("UvSetName" + matInstanceBind.targetBindInputSetNo, matInstanceBind.bindName));
										break;
								}
							}
						}

						for (Mesh mesh : instanceMeshes) {
							MetaDataValue meta = mesh.metaData.getValue(DAEGeometry.SubMesh.DAE_MATSYMBOL_META);

							String metaValue;
							if (meta == null || meta.getValues().isEmpty() || meta.getValues().get(0) == null) {
								metaValue = null;
							} else {
								metaValue = meta.stringValue();
							}

							if (smb.equals(metaValue)) {
								mesh.metaData.putValue(DAE_MESH_MAT_URL_META, smbR);
							}

							mesh.metaData.putValues(uvSetNoBinds);
						}
					}
				}
			}

			meshes.addAll(instanceMeshes);
		}

		for (DAENode child : children) {
			meshes.addAll(child.getUnderlyingMeshes(scene, vs, skl, ppCfg));
		}
		return meshes;
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("node");
		e.setAttribute("id", id);
		e.setAttribute("name", name);
		e.setAttribute("type", isNode ? "NODE" : "JOINT");
		XmlFormat.setAttributeNonNull(e, "sid", sid);

		if (lookAt != null) {
			e.appendChild(XmlFormat.createSimpleTextContentElem(doc, "lookat",
				XmlFormat.getVec3(lookAt[0]) //eye
				+ " " + XmlFormat.getVec3(lookAt[1]) //target
				+ " " + XmlFormat.getVec3(lookAt[2]) //up vector
			));
		}

		if (t != null && r != null && s != null) {
			//Using transform matrices is safer with shitty tools like Blender
			if (bake) {
				Element mat = doc.createElement("matrix");
				mat.setAttribute("sid", "transform");
				Matrix4 transformMtx = new Matrix4();
				transformMtx.translate(t);
				transformMtx.rotateZYX(r);
				transformMtx.scale(s);
				mat.setTextContent(XmlFormat.getMat4(transformMtx));
				e.appendChild(mat);
			} else {
				Element translate = doc.createElement("translate");
				translate.setAttribute("sid", "location");
				translate.setTextContent(XmlFormat.getVec3(t));
				e.appendChild(translate);

				Element rz = doc.createElement("rotate");
				rz.setAttribute("sid", "rotationZ");
				rz.setTextContent("0 0 1 " + MathEx.toDegreesf(r.z));
				e.appendChild(rz);
				Element ry = doc.createElement("rotate");
				ry.setAttribute("sid", "rotationY");
				ry.setTextContent("0 1 0 " + MathEx.toDegreesf(r.y));
				e.appendChild(ry);
				Element rx = doc.createElement("rotate");
				rx.setAttribute("sid", "rotationX");
				rx.setTextContent("1 0 0 " + MathEx.toDegreesf(r.x));
				e.appendChild(rx);

				XmlFormat.setParamNodeWithSID(doc, e, "scale", XmlFormat.getVec3(s));
			}
		}

		for (DAENode child : children) {
			e.appendChild(child.createElement(doc));
		}

		for (DAEInstance inst : instances) {
			e.appendChild(inst.createElement(doc));
		}

		return e;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public String getSID() {
		return sid;
	}

	@Override
	public void setSID(String sid) {
		this.sid = sid;
	}
}
