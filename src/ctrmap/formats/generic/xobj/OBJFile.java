package ctrmap.formats.generic.xobj;

import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.vtxlist.AbstractVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.MaterialProcessor;
import ctrmap.renderer.util.MeshProcessor;
import ctrmap.renderer.util.PrimitiveConverter;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.text.StringEx;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OBJFile {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Wavefront OBJ", "*.obj");

	public String name;

	public MTLFile mtllib;

	public List<Mesh> meshes = new ArrayList<>();
	public List<Texture> textures = new ArrayList<>();

	public OBJFile(File f) {
		this(new DiskFile(f));
	}

	public OBJFile(FSFile f) {
		name = f.getName();

		FSFile parentDir = f.getParent();

		Scanner scanner = new Scanner(f.getNativeInputStream());

		List<Vec3f> posBuffer = new ArrayList<>();
		List<Vec3f> normalBuffer = new ArrayList<>();
		List<Vec2f> uvBuffer = new ArrayList<>();
		List<Vec4f> colorBuffer = new ArrayList<>();

		Mesh currentMesh = null;
		String currentMatName = null;
		String currentMeshName = null;

		boolean isVColStupidFormat = false;

		VertexArrayList triVertices = new VertexArrayList();
		VertexArrayList quadVertices = new VertexArrayList();

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String[] cmds = StringEx.splitOnecharFastNoBlank(line, ' ');
			if (cmds.length == 0) {
				continue;
			}

			switch (cmds[0]) {
				case "mtllib":
					mtllib = new MTLFile(getRelativeOrAbsoluteFile(StringEx.join(' ', 1, cmds.length, cmds), parentDir));
					break;
				case "o":
				case "g":
					if (currentMesh != null) {
						finishMesh(meshes, currentMesh, triVertices, quadVertices);
					}
					currentMesh = new Mesh();
					currentMesh.name = cmds[1];
					currentMeshName = currentMesh.name;
					break;
				case "v":
					posBuffer.add(new Vec3f(readFloatArray(cmds)));
					break;
				case "vn":
					normalBuffer.add(new Vec3f(readFloatArray(cmds)));
					break;
				case "vt":
					uvBuffer.add(new Vec2f(readFloatArray(cmds)));
					break;
				case "c":
				case "vc":
					colorBuffer.add(new Vec4f(readFloatArray(cmds)));
					if (!isVColStupidFormat) {
						isVColStupidFormat = cmds[0].equals("c");
					}
					break;
				case "usemtl":
					currentMatName = cmds[1];
					if (currentMesh != null) {
						if (currentMesh.materialName != null) {
							finishMesh(meshes, currentMesh, triVertices, quadVertices);

							currentMesh = new Mesh();
							currentMesh.name = currentMeshName + "_" + currentMatName;
							currentMesh.materialName = currentMatName;
						} else {
							currentMesh.materialName = currentMatName;
						}
					}
					break;
				case "f":
					if (cmds.length - 1 > 4) {
						throw new UnsupportedOperationException("Triangulate yo shit. Or at least quadulate, damn.");
					}

					List<Vertex> vl;
					if (cmds.length - 1 == 3) {
						vl = triVertices;
					} else {
						vl = quadVertices;
					}

					for (int i = 0; i < cmds.length - 1; i++) {
						Vertex v = new Vertex();
						String[] coords = StringEx.splitOnecharFast(cmds[i + 1], '/');
						for (int c = 0; c < coords.length; c++) { //should be 3 at maximum
							if (coords[c].length() == 0) {
								continue; //blank coordinate
							}
							int idx = Integer.parseInt(coords[c]) - 1;
							switch (c) {
								case 0:
									v.position = new Vec3f(posBuffer.get(idx));
									break;
								case 1:
									currentMesh.hasUV[0] = true;
									v.uv[0] = uvBuffer.get(idx);
									if (!isVColStupidFormat && colorBuffer.size() > idx) {
										currentMesh.hasColor = true;
										v.color = new RGBA(colorBuffer.get(idx));
									}
									break;
								case 2:
									currentMesh.hasNormal = true;
									v.normal = new Vec3f(normalBuffer.get(idx));
									break;
								case 3:
									if (isVColStupidFormat) {
										currentMesh.hasColor = true;
										v.color = new RGBA(colorBuffer.get(idx));
									}
									break;
							}
						}
						vl.add(v);
					}
					/*if (cmds.length - 1 == 4) {
							Vertex v1 = currentMesh.vertices.get(currentMesh.vertices.size() - 4);
							Vertex v3 = currentMesh.vertices.get(currentMesh.vertices.size() - 2);
							currentMesh.vertices.add(new Vertex(v1, currentMesh));
							currentMesh.vertices.add(new Vertex(v3, currentMesh));
						}*/
					break;
			}
		}
		if (currentMesh != null) {
			finishMesh(meshes, currentMesh, triVertices, quadVertices);
		}

		//fetch textures
		for (Material mtl : mtllib.materials) {
			boolean hasTexture = false;
			boolean hasVCol = false;
			if (!mtl.textures.isEmpty()) {
				hasTexture = true;
				TextureMapper m = mtl.textures.get(0);
				if (m.textureName != null) {
					FSFile texture = getRelativeOrAbsoluteFile(m.textureName, parentDir);
					m.textureName = texture.getName();
					if (texture.exists()) {
						textures.add(TextureConverter.readTextureFromFile(texture));
					}
				}
			}
			for (Mesh m : meshes) {
				if (Objects.equals(m.materialName, mtl.name)) {
					hasVCol = m.hasColor;
					break;
				}
			}
			TexEnvStage.TexEnvTemplate tmp = TexEnvStage.TexEnvTemplate.PASSTHROUGH;
			if (hasTexture && hasVCol) {
				tmp = TexEnvStage.TexEnvTemplate.TEX0_VCOL;
			} else if (hasTexture) {
				tmp = TexEnvStage.TexEnvTemplate.TEX0;
			} else if (hasVCol) {
				tmp = TexEnvStage.TexEnvTemplate.VCOL;
			}
			mtl.tevStages.stages[0] = new TexEnvStage(tmp);
		}
		scanner.close();
	}

	private static void finishMesh(List<Mesh> meshes, Mesh currentMesh, AbstractVertexList triVerts, AbstractVertexList quadVerts) {
		currentMesh.vertices = new VertexArrayList(triVerts);

		if (!currentMesh.vertices.isEmpty()) {
			meshes.add(currentMesh);
			triVerts.clear();
		}
		if (!quadVerts.isEmpty()) {
			Mesh quadMesh = new Mesh();
			quadMesh.setAttributes(currentMesh);
			quadMesh.primitiveType = PrimitiveType.QUADS;
			quadMesh.vertices = new VertexArrayList(quadVerts);
			meshes.add(quadMesh);
			quadVerts.clear();
		}
	}

	public OBJFile(G3DResource res) {
		textures.addAll(res.textures);
		for (Model mdl : res.models) {
			meshes.addAll(mdl.meshes);
		}
		mtllib = new MTLFile(res);
	}

	public void write(File f, OBJExportSettings settings) {
		write(new DiskFile(f), settings);
	}

	public void write(FSFile f, OBJExportSettings settings) {
		PrintStream out = new PrintStream(f.getNativeOutputStream());

		FSFile mtl = null;

		FSFile parent = f.getParent();
		if (parent != null) {
			mtl = parent.getChild(FSUtil.getFileNameWithoutExtension(f.getName()) + ".mtl");
			if (mtl != null) {
				out.println("mtllib " + mtl.getName());
			}
		}

		int vtxOff = 1;
		int nrmOff = 1;
		int colOff = 1;
		int uvOff = 1;

		for (Mesh mesh : meshes) {
			Skeleton refSkl = mesh.parentModel.skeleton;
			Mesh triOrQuad = PrimitiveConverter.getTriOrQuadMesh(mesh);
			if (mesh.skinningType == Mesh.SkinningType.RIGID) {
				if (triOrQuad == mesh) {
					triOrQuad = new Mesh(triOrQuad);
				}
				MeshProcessor.transformRigidSkinningToSmooth(triOrQuad, refSkl);
			}
			mesh = triOrQuad;

			int stride = 0;
			switch (mesh.primitiveType) {
				case LINES:
					stride = 2;
					break;
				case QUADS:
					stride = 4;
					break;
				case TRIS:
					stride = 3;
					break;
				case QUADSTRIPS:
				case TRIFANS:
				case TRISTRIPS:
				case LINESTRIPS:
					System.err.println("Unexportable primitive type: " + mesh.primitiveType + " at mesh " + mesh.name + ". Please sanitize with PrimitiveConverter first.");
					continue;
			}

			out.print("o ");
			out.println(mesh.name);

			for (Vertex vtx : mesh.vertices) {
				out.print("v ");
				out.println(getVec3Str(vtx.position));
			}

			if (mesh.hasNormal) {
				for (Vertex vtx : mesh.vertices) {
					out.print("vn ");
					out.println(getVec3Str(vtx.normal));
				}
			}

			if (mesh.hasUV(0)) {
				for (Vertex vtx : mesh.vertices) {
					out.print("vt ");
					out.println(getVec2Str(vtx.uv[0]));
				}
			}

			if (settings.VCOL_ENABLE) {
				if (mesh.hasColor) {
					for (Vertex vtx : mesh.vertices) {
						out.print(settings.VCOL_TRIFINDO ? "c " : "vc ");
						if (settings.VCOL_TRIFINDO) {
							out.println(getVec3Str(vtx.color.toVector4().toVec3()));
						} else {
							out.println(getVec4Str(vtx.color.toVector4()));
						}
					}
				} else {
					if (!settings.VCOL_TRIFINDO) {
						for (Vertex vtx : mesh.vertices) {
							out.print("vc ");
							out.println(getVec4Str(RGBA.WHITE.toVector4()));
						}
					}
				}
			}

			out.print("usemtl ");
			out.println(mesh.materialName);

			boolean writeExtraVCol = settings.VCOL_ENABLE && settings.VCOL_TRIFINDO;

			int faceCount = mesh.getVertexCount() / stride;

			for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
				out.print("f ");
				int faceVtxOffset = faceIndex * stride;
				for (int facePointIndex = 0; facePointIndex < stride; facePointIndex++) {
					int val = mesh.useIBO ? mesh.indices.get(faceVtxOffset + facePointIndex) : faceVtxOffset + facePointIndex;

					out.print(val + vtxOff);

					if (mesh.hasUV(0)) {
						out.print("/");
						out.print(val + uvOff);
					} else if (mesh.hasNormal || mesh.hasColor) {
						out.print("/");
					}

					if (mesh.hasNormal) {
						out.print("/");
						out.print(val + nrmOff);
					} else if (mesh.hasColor && writeExtraVCol) {
						out.print("/");
					}

					if (writeExtraVCol) { //trifindo format requires colors on all meshes or nothing
						out.print("/");
						out.print(val + colOff);
					}

					if (facePointIndex < stride - 1) {
						out.print(" ");
					}
				}

				out.println();
			}

			vtxOff += mesh.vertices.size();
			if (mesh.hasColor) {
				colOff += mesh.vertices.size();
			}
			if (mesh.hasNormal) {
				nrmOff += mesh.vertices.size();
			}
			if (mesh.hasUV(0)) {
				uvOff += mesh.vertices.size();
			}
		}

		out.close();

		if (mtl != null) {
			mtllib.write(mtl, textures, settings);
		}
	}

	private static String getVec2Str(Vec2f vec) {
		return vec.x + " " + vec.y;
	}

	private static String getVec3Str(Vec3f vec) {
		return vec.x + " " + vec.y + " " + vec.z;
	}

	private static String getVec4Str(Vec4f vec) {
		return vec.x + " " + vec.y + " " + vec.z + " " + vec.w;
	}

	public static FSFile getRelativeOrAbsoluteFile(String path, FSFile workDir) {
		DiskFile abs = new DiskFile(path);
		if (abs.exists()) {
			return abs;
		}
		return workDir.getChild(path);
	}

	private float[] readFloatArray(String[] cmds) {
		//assume start from 1
		float[] r = new float[cmds.length - 1];
		for (int i = 0; i < r.length; i++) {
			r[i] = Float.parseFloat(cmds[i + 1]);
		}
		return r;
	}

	public G3DResource toGeneric() {
		Model m = new Model();
		m.name = name;
		for (Material mat : mtllib.materials) {
			m.addMaterial(mat);
		}
		for (Mesh mesh : meshes) {
			m.addMesh(mesh);
		}

		G3DResource res = new G3DResource();

		res.addTextures(textures);
		res.addModel(m);
		MaterialProcessor.setAutoAlphaBlendByTexture(res);
		return res;
	}
}
