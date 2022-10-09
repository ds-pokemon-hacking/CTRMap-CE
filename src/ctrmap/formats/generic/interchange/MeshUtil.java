package ctrmap.formats.generic.interchange;

import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.InvalidMagicException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.StringIO;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MeshUtil {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Polygon Mesh", "*.ifpl");
	public static final String MESH_MAGIC = "IFPL";

	public static Mesh readMesh(File f) {
		return readMesh(new DiskFile(f));
	}

	public static Mesh readMesh(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, MESH_MAGIC);

			Mesh mesh = readMesh(l0.io, l0.fileVersion);

			l0.io.close();
			return mesh;
		} catch (IOException ex) {
			Logger.getLogger(MeshUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static Mesh readLevel0Mesh(DataIOStream dis) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0Section(dis, MESH_MAGIC);
			return readMesh(l0.io, l0.fileVersion);
		} catch (IOException ex) {
			Logger.getLogger(MaterialUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void writeMesh(Mesh m, File f) {
		writeMesh(m, new DiskFile(f));
	}

	public static void writeMesh(Mesh m, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(MESH_MAGIC);
			writeMesh(null, m, dos);

			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static final Mesh readMesh(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, MESH_MAGIC)) {
			throw new InvalidMagicException("Invalid mesh magic.");
		}

		Mesh mesh = new Mesh();
		mesh.name = dis.readStringWithAddress();
		mesh.materialName = dis.readStringWithAddress();
		if (fileVersion >= Revisions.REV_VISGROUP_ASSIGN) {
			mesh.visGroupName = dis.readStringWithAddress();
		}
		mesh.renderLayer = dis.readByte();

		if (fileVersion >= Revisions.REV_META_DATA) {
			mesh.metaData = MetaDataUtil.readMetaData(dis, fileVersion);
		}

		if (fileVersion >= Revisions.REV_PRIMITIVE_TYPE) {
			mesh.primitiveType = PrimitiveType.values()[dis.read()];
		}

		if (fileVersion >= Revisions.REV_SKINNING_MODE) {
			mesh.skinningType = Mesh.SkinningType.values()[dis.read() & 3];
		}

		int numAttributes = dis.readByte();

		List<VertexAttrib> attribs = new ArrayList<>();
		for (int i = 0; i < numAttributes; i++) {
			byte attrByte = dis.readByte();
			VertexAttrib a = new VertexAttrib(VertexAttribName.values()[attrByte & 0b1111]);
			a.exists = true;
			a.isConstant = (attrByte & 128) > 0;
			attribs.add(a);
		}

		int vertexCount = dis.readInt();
		for (int i = 0; i < vertexCount; i++) {
			mesh.vertices.add(new Vertex());
		}

		for (VertexAttrib a : attribs) {
			if (a.isConstant) {
				a.constValue = new Vec4f(dis);
			} else {
				for (Vertex v : mesh.vertices) {
					switch (a.name) {
						case POSITION:
							v.position = new Vec3f(dis);
							break;
						case NORMAL:
							v.normal = new Vec3f(dis);
							break;
						case COLOR:
							v.color = new RGBA(dis);
							break;
						case UV0:
							v.uv[0] = new Vec2f(dis);
							break;
						case UV1:
							v.uv[1] = new Vec2f(dis);
							break;
						case UV2:
							v.uv[2] = new Vec2f(dis);
							break;
						case BONE_IDX: {
							int count = dis.readByte();
							for (int i = 0; i < count; i++) {
								v.boneIndices.add(dis.readUnsignedShort());
							}
							break;
						}
						case BONE_WEIGHT: {
							int count = dis.readByte();
							for (int i = 0; i < count; i++) {
								v.weights.add(dis.readFloat());
							}
							break;
						}
					}
				}
			}
		}

		for (VertexAttrib a : attribs) {
			if (a.isConstant) {
				Vec3f constantVec3 = new Vec3f(a.constValue.x, a.constValue.y, a.constValue.z);
				RGBA constantColor = new RGBA(a.constValue);	//we make those to not clog up the memory with values that are all the same

				for (Vertex v : mesh.vertices) {
					switch (a.name) {
						case NORMAL:
							v.normal = constantVec3;
							break;
						case COLOR:
							v.color = constantColor;
							break;
						case BONE_IDX:
							v.boneIndices.add((int) a.constValue.x);
							v.boneIndices.add((int) a.constValue.y);
							v.boneIndices.add((int) a.constValue.z);
							v.boneIndices.add((int) a.constValue.w);
							for (int i = 0; i < v.boneIndices.size(); i++) {
								if (v.boneIndices.get(i) == -1) {
									v.boneIndices.remove(i);
									i--;
								}
							}
							break;
						case BONE_WEIGHT:
							v.weights.add(a.constValue.x);
							v.weights.add(a.constValue.y);
							v.weights.add(a.constValue.z);
							v.weights.add(a.constValue.w);
							while (v.weights.size() > v.boneIndices.size()) {
								v.weights.remove(v.weights.size() - 1);
							}
							break;
					}
				}
			}
		}

		for (VertexAttrib a : attribs) {
			switch (a.name) {
				case BONE_IDX:
					mesh.hasBoneIndices = true;
					break;
				case BONE_WEIGHT:
					mesh.hasBoneWeights = true;
					break;
				case COLOR:
					mesh.hasColor = true;
					break;
				case NORMAL:
					mesh.hasNormal = true;
					break;
				case UV0:
					mesh.hasUV[0] = true;
					break;
				case UV1:
					mesh.hasUV[1] = true;
					break;
				case UV2:
					mesh.hasUV[2] = true;
					break;
			}
		}
		
		if (fileVersion >= Revisions.REV_INDEX_BUFFERS) {
			mesh.useIBO = dis.readBoolean();
			if (mesh.useIBO) {
				int indexCount = dis.readInt();
				int indexSize = dis.read();
				for (int i = 0; i < indexCount; i++) {
					mesh.indices.add(dis.readSized(indexSize));
				}
			}
		}
		
		return mesh;
	}

	public static void writeMesh(Model m, Mesh mesh, CMIFWriter dos) throws IOException {
		//magic and nameofs
		dos.writeStringUnterminated(MESH_MAGIC);
		dos.writeString(mesh.name);
		dos.writeString(mesh.materialName);
		dos.writeString(mesh.visGroupName);
		dos.write(mesh.renderLayer);

		MetaDataUtil.writeMetaData(mesh.metaData, dos);

		dos.writeEnum(mesh.primitiveType);
		dos.writeEnum(mesh.skinningType);

		//generate vertex attribs first for optimization
		VertexAttrib positions = new VertexAttrib(VertexAttribName.POSITION);
		//those are gonna be always present and not constant
		positions.exists = true;

		VertexAttrib normals = new VertexAttrib(VertexAttribName.NORMAL);
		VertexAttrib colors = new VertexAttrib(VertexAttribName.COLOR);
		VertexAttrib boneIndices = new VertexAttrib(VertexAttribName.BONE_IDX);
		VertexAttrib boneWeights = new VertexAttrib(VertexAttribName.BONE_WEIGHT);

		VertexAttrib texCoord0 = new VertexAttrib(VertexAttribName.UV0);
		VertexAttrib texCoord1 = new VertexAttrib(VertexAttribName.UV1);
		VertexAttrib texCoord2 = new VertexAttrib(VertexAttribName.UV2);

		texCoord0.exists = mesh.hasUV(0);
		texCoord1.exists = mesh.hasUV(1);
		texCoord2.exists = mesh.hasUV(2);
		colors.exists = mesh.hasColor;
		normals.exists = mesh.hasNormal;
		boneIndices.exists = mesh.hasBoneIndices;
		boneWeights.exists = mesh.hasBoneWeights;

		for (Vertex v : mesh.vertices) {
			if (mesh.hasColor) {
				checkConstantVertex(v.color.toVector4(), colors);
			}
			if (mesh.hasNormal) {
				checkConstantVertex(new Vec4f(v.normal.x, v.normal.y, v.normal.z, 0f), normals);
			}
			if (mesh.hasBoneIndices) {
				//only do this if there is leq 3 (yes, not 4) since that's the most BCH supports for it
				if (v.boneIndices.size() <= 3) {
					checkConstantVertex(new Vec4f(getValueNonEx(0, v.boneIndices), getValueNonEx(1, v.boneIndices), getValueNonEx(2, v.boneIndices), 0), colors);
				} else {
					//disallow constants and finalize
					boneIndices.isConstant = false;
					boneIndices.finalized = true;
				}
			}
			if (mesh.hasBoneWeights) {
				if (v.weights.size() <= 3) {
					checkConstantVertex(new Vec4f(getValueNonExF(0, v.weights), getValueNonExF(1, v.weights), getValueNonExF(2, v.weights), 0), colors);
				} else {
					//disallow constants and finalize
					boneIndices.isConstant = false;
					boneIndices.finalized = true;
				}
			}
			//I mean come on, literally nothing has constant UVs. That's a waste of space.
		}
		/*if (colors.exists && colors.isConstant && colors.constValue.equals(RGBA.WHITE.getVector4())) {
			colors.exists = false;	//Having a constant white is quite useless
		}*/ //yes, it's useless, but we'd have to remap materials that rely on it
		if (m != null && m.skeleton.getJoints().isEmpty()) {
			//indices and weights are useless
			boneIndices.exists = false;
			boneWeights.exists = false;
		}

		//Constants done, write buffer format
		int numAttribs = 1; //positions
		numAttribs += normals.exists ? 1 : 0;
		numAttribs += colors.exists ? 1 : 0;
		numAttribs += texCoord0.exists ? 1 : 0;
		numAttribs += texCoord1.exists ? 1 : 0;
		numAttribs += texCoord2.exists ? 1 : 0;
		numAttribs += boneWeights.exists ? 1 : 0;
		numAttribs += boneIndices.exists ? 1 : 0;

		dos.write(numAttribs);

		writeVertexAttrib(positions, dos);
		writeVertexAttrib(normals, dos);
		writeVertexAttrib(colors, dos);
		writeVertexAttrib(texCoord0, dos);
		writeVertexAttrib(texCoord1, dos);
		writeVertexAttrib(texCoord2, dos);
		writeVertexAttrib(boneIndices, dos);
		writeVertexAttrib(boneWeights, dos);

		//since this converter always keeps the given order (positions, normals, colors...), we can just dump it into the buffer
		int vertexCount = mesh.vertices.size();
		dos.writeInt(vertexCount);
		writeVertexAttribArray(positions, mesh.vertices, dos);
		writeVertexAttribArray(normals, mesh.vertices, dos);
		writeVertexAttribArray(colors, mesh.vertices, dos);
		writeVertexAttribArray(texCoord0, mesh.vertices, dos);
		writeVertexAttribArray(texCoord1, mesh.vertices, dos);
		writeVertexAttribArray(texCoord2, mesh.vertices, dos);
		writeVertexAttribArray(boneIndices, mesh.vertices, dos);
		writeVertexAttribArray(boneWeights, mesh.vertices, dos);

		dos.writeBoolean(mesh.useIBO);
		if (mesh.useIBO) {
			dos.writeInt(mesh.indices.size());
			int maxIndex = vertexCount - 1;
			int size = (maxIndex > 0xFFFF) ? 4 : (maxIndex > 0xFF) ? 2 : 1;
			dos.write(size);
			for (int i = 0; i < mesh.indices.size(); i++) {
				dos.writeSized(mesh.indices.get(i), size);
			}
		}
	}

	private static void writeVertexAttrib(VertexAttrib a, DataOutput dos) throws IOException {
		if (a.exists) {
			dos.write(a.name.ordinal() | ((a.isConstant ? 1 : 0) << 7));
		}
	}

	private static void writeVertexAttribArray(VertexAttrib a, Iterable<Vertex> vertices, DataOutput dos) throws IOException {
		if (a.exists) {
			if (a.isConstant) {
				a.constValue.write(dos);
			} else {
				for (Vertex v : vertices) {
					switch (a.name) {
						case BONE_IDX:
							dos.write(v.boneIndices.size());
							for (int i = 0; i < v.boneIndices.size(); i++) {
								dos.writeShort((short) v.boneIndices.get(i));
							}
							break;
						case BONE_WEIGHT:
							dos.write(v.weights.size());
							for (int i = 0; i < v.weights.size(); i++) {
								dos.writeFloat(v.weights.get(i));
							}
							break;
						case COLOR:
							v.color.write(dos);
							break;
						case NORMAL:
							v.normal.write(dos);
							break;
						case POSITION:
							v.position.write(dos);
							break;
						case UV0:
							v.uv[0].write(dos);
							break;
						case UV1:
							v.uv[1].write(dos);
							break;
						case UV2:
							v.uv[2].write(dos);
							break;
					}
				}
			}
		}
	}

	private static float getValueNonEx(int index, IntList l) {
		if (index >= l.size()) {
			return -1;
		}
		return l.get(index);
	}

	private static float getValueNonExF(int index, FloatList l) {
		if (index >= l.size()) {
			return 0;
		}
		return l.get(index);
	}

	private static void checkConstantVertex(Vec4f vec, VertexAttrib a) {
		if (!a.finalized) {
			if (!a.isConstant) {
				//first vertex, set it
				a.isConstant = true;
				a.constValue = new Vec4f(vec);
			}
			if (!vec.equalsImprecise(a.constValue, 0.01f)) {
				a.isConstant = false;
				a.finalized = true; //the loop won't run again for this one, so the constant stays
			}
		}
	}

	public static class VertexAttrib {

		public boolean finalized = false;

		public boolean exists = false;
		public boolean isConstant = false;
		public Vec4f constValue = new Vec4f(0f, 0f, 0f, 0f);

		public VertexAttribName name;

		public VertexAttrib(VertexAttribName name) {
			this.name = name;
		}
	}

	public static enum VertexAttribName {
		POSITION,
		NORMAL,
		COLOR,
		UV0,
		UV1,
		UV2,
		BONE_IDX,
		BONE_WEIGHT
	}
}
