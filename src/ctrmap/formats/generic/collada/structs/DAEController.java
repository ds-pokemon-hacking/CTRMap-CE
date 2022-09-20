package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAEConvMemory;
import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.model.Joint;
import xstandard.math.MatrixUtil;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.math.vec.Matrix4;
import xstandard.util.ArraysEx;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEController implements DAEIDAble {

	private String id;
	public String name;

	public String meshUrl;

	public Matrix4 bindShapeMatrix = new Matrix4();

	public List<String> jointNamesIntermediate;
	public List<Matrix4> invBindMatrices = new ArrayList<>();

	public boolean processedSkeleton = false;
	public List<Vertex> partialVertexBuffer = new ArrayList<>();

	public DAEController(Mesh mesh, Skeleton skel, DAEGeometry geom, DAENode meshRoot, DAEConvMemory<Joint, DAENode> nodeSIDs) {
		meshUrl = geom.getURL();
		name = mesh.name;
		jointNamesIntermediate = new ArrayList<>();
		for (int i = 0; i < skel.getJointCount(); i++) {
			String sid = nodeSIDs.findByInput(skel.getJoint(i)).getSID();
			if (sid == null) {
				throw new RuntimeException("No SID assigned !! " + sid);
			}
			jointNamesIntermediate.add(sid);
			Matrix4 invBind = skel.getAbsoluteJointBindPoseMatrix(skel.getJoint(i));
			invBind.invert();
			invBindMatrices.add(invBind);
		}
	}

	public DAEController(Element elem) {
		id = elem.getAttribute("id");
		name = elem.getAttribute("name");

		Element skin = XmlFormat.getParamElement(elem, "skin");
		//Collada spec allows exactly ONE morph or skin sub-element
		if (skin != null) {
			meshUrl = skin.getAttribute("source");

			Element bsm = XmlFormat.getParamElement(skin, "bind_shape_matrix");
			if (bsm != null) {
				bindShapeMatrix = Matrix4.createRowMajor(XmlFormat.getFloatArrayValue(bsm));
			}

			List<Element> sourceElems = XmlFormat.getElementsByTagName(skin, "source");
			DAEDict<DAESource> sources = new DAEDict<>();
			for (Element se : sourceElems) {
				sources.putNode(new DAESource(se));
			}

			Element vertexWeights = XmlFormat.getParamElement(skin, "vertex_weights");

			List<Element> weightsInputElems = XmlFormat.getElementsByTagName(vertexWeights, "input");
			DAEDict<DAEInput> weightsInputs = new DAEDict<>();
			for (Element in : weightsInputElems) {
				weightsInputs.putNode(new DAEInput(in));
			}

			//Mandatory input by the spec
			DAEInput jIn = weightsInputs.get("JOINT");
			DAESource jointsSource = sources.getByUrl(jIn.sourceUrl);
			//This is actually the SIDs of the joints, not their names
			jointNamesIntermediate = jointsSource.accessor.getStringList();

			float[] weightBuffer = null;
			int weightOffs = -1;
			int jntOffs = jIn.offset;

			int stride = 0;

			for (DAEInput in : weightsInputs) {
				switch (in.semantic) {
					case "WEIGHT":
						weightBuffer = sources.getByUrl(in.sourceUrl).accessor.getFloatArray();
						weightOffs = in.offset;
						break;
				}
				stride = Math.max(in.offset, stride);
			}
			stride++;

			int[] vCount = XmlFormat.getIntArrayValue(XmlFormat.getParamElement(vertexWeights, "vcount"));
			int[] v = XmlFormat.getIntArrayValue(XmlFormat.getParamElement(vertexWeights, "v"));

			int vOffset = 0;
			for (int i = 0; i < vCount.length; i++) {
				Vertex vtx = new Vertex();
				for (int j = 0; j < vCount[i]; j++) {
					for (int att = 0; att < stride; att++) {
						if (att == jntOffs) {
							int bidx = v[vOffset];
							if (bidx >= jointNamesIntermediate.size()) {
								throw new ArrayIndexOutOfBoundsException("Bone index " + bidx + " out of bounds, stride " + stride + " joffs " + jntOffs + " woffs " + weightOffs);
							}
							vtx.boneIndices.add(bidx);
						}
						if (att == weightOffs) {
							vtx.weights.add(weightBuffer[v[vOffset]]);
						}
						if (weightBuffer == null) {
							vtx.weights.add(1f);
						}
						vOffset++;
					}
				}
				partialVertexBuffer.add(vtx);
			}
		}
	}

	public Element createElement(Document doc, List<DAEGeometry.DAEVertex> inListVerts) {
		Element elem = doc.createElement("controller");
		elem.setAttribute("id", id);
		elem.setAttribute("name", name);

		Element skin = doc.createElement("skin");
		skin.setAttribute("source", meshUrl);

		skin.appendChild(XmlFormat.createSimpleTextContentElem(doc, "bind_shape_matrix", XmlFormat.getMat4(bindShapeMatrix)));

		List<String> compactJointNames = new ArrayList<>();
		for (DAEGeometry.DAEVertex v : inListVerts) {
			for (int i = 0; i < v.activeWeightCount; i++) {
				String jn = jointNamesIntermediate.get(v.boneIndices.get(i));
				if (!compactJointNames.contains(jn)) {
					compactJointNames.add(jn);
				}
			}
		}

		Map<Integer, Integer> jnToCompact = new HashMap<>();
		Map<Integer, Integer> jnFromCompact = new HashMap<>();
		for (String jn : compactJointNames) {
			int srcIdx = jointNamesIntermediate.indexOf(jn);
			int dstIdx = compactJointNames.indexOf(jn);
			jnToCompact.put(srcIdx, dstIdx);
			jnFromCompact.put(dstIdx, srcIdx);
		}

		Matrix4[] compactInvBind = new Matrix4[compactJointNames.size()];

		for (int i = 0; i < compactJointNames.size(); i++) {
			int idx = jnFromCompact.get(i);
			if (idx == -1) {
				compactInvBind[i] = new Matrix4();
			} else {
				compactInvBind[i] = invBindMatrices.get(idx);
			}
		}

		DAESource jointsSource = new DAESource(compactJointNames.toArray(new String[compactJointNames.size()]), "JOINT");
		jointsSource.setID(XmlFormat.makeSafeId(id + "-joints"));
		skin.appendChild(jointsSource.createElement(doc));

		DAESource invBindSource = new DAESource(compactInvBind, "TRANSFORM");
		invBindSource.setID(XmlFormat.makeSafeId(id + "-inv-bind-mtx"));
		skin.appendChild(invBindSource.createElement(doc));

		FloatList weights = new FloatList();
		IntList vcount = new IntList();
		IntList v = new IntList();

		float weightSum;

		for (DAEGeometry.DAEVertex vtx : inListVerts) {
			weightSum = 0f;
			vcount.add(vtx.activeWeightCount);
			for (int i = 0; i < vtx.activeWeightCount; i++) {
				int bidx = jnToCompact.get(vtx.boneIndices.get(i));
				float weight;
				if (i < vtx.boneWeights.size()) {
					weight = vtx.boneWeights.get(i);
				} else {
					weight = (1f - weightSum) / (vtx.activeWeightCount - vtx.boneWeights.size());
				}
				v.add(bidx);
				int widx = weights.indexOf(weight);
				if (widx == -1) {
					widx = weights.size();
					weights.add(weight);
				}
				v.add(widx);
				weightSum += weight;
			}
		}

		DAESource weightsSrc = new DAESource(weights.toArray(), "WEIGHT");
		weightsSrc.setID(XmlFormat.makeSafeId(id + "-weights"));
		skin.appendChild(weightsSrc.createElement(doc));

		Element joints = doc.createElement("joints");
		DAEInput jointsInput = new DAEInput("JOINT", jointsSource, -1);
		DAEInput invBindInput = new DAEInput("INV_BIND_MATRIX", invBindSource, -1);
		joints.appendChild(jointsInput.createElement(doc));
		joints.appendChild(invBindInput.createElement(doc));
		skin.appendChild(joints);

		DAEInput jntInput = new DAEInput("JOINT", jointsSource, 0);
		DAEInput wgtInput = new DAEInput("WEIGHT", weightsSrc, 1);

		Element vv = doc.createElement("vertex_weights");
		vv.setAttribute("count", String.valueOf(vcount.size()));

		vv.appendChild(jntInput.createElement(doc));
		vv.appendChild(wgtInput.createElement(doc));

		vv.appendChild(XmlFormat.createSimpleTextContentElem(doc, "vcount", XmlFormat.getIntList(vcount)));
		vv.appendChild(XmlFormat.createSimpleTextContentElem(doc, "v", XmlFormat.getIntList(v)));

		skin.appendChild(vv);

		elem.appendChild(skin);

		return elem;
	}

	public List<Mesh> toMeshes(Skeleton skl, DAEVisualScene visualScene, DAEDict<DAEGeometry> geometries, DAEPostProcessConfig ppCfg) {
		if (!processedSkeleton) {
			Map<Integer, Integer> jointIdxLUT = new HashMap<>();
			for (int i = 0; i < jointNamesIntermediate.size(); i++) {
				DAENode node = visualScene.getNodeBySID(jointNamesIntermediate.get(i));
				int index = 0; //if it's -1 it would crash the renderer
				if (node != null) {
					index = skl.getJointIndex(node.name);
					if (index == -1) {
						index = 0;
					}
				}
				jointIdxLUT.put(i, index);
			}

			for (Vertex vtx : partialVertexBuffer) {
				for (int i = 0; i < vtx.boneIndices.size(); i++) {
					Integer index = jointIdxLUT.get(vtx.boneIndices.get(i));
					if (index == null) {
						throw new NullPointerException("Bone index can not be null. - controller id " + id + " name " + name + " desired index " + vtx.boneIndices.get(i) + " joint count " + jointNamesIntermediate.size());
					}
					vtx.boneIndices.set(i, index);
				}
			}

			processedSkeleton = true;
		}

		Matrix4 mtx = bindShapeMatrix;

		List<Mesh> baseMeshes = geometries.getByUrl(meshUrl).getMeshes(mtx, ppCfg, partialVertexBuffer, true);

		return baseMeshes;
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
