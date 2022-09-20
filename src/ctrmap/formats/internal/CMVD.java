package ctrmap.formats.internal;

import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.io.util.StringIO;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Vertex;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CTRMap Vertex Data, a lightweight format to store colored vertices. Supports
 * only up to 65535 vertices as the current scope of its usage does not need
 * more than that.
 */
public class CMVD {

	public static final String CMVD_MAGIC = "CMVD";

	public List<CMVDVertex> vertices = new ArrayList<>();
	public List<int[]> faces = new ArrayList<>();

	public Vec3f minVector = new Vec3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
	public Vec3f maxVector = new Vec3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);

	public CMVD() {

	}

	public CMVD(G3DResource res) {
		int vertexCount = 0;
		for (Model model : res.models) {
			for (Mesh mesh : model.meshes) {
				for (Vertex v : mesh) {
					CMVDVertex cmv = new CMVDVertex();
					cmv.col = v.color;
					cmv.x = v.position.x;
					cmv.y = v.position.y;
					cmv.z = v.position.z;

					vertices.add(cmv);
					vertexCount++;
				}
			}
		}
		for (int i = 0; i < vertexCount; i += 3) {
			faces.add(new int[]{i, i + 1, i + 2});
		}
	}

	public byte[] getBinaryData() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			write(out);
		} catch (IOException ex) {
			Logger.getLogger(CMVD.class.getName()).log(Level.SEVERE, null, ex);
		}
		return out.toByteArray();
	}

	public void write(OutputStream out) throws IOException {
		DataOutputStream dos = new DataOutputStream(out);
		StringIO.writeStringUnterminated(dos, CMVD_MAGIC);
		dos.writeShort(vertices.size());
		dos.writeShort(faces.size());
		for (int vertex = 0; vertex < vertices.size(); vertex++) {
			vertices.get(vertex).write(dos);
		}
		for (int face = 0; face < faces.size(); face++) {
			int[] f = faces.get(face);
			dos.writeShort(f[0]);
			dos.writeShort(f[1]);
			dos.writeShort(f[2]);
		}
		dos.close();
	}

	public CMVD(InputStream in) {
		this(in, true);
	}

	public CMVD(InputStream in, boolean closeStream) {
		try {
			DataInputStream dis = new DataInputStream(in);
			if (!StringIO.checkMagic(dis, CMVD_MAGIC)) {
				System.err.println("CMVD magic invalid.");
				return;
			}
			int verticesCount = dis.readUnsignedShort();
			int facesCount = dis.readUnsignedShort();
			for (int vertex = 0; vertex < verticesCount; vertex++) {
				CMVDVertex v = new CMVDVertex(dis);
				if (v.x < minVector.x) {
					minVector.x = v.x;
				}
				if (v.x > maxVector.x) {
					maxVector.x = v.x;
				}
				if (v.y < minVector.y) {
					minVector.y = v.y;
				}
				if (v.y > maxVector.y) {
					maxVector.y = v.y;
				}
				if (v.z < minVector.z) {
					minVector.z = v.z;
				}
				if (v.z > maxVector.z) {
					maxVector.z = v.z;
				}
				vertices.add(v);
			}
			for (int face = 0; face < facesCount; face++) {
				faces.add(new int[]{dis.readUnsignedShort(), dis.readUnsignedShort(), dis.readUnsignedShort()});
			}
			dis.close();
		} catch (IOException ex) {
			Logger.getLogger(CMVD.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public G3DResource toGeneric() {
		Model m = new Model();
		m.name = "CMVD" + System.currentTimeMillis();
		Mesh cmvd = new Mesh();
		cmvd.hasColor = false;
		cmvd.name = m.name + "_rootMesh";
		for (int[] f : faces) {
			for (int i = 0; i < 3; i++) {
				CMVDVertex v = vertices.get(f[i]);
				Vertex v2 = new Vertex();
				v2.position = new Vec3f(v.x, v.y, v.z);
				v2.color = new RGBA(v.col.r, v.col.g, v.col.b, v.col.a);
				if (!v2.color.equals(RGBA.WHITE)) {
					cmvd.hasColor = true;
				}
				cmvd.vertices.add(v2);
			}
		}
		m.addMesh(cmvd);
		return new G3DResource(m);
	}
	
	public static class CMVDVertex {

		public float x;
		public float y;
		public float z;

		public RGBA col;

		public CMVDVertex() {
		}

		public CMVDVertex(DataInputStream dis) throws IOException {
			x = dis.readFloat();
			y = dis.readFloat();
			z = dis.readFloat();
			col = new RGBA(dis);
		}

		public void write(DataOutputStream dos) throws IOException {
			dos.writeFloat(x);
			dos.writeFloat(y);
			dos.writeFloat(z);
			col.write(dos);
		}
	}
}
