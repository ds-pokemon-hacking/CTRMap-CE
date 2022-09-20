package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAE;
import ctrmap.formats.generic.collada.DAEConvMemory;
import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.model.Face;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.math.MatrixUtil;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import ctrmap.renderer.scene.texturing.Material;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec4f;
import xstandard.text.FormattingUtils;
import xstandard.util.ArraysEx;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.joml.Matrix3f;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEGeometry implements DAEIDAble {

	private String id;

	public String name;

	public List<SubMesh> faces = new ArrayList<>();

	public DAEGeometry(Element geomNode) {
		id = geomNode.getAttribute("id");
		name = geomNode.getAttribute("name");

		List<Element> sourceElems = XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "source");
		List<Element> vertices = XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "vertices");
		List<Element> polygons = XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "polygons");
		polygons.addAll(XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "triangles"));
		polygons.addAll(XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "lines"));
		polygons.addAll(XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "linestrips"));
		polygons.addAll(XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "tristrips"));
		polygons.addAll(XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "trifans"));
		polygons.addAll(XmlFormat.getLibraryContentDataElems(geomNode, "mesh", "polylist"));

		Map<String, List<Vertex>> vertexSets = new HashMap<>();

		DAEDict<DAESource> sources = new DAEDict<>();

		for (Element src : sourceElems) {
			sources.putNode(new DAESource(src));
		}
		
		boolean globalHasNormal = false;
		boolean globalHasColor = false;
		boolean[] globalHasUv = new boolean[3];

		for (Element verticesEntry : vertices) {
			List<Element> vertexInputs = XmlFormat.getElementsByTagName(verticesEntry, "input");
			
			List<Vertex> data = new ArrayList<>();
			
			for (Element input : vertexInputs) {
				if (input.getAttribute("semantic").equals("POSITION")) {
					DAEInput in = new DAEInput(input);
					DAESource src = sources.getByUrl(in.sourceUrl);
					
					List<Vec3f> positions = src.accessor.getVec3fArray();
					for (Vec3f pos : positions) {
						Vertex v = new Vertex();
						v.position = pos;
						data.add(v);
					}
					break;
				}
			}

			//vertices can only have positions, rest is indexed
			for (Element input : vertexInputs) {
				DAEInput in = new DAEInput(input);
				DAEAccessor srcAccessor = sources.getByUrl(in.sourceUrl).accessor;
				switch (in.semantic) {
					case "COLOR":
						List<RGBA> colors = srcAccessor.getRGBAArray();
						for (int i = 0; i < data.size(); i++) {
							data.get(i).color = colors.get(i);
						}
						globalHasColor = true;
						break;
					case "NORMAL":
						List<Vec3f> normals = srcAccessor.getVec3fArray();
						for (int i = 0; i < data.size(); i++) {
							data.get(i).normal = normals.get(i);
						}
						globalHasNormal = true;
						break;
					case "TEXCOORD":
						if (in.setNo < 3) {
							globalHasUv[in.setNo] = true;
							Vec2f[] uvs = srcAccessor.getVec2fArray();
							for (int i = 0; i < data.size(); i++) {
								data.get(i).uv[in.setNo] = uvs[i];
							}
						}
						break;
				}
			}
			vertexSets.put(verticesEntry.getAttribute("id"), data);
		}

		int subMeshIdx = 0;
		for (Element polygonArr : polygons) {
			PrimitiveType pt = null;
			switch (polygonArr.getTagName()) {
				case "triangles":
					pt = PrimitiveType.TRIS;
					break;
				case "tristrips":
					pt = PrimitiveType.TRISTRIPS;
					break;
				case "trifans":
					pt = PrimitiveType.TRIFANS;
					break;
				case "lines":
					pt = PrimitiveType.LINES;
					break;
				case "linestrips":
					pt = PrimitiveType.LINESTRIPS;
					break;
			}

			List<Element> inputs = XmlFormat.getElementsByTagName(polygonArr, "input");

			DAEDict<DAEInput> inputList = new DAEDict<>();

			for (Element input : inputs) {
				inputList.putNode(new DAEInput(input));
			}

			SubMesh subMesh = new SubMesh();
			int ptSepSize = -1;
			if (pt != null) {
				ptSepSize = PrimitiveType.getPrimitiveTypeSeparationSize(pt, -1);
				subMesh.forcePrimitiveHeader = new PrimitiveHeader(pt, ptSepSize);
			}
			subMesh.hasColor = globalHasColor;
			subMesh.hasNormal = globalHasNormal;
			subMesh.hasUV = globalHasUv.clone();

			int numFaces = XmlFormat.getIntAttribute(polygonArr, "count");

			Element vcount = XmlFormat.getParamElement(polygonArr, "vcount");
			List<Element> polys = XmlFormat.getElementsByTagName(polygonArr, "p");

			subMesh.faces = new DAEFace[numFaces];

			List<Vertex> vertexSrc = new ArrayList<>();
			List<Vec3f> normalSrc = new ArrayList<>();
			Map<Integer, List<Vec2f>> uvSrc = new HashMap<>();
			List<RGBA> colorSrc = new ArrayList<>();

			int elemsPerFacepoint = 0;

			for (DAEInput in : inputList) {
				switch (in.semantic) {
					case "VERTEX":
						vertexSrc = vertexSets.get(DAE.idFromUrl(in.sourceUrl));
						subMesh.vertexPointerOffset = in.offset;
						break;
					case "COLOR":
						subMesh.hasColor = true;
						colorSrc = sources.getByUrl(in.sourceUrl).accessor.getRGBAArray();
						subMesh.colorPointerOffset = in.offset;
						break;
					case "NORMAL":
						subMesh.hasNormal = true;
						normalSrc = sources.getByUrl(in.sourceUrl).accessor.getVec3fArray();
						subMesh.normalPointerOffset = in.offset;
						break;
					case "TEXCOORD":
						if (in.setNo < 3) {
							subMesh.hasUV[in.setNo] = true;
						}
						uvSrc.put(in.setNo, sources.getByUrl(in.sourceUrl).accessor.getVec2fList());
						break;
				}
				elemsPerFacepoint = Math.max(in.offset + 1, elemsPerFacepoint);
			}

			int faceIdx = 0;
			for (Element poly : polys) {
				List<String> indexPoints = ArraysEx.asList(poly.getTextContent().split("\\s+"));
				for (int i = 0; i < indexPoints.size(); i++) {
					if (indexPoints.get(i).trim().isEmpty()) {
						indexPoints.remove(i);
					}
				}

				if (pt != null) {
					int nFacepoints = indexPoints.size() / elemsPerFacepoint;
					int faceStride = PrimitiveType.getPrimitiveTypeSeparationSize(pt, -1);
					if (faceStride == -1) {
						faceStride = nFacepoints;
					}
					int faceStrideRaw = faceStride * elemsPerFacepoint;
					int facesInP = nFacepoints / faceStride;

					for (int i = 0; i < facesInP; i++) {
						DAEFace f = new DAEFace(faceStrideRaw);

						int foff = faceStrideRaw * i;

						for (int inOff = foff, outOff = 0; outOff < faceStrideRaw; outOff++, inOff++) {
							f.vertexPoints[outOff] = Integer.parseInt(indexPoints.get(inOff));
						}

						subMesh.faces[faceIdx] = f;
						faceIdx++;
					}
				} else if (vcount != null) {
					int[] vcounts = XmlFormat.getIntArrayValue(vcount);
					int inOff = 0;
					for (int vc : vcounts) {
						DAEFace f = new DAEFace(vc * elemsPerFacepoint);

						for (int outOff = 0; outOff < f.vertexPoints.length; outOff++, inOff++) {
							f.vertexPoints[outOff] = Integer.parseInt(indexPoints.get(inOff));
						}

						subMesh.faces[faceIdx] = f;
						faceIdx++;
					}
				} else {
					DAEFace f = new DAEFace(indexPoints.size());

					for (int i = 0; i < indexPoints.size(); i++) {
						f.vertexPoints[i] = Integer.parseInt(indexPoints.get(i));
					}

					subMesh.faces[faceIdx] = f;
					faceIdx++;
				}
			}

			subMesh.createVertexBuffer(vertexSrc, colorSrc, normalSrc, uvSrc, inputList);

			subMesh.materialSymbol = polygonArr.getAttribute("material");
			subMesh.index = subMeshIdx;
			faces.add(subMesh);
			subMeshIdx++;
		}
	}

	public List<Mesh> getMeshes(Matrix4 worldTransformMatrix, DAEPostProcessConfig cfg) {
		return getMeshes(worldTransformMatrix, cfg, null, false);
	}

	public List<Mesh> getMeshes(Matrix4 worldTransformMatrix, DAEPostProcessConfig cfg, List<Vertex> skinBuffer, boolean isController) {
		List<Mesh> meshes = new ArrayList<>();
		worldTransformMatrix = new Matrix4(worldTransformMatrix);
		if (cfg.upAxis == DAEPostProcessConfig.DAEUpAxis.Z_UP && (!cfg.isShitBlender || !isController)) {
			worldTransformMatrix.rotateLocalX(MathEx.HALF_PI_NEG);
		}
		Matrix3f normalMatrix = new Matrix3f();
		worldTransformMatrix.clone().normalize3x3().normal(normalMatrix);

		int idx = 0;
		for (SubMesh f : faces) {
			if (skinBuffer != null) {
				f.applySkinning(skinBuffer);
			}
			List<Mesh> faceToMeshes = f.toMeshes();
			for (Mesh m : faceToMeshes) {
				if (skinBuffer != null) {
					m.hasBoneIndices = true;
					m.hasBoneWeights = true;
				}
				m.name = (idx == 0 ? name : name + "_" + idx) + ((faceToMeshes.size() == 1) ? "" : "_" + FormattingUtils.getFriendlyEnum(m.primitiveType));

				for (Vertex vtx : m.vertices) {
					vtx.position.mulPosition(worldTransformMatrix);
					if (vtx.normal != null) {
						vtx.normal = vtx.normal.clone();
						vtx.normal.mul(normalMatrix);
					}
				}
				meshes.add(m);
				idx++;
			}
		}

		return meshes;
	}

	public static boolean isPrimitiveTypeCOLLADACompatible(PrimitiveType type) {
		switch (type) {
			case LINES:
			case QUADS:
			case TRIS:
			case TRISTRIPS:
			case TRIFANS:
				return true;
		}
		return false;
	}

	public DAEGeometry(Mesh mesh) {
		if (!isPrimitiveTypeCOLLADACompatible(mesh.primitiveType)) {
			throw new RuntimeException("Primitive type " + mesh.primitiveType + " not supported by COLLADA!!");
		}
		name = XmlFormat.sanitizeName(mesh.name);

		SubMesh submesh = new SubMesh();
		submesh.hasColor = mesh.hasColor;
		submesh.hasUV = mesh.hasUV;
		submesh.hasNormal = mesh.hasNormal;

		int pOffs = 0;

		submesh.vertexPointerOffset = pOffs++;
		submesh.normalPointerOffset = mesh.hasNormal ? pOffs++ : -1;
		submesh.colorPointerOffset = mesh.hasColor ? pOffs++ : -1;
		for (int i = 0; i < mesh.hasUV.length; i++) {
			if (mesh.hasUV(i)) {
				submesh.uvOffsets.add(pOffs++);
			} else {
				break;
			}
		}

		submesh.materialSymbol = mesh.materialName;

		VertexArrayList deindexed = new VertexArrayList();
		for (Vertex vtx : mesh) {
			deindexed.add(vtx);
		}

		submesh.vertexBuffers.put(new PrimitiveHeader(mesh), deindexed);

		faces.add(submesh);
	}

	public Element createElement(Document doc, List<DAEVertex> outListVertices) {
		Element elem = doc.createElement("geometry");
		elem.setAttribute("id", id);
		elem.setAttribute("name", name);

		Element mesh = doc.createElement("mesh");

		for (SubMesh sm : faces) {
			HashSet<DAEVertex> vertices = new HashSet<>();
			HashSet<Vec3f> normals = new HashSet<>();
			HashSet<RGBA> colors = new HashSet<>();
			List<HashSet<Vec2f>> uvs = new ArrayList<>();
			for (int i = 0; i < sm.uvOffsets.size(); i++) {
				uvs.add(new HashSet<>());
			}

			DAEVertex vTemp = new DAEVertex();

			for (Map.Entry<PrimitiveHeader, VertexArrayList> vertList : sm.vertexBuffers.entrySet()) {
				for (Vertex vtx : vertList.getValue()) {
					vTemp.set(vtx);
					if (!vertices.contains(vTemp)) {
						vertices.add(vTemp);
						vTemp = new DAEVertex();
					}
					if (sm.hasNormal) {
						Vec3f nor = vtx.normal;
						if (nor == null) {
							nor = new Vec3f();
						}
						normals.add(nor);
					}
					if (sm.hasColor && !colors.contains(vtx.color)) {
						colors.add(vtx.color);
					}
					for (int i = 0; i < sm.hasUV.length; i++) {
						if (sm.hasUV[i]) {
							HashSet<Vec2f> uvSet = uvs.get(i);
							if (!uvSet.contains(vtx.uv[i])) {
								uvSet.add(vtx.uv[i]);
							}
						}
					}
				}
			}

			List<Vec3f> positionsList = new ArrayList<>();
			List<Vec3f> normalsList = new ArrayList<>(normals);
			List<RGBA> colorsList = new ArrayList<>(colors);
			List<List<Vec2f>> uvsList = new ArrayList<>();
			for (HashSet<Vec2f> uv : uvs) {
				uvsList.add(new ArrayList<>(uv));
			}

			for (DAEVertex v : vertices) {
				positionsList.add(v.position);
				if (outListVertices != null) {
					outListVertices.add(v);
				}
			}

			List<DAEInput> inputs = new ArrayList<>();

			DAESource srcPositions = new DAESource(positionsList.toArray(new Vec3f[positionsList.size()]), "X", "Y", "Z");
			srcPositions.setID(XmlFormat.makeSafeId(id + "-positions"));
			mesh.appendChild(srcPositions.createElement(doc));

			String vertsId = XmlFormat.makeSafeId(id + "-vertices");

			Element verticesElem = doc.createElement("vertices");
			verticesElem.setAttribute("id", vertsId);
			verticesElem.appendChild(new DAEInput("POSITION", srcPositions, -1).createElement(doc));
			mesh.appendChild(verticesElem);

			inputs.add(new DAEInput("VERTEX", vertsId, sm.vertexPointerOffset));

			if (!normalsList.isEmpty()) {
				DAESource srcNormals = new DAESource(normals.toArray(new Vec3f[normals.size()]), "X", "Y", "Z");
				srcNormals.setID(XmlFormat.makeSafeId(id + "-normals"));
				mesh.appendChild(srcNormals.createElement(doc));

				inputs.add(new DAEInput("NORMAL", srcNormals, sm.normalPointerOffset));
			}

			if (!colorsList.isEmpty()) {
				Vec4f[] colorsF = new Vec4f[colorsList.size()];
				for (int i = 0; i < colorsList.size(); i++) {
					colorsF[i] = colorsList.get(i).toVector4();
				}

				DAESource srcColors = new DAESource(colorsF, "R", "G", "B", "A");
				srcColors.setID(XmlFormat.makeSafeId(id + "-colors"));
				mesh.appendChild(srcColors.createElement(doc));

				inputs.add(new DAEInput("COLOR", srcColors, sm.colorPointerOffset));
			}

			for (int uvSetNo = 0; uvSetNo < uvsList.size(); uvSetNo++) {
				List<Vec2f> uv = uvsList.get(uvSetNo);
				if (!uv.isEmpty()) {
					DAESource srcUV = new DAESource(uv.toArray(new Vec2f[uv.size()]), "S", "T");
					srcUV.setID(XmlFormat.makeSafeId(id + "-uv-" + uvSetNo));
					mesh.appendChild(srcUV.createElement(doc));

					inputs.add(new DAEInput("TEXCOORD", srcUV, sm.uvOffsets.get(uvSetNo), uvSetNo));
				}
			}

			for (Map.Entry<PrimitiveHeader, VertexArrayList> vbuf : sm.vertexBuffers.entrySet()) {
				VertexArrayList verts = vbuf.getValue();

				int stride = sm.getFacepointStride();
				int[] indices = new int[stride * verts.size()];
				//System.out.println("vertex count " + verts.size() + " vertex stride " + stride);
				//System.out.println("has color " + sm.hasColor + " has normal " + sm.hasNormal + " has uv " + Arrays.toString(sm.hasUV));

				int indicesOffset = 0;
				for (Vertex vtx : verts) {
					indices[indicesOffset + sm.vertexPointerOffset] = positionsList.indexOf(vtx.position);
					if (sm.hasNormal) {
						indices[indicesOffset + sm.normalPointerOffset] = normalsList.indexOf(vtx.normal);
					}
					if (sm.hasColor) {
						indices[indicesOffset + sm.colorPointerOffset] = colorsList.indexOf(vtx.color);
					}
					for (int i = 0; i < sm.hasUV.length; i++) {
						if (sm.hasUV[i]) {
							indices[indicesOffset + sm.uvOffsets.get(i)] = uvsList.get(i).indexOf(vtx.uv[i]);
						}
					}
					indicesOffset += stride;
				}

				Element vertsElem = null;
				PrimitiveHeader ph = vbuf.getKey();

				int faceCountPerPrimitive = ph.facepointsPerFace;

				int polyCount = verts.size() / faceCountPerPrimitive;

				if (ph.primitiveType != null) {
					switch (ph.primitiveType) {
						case LINES:
						case LINESTRIPS:
						case TRIFANS:
						case TRISTRIPS:
						case TRIS: {
							//triangles
							vertsElem = doc.createElement(getPrimitiveTypeElementName(ph.primitiveType));

							StringBuilder indicesSB = new StringBuilder();

							for (int i = 0; i < indices.length; i++) {
								if (i != 0) {
									indicesSB.append(" ");
								}
								indicesSB.append(indices[i]);
							}

							vertsElem.appendChild(XmlFormat.createSimpleTextContentElem(doc, "p", indicesSB.toString()));

							break;
						}
						case QUADS: {
							//polygons - quads
							vertsElem = doc.createElement("polygons");

							int polyStride = stride * faceCountPerPrimitive;
							//System.out.println("poly count " + polyCount + " poly stride " + polyStride + " indices count " + indices.length);

							for (int polyIndex = 0, indicesIndex = 0; polyIndex < polyCount; polyIndex++, indicesIndex += polyStride) {
								StringBuilder indicesSB = new StringBuilder();

								for (int i = 0, j = indicesIndex; i < polyStride; i++, j++) {
									if (i != 0) {
										indicesSB.append(" ");
									}
									indicesSB.append(indices[j]);
								}
								vertsElem.appendChild(XmlFormat.createSimpleTextContentElem(doc, "p", indicesSB.toString()));
							}

							break;
						}
					}
				}

				if (vertsElem != null) {
					vertsElem.setAttribute("count", String.valueOf(polyCount));
					XmlFormat.setAttributeNonNull(vertsElem, "material", sm.materialSymbol);

					Collections.reverse(inputs);

					for (DAEInput in : inputs) {
						vertsElem.insertBefore(in.createElement(doc), vertsElem.getFirstChild());
					}

					mesh.appendChild(vertsElem);
				}
			}
		}

		elem.appendChild(mesh);
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

	private static String getPrimitiveTypeElementName(PrimitiveType primitiveType) {
		switch (primitiveType) {
			case LINES:
				return "lines";
			case LINESTRIPS:
				return "linestrips";
			case TRIFANS:
				return "trifans";
			case TRISTRIPS:
				return "tristrips";
			case TRIS:
				return "triangles";
		}
		throw new RuntimeException();
	}

	public static class DAEFace {

		public int[] vertexPoints;
		//vertex points
		//
		// attribute 1 (position)
		//		v0
		//		v1
		//		v2
		// attribute 2 (normal usually)
		//		v0
		//		v1
		//		v2
		// and so on			

		public DAEFace(int vpCount) {
			vertexPoints = new int[vpCount];
		}
	}

	public static class SubMesh {

		public static final String DAE_MATSYMBOL_META = "DAEMaterialSymbol";

		public int vertexPointerOffset;
		public int normalPointerOffset;
		public int colorPointerOffset;
		public List<Integer> uvOffsets = new ArrayList<>();

		public DAEFace[] faces;
		public PrimitiveHeader forcePrimitiveHeader = null;

		public int index;

		public Map<PrimitiveHeader, VertexArrayList> vertexBuffers = new HashMap<>();
		public Map<PrimitiveHeader, List<Integer>> daeIndexBuffers = new HashMap<>();

		public String materialSymbol;

		public boolean hasColor;
		public boolean[] hasUV = new boolean[3];
		public boolean hasNormal;

		public void createVertexBuffer(
			List<Vertex> vertexSrc,
			List<RGBA> colorSrc,
			List<Vec3f> normalSrc,
			Map<Integer, List<Vec2f>> uvSrc,
			DAEDict<DAEInput> inputList
		) {
			List<List<Vec2f>> uvBuffers = new ArrayList<>();

			for (Map.Entry<Integer, List<Vec2f>> e : uvSrc.entrySet()) {
				while (uvBuffers.size() <= e.getKey()) {
					uvBuffers.add(null);
				}
				uvBuffers.set(e.getKey(), e.getValue());
			}
			for (DAEInput in : inputList) {
				if (in.semantic.equals("TEXCOORD")) {
					while (uvOffsets.size() <= in.setNo) {
						uvOffsets.add(null);
					}
					uvOffsets.set(in.setNo, in.offset);
				}
			}

			int offsetMax = Math.max(vertexPointerOffset, Math.max(normalPointerOffset, colorPointerOffset));
			for (Integer uvo : uvOffsets) {
				if (uvo != null) {
					offsetMax = Math.max(uvo, offsetMax);
				}
			}
			offsetMax++;

			for (DAEFace face : faces) {
				PrimitiveHeader primitiveHeader = forcePrimitiveHeader;
				if (primitiveHeader == null) {
					primitiveHeader = new PrimitiveHeader();
					primitiveHeader.facepointsPerFace = face.vertexPoints.length / offsetMax;
					primitiveHeader.primitiveType = PrimitiveType.forFacepointCount(primitiveHeader.facepointsPerFace);
				} else if (primitiveHeader.facepointsPerFace == -1) {
					PrimitiveType pt = primitiveHeader.primitiveType;
					primitiveHeader = new PrimitiveHeader();
					primitiveHeader.facepointsPerFace = face.vertexPoints.length / offsetMax;
					primitiveHeader.primitiveType = pt;
				}

				VertexArrayList vl = vertexBuffers.get(primitiveHeader);
				if (vl == null) {
					vl = new VertexArrayList();
					vertexBuffers.put(primitiveHeader, vl);
				}
				List<Integer> positionIBO = daeIndexBuffers.get(primitiveHeader);
				if (positionIBO == null) {
					positionIBO = new ArrayList<>();
					daeIndexBuffers.put(primitiveHeader, positionIBO);
				}

				for (int facepointIdx = 0; facepointIdx < primitiveHeader.facepointsPerFace; facepointIdx++) {
					int[] vp = face.vertexPoints;

					Vertex v = new Vertex();

					int vo = vp[facepointIdx * offsetMax + vertexPointerOffset];
					positionIBO.add(vo);
					Vertex src = vertexSrc.get(vo);
					v.position = new Vec3f(src.position);

					if (!normalSrc.isEmpty()) {
						v.normal = normalSrc.get(vp[facepointIdx * offsetMax + normalPointerOffset]);
					}
					else {
						v.normal = src.normal;
					}
					if (!colorSrc.isEmpty()) {
						v.color = colorSrc.get(vp[facepointIdx * offsetMax + colorPointerOffset]);
					}
					else {
						v.color = src.color;
					}
					
					for (int i = 0; i < v.uv.length; i++) { 
						if (i < uvBuffers.size()) {
							if (uvOffsets.get(i) != null) {
								List<Vec2f> uvb = uvBuffers.get(i);
								int off = uvOffsets.get(i);
								if (uvb != null) {
									v.uv[i] = new Vec2f(uvb.get(vp[facepointIdx * offsetMax + off]));
								}
							}
						}
						else {
							v.uv[i] = src.uv[i];
						}
					}
					vl.add(v);
				}
			}
		}

		private List<Mesh> toMeshes() {
			List<Mesh> meshes = new ArrayList<>();
			for (Map.Entry<PrimitiveHeader, VertexArrayList> vbo : vertexBuffers.entrySet()) {
				PrimitiveHeader hdr = vbo.getKey();

				if (hdr.primitiveType != null) {
					Mesh m = new Mesh();

					m.vertices = vbo.getValue();
					m.metaData.putValue(DAE_MATSYMBOL_META, materialSymbol);
					m.hasColor = hasColor;
					m.hasNormal = hasNormal;
					m.hasUV = hasUV;
					m.primitiveType = hdr.primitiveType;

					meshes.add(m);
				}
			}
			return meshes;
		}

		public void applySkinning(List<Vertex> skinVertexBuf) {
			for (Map.Entry<PrimitiveHeader, List<Integer>> e : daeIndexBuffers.entrySet()) {
				List<Integer> l = e.getValue();
				VertexArrayList vbo = vertexBuffers.get(e.getKey());
				for (int i = 0; i < l.size(); i++) {
					Vertex skinVertex = skinVertexBuf.get(l.get(i));
					vbo.get(i).boneIndices.addAll(skinVertex.boneIndices);
					vbo.get(i).weights.addAll(skinVertex.weights);
				}
			}
		}

		private int getFacepointStride() {
			int max = Math.max(vertexPointerOffset, normalPointerOffset);
			max = Math.max(max, colorPointerOffset);
			for (int uv : uvOffsets) {
				max = Math.max(max, uv);
			}
			return max + 1;
		}
	}

	private static class PrimitiveHeader {

		public PrimitiveType primitiveType;
		public int facepointsPerFace;

		public PrimitiveHeader() {

		}

		public PrimitiveHeader(Mesh mesh) {
			primitiveType = mesh.primitiveType;
			facepointsPerFace = PrimitiveType.getPrimitiveTypeSeparationSize(primitiveType, mesh);
		}

		public PrimitiveHeader(PrimitiveType t, int fppf) {
			this.primitiveType = t;
			this.facepointsPerFace = fppf;
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof PrimitiveHeader) {
				PrimitiveHeader vs = (PrimitiveHeader) o;
				if (primitiveType == PrimitiveType.TRIFANS || primitiveType == PrimitiveType.TRISTRIPS || primitiveType == PrimitiveType.LINESTRIPS) {
					return vs == this;
				}
				return vs.primitiveType == primitiveType && vs.facepointsPerFace == facepointsPerFace;
			}
			return false;
		}

		@Override
		public int hashCode() {
			if (primitiveType == PrimitiveType.TRIFANS || primitiveType == PrimitiveType.TRISTRIPS || primitiveType == PrimitiveType.LINESTRIPS) {
				return System.identityHashCode(this);
			}
			int hash = 5;
			hash = 59 * hash + Objects.hashCode(this.primitiveType);
			hash = 59 * hash + this.facepointsPerFace;
			return hash;
		}
	}

	public static class DAEVertex {

		public Vec3f position;
		
		public int activeWeightCount;
		public IntList boneIndices;
		public FloatList boneWeights;

		public DAEVertex() {

		}

		public void set(Vertex vtx) {
			activeWeightCount = vtx.getActiveWeightCount();
			position = vtx.position;
			boneIndices = vtx.boneIndices;
			boneWeights = vtx.weights;
		}

		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof DAEVertex) {
				DAEVertex v = (DAEVertex) o;
				return v.position.equals(position) && boneIndices.equals(v.boneIndices) && boneWeights.equals(v.boneWeights);
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 37 * hash + Objects.hashCode(this.position);
			hash = 37 * hash + Objects.hashCode(this.boneIndices);
			hash = 37 * hash + Objects.hashCode(this.boneWeights);
			return hash;
		}
	}
}
