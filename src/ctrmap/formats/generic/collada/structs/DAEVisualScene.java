package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAE;
import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.Camera;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEVisualScene implements DAEIDAble, DAESerializable {

	private String id;

	public String name;

	public List<DAENode> nodes = new ArrayList<>();

	public DAEVisualScene(Element elem) {
		id = elem.getAttribute("id");
		name = elem.getAttribute("name");

		for (Element node : XmlFormat.getElementsByTagName(elem, "node")) {
			nodes.add(new DAENode(node));
		}
	}
	
	public DAEVisualScene() {
		
	}

	public Skeleton getSkeleton(DAEPostProcessConfig cfg) {
		Skeleton skl = new Skeleton();

		for (DAENode node : nodes) {
			skl.addJoints(node.toSklJoints(cfg));
		}

		return skl;
	}

	public List<Camera> getCameras(DAE scn, Skeleton skl, DAEPostProcessConfig ppCfg) {
		List<Camera> cameras = new ArrayList<>();
		for (DAENode n : nodes) {
			cameras.addAll(n.getUnderlyingCameras(scn, skl, ppCfg));
		}
		return cameras;
	}
	
	public List<Light> getLights(DAE scn, Skeleton skl, DAEPostProcessConfig ppCfg) {
		List<Light> lights = new ArrayList<>();
		for (DAENode n : nodes) {
			lights.addAll(n.getUnderlyingLights(scn, skl, ppCfg));
		}
		return lights;
	}

	public Model getModel(DAE scn, DAEPostProcessConfig ppCfg) {
		Model m = new Model();
		m.name = name;

		Skeleton skl = getSkeleton(ppCfg);

		List<Mesh> meshes = new ArrayList<>();
		for (DAENode n : nodes) {
			meshes.addAll(n.getUnderlyingMeshes(scn, this, skl, ppCfg));
		}

		for (Mesh mesh : meshes) {
			MetaDataValue url = mesh.metaData.getValue(DAENode.DAE_MESH_MAT_URL_META);
			if (url != null) {
				DAEMaterial matBase = scn.materials.getByUrl(url.stringValue());

				Material material = new Material();
				material.faceCulling = MaterialParams.FaceCulling.BACK_FACE;
				matBase.applyToMaterial(material);
				DAEEffect eff = scn.effects.getByUrl(matBase.effInstance.url);
				eff.applyToMaterial(material, scn.images, mesh);

				for (Material existing : m.materials) {
					if (existing.name.equals(material.name)) {
						boolean isUvSetMatch = true;
						if (existing.textures.size() == material.textures.size()) {
							for (int i = 0; i < existing.textures.size(); i++) {
								if (existing.textures.get(i).uvSetNo != material.textures.get(i).uvSetNo) {
									isUvSetMatch = false;
									break;
								}
							}
						} else {
							isUvSetMatch = false;
						}
						if (isUvSetMatch) {
							material = existing;
						} else {
							String baseName = material.name;
							int num = 1;
							while (Scene.getNamedObject(material.name, m.materials) != null) {
								material.name = baseName + "_" + num;
								num++;
							}
						}
					}
				}
				mesh.materialName = material.name;
				if (material.blendOperation.alphaSrcFunc == MaterialParams.BlendFunction.SRC_ALPHA) {
					mesh.renderLayer = 1;
				}

				m.addMaterial(material);
			}
			m.addMesh(mesh);
		}
		m.skeleton = skl;
		return m;
	}

	public DAENode getNodeBySID(String sid) {
		DAENode result = null;
		for (DAENode node : nodes) {
			result = node.getNodeBySID(sid);
			if (result != null) {
				break;
			}
		}
		return result;
	}

	public List<DAENode> getAllNodes() {
		List<DAENode> l = new ArrayList<>();
		for (DAENode n : nodes) {
			l.addAll(n.getNodes());
		}
		return l;
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("visual_scene");
		e.setAttribute("id", id);
		e.setAttribute("name", name);
		
		for (DAENode node : nodes) {
			if (node.parent == null) {
				e.appendChild(node.createElement(doc));
			}
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
}
