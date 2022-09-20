package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PrimitiveConverter {

	/*
	http://www.dgp.toronto.edu/~ah/csc418/fall_2001/tut/ogl_draw.html
	 */
	public static Mesh getTriOrQuadMesh(Mesh original) {
		switch (original.primitiveType) {
			case LINES:
			case QUADS:
			case TRIS:
				return original;
		}

		Mesh ret = new Mesh(original);

		switch (original.primitiveType) {
			case QUADSTRIPS:
			case TRISTRIPS:
			case LINESTRIPS:
				stripsToNormal(ret);
				break;
			case TRIFANS:
				fansToNormal(ret);
				break;
		}

		return ret;
	}

	public static Mesh getTriMesh(Mesh original) {
		Mesh m = getTriOrQuadMesh(original);

		if (m.primitiveType == PrimitiveType.QUADS) {
			if (m == original) {
				m = new Mesh(original);
			}
			triangulateQuads(m);
		}

		return m;
	}

	public static void stripsToNormal(Mesh mesh) {
		stripsToNormal(mesh, true);
	}

	public static void stripsToNormal(Mesh mesh, boolean updateBuffers) {
		switch (mesh.primitiveType) {
			case LINESTRIPS:
				linestripsToNormal(mesh, updateBuffers);
				break;
			case QUADSTRIPS:
				quadstripsToNormal(mesh, updateBuffers);
				break;
			case TRISTRIPS:
				tristripsToNormal(mesh, updateBuffers);
				break;
		}
	}

	public static void linestripsToNormal(Mesh mesh, boolean updateBuffers) {
		if (mesh.primitiveType != PrimitiveType.LINESTRIPS) {
			return;
		}
		VertexMemory mem = new VertexMemory(mesh);
		Vertex last = null;

		for (Vertex vtx : mesh) {
			if (last != null) {
				mem.push(last);
				mem.push(vtx);
			}

			last = vtx;
		}

		mesh.primitiveType = PrimitiveType.LINES;
		mesh.vertices = new VertexArrayList(mem.buffer);
		if (updateBuffers) {
			mesh.createAndInvalidateBuffers();
		}
	}

	public static void quadstripsToNormal(Mesh mesh, boolean updateBuffers) {
		if (mesh.primitiveType != PrimitiveType.QUADSTRIPS) {
			return;
		}
		VBOProcessor.makeInline(mesh, false);

		VertexMemory mem = new VertexMemory(mesh);

		int vertexIdx = 2;

		mem.push(mesh.vertices.get(1));
		mem.push(mesh.vertices.get(0));

		Vertex last;
		Vertex pu;
		Vertex vtx;

		while (vertexIdx < mesh.vertices.size()) {
			last = mem.getLast();
			vtx = mesh.vertices.get(vertexIdx);

			pu = mem.getPenultimate();
			mem.push(last);
			mem.push(pu);
			mem.push(mesh.vertices.get(vertexIdx + 1));
			mem.push(vtx);

			vertexIdx += 2;
		}

		mem.buffer.remove(0);
		mem.buffer.remove(0); //remove the reference primitives pushed before the process

		mesh.primitiveType = PrimitiveType.QUADS;
		mesh.vertices = new VertexArrayList(mem.buffer);
		if (updateBuffers) {
			mesh.createAndInvalidateBuffers();
		}
	}

	public static void tristripsToNormal(Mesh mesh, boolean updateBuffers) {
		if (mesh.primitiveType != PrimitiveType.TRISTRIPS) {
			return;
		}
		VBOProcessor.makeInline(mesh, false);

		VertexMemory mem = new VertexMemory(mesh);

		int vertexIdx = 2;

		mem.push(mesh.vertices.get(1));
		mem.push(mesh.vertices.get(0));

		Vertex last;
		Vertex pu;
		Vertex vtx;

		boolean b = false;

		while (vertexIdx < mesh.vertices.size()) {
			last = mem.getLast();
			vtx = mesh.vertices.get(vertexIdx);
			pu = mem.getPenultimate();
			if (!b) {
				mem.push(last);
				mem.push(pu);
				mem.push(vtx);
			} else {
				mem.push(pu);
				mem.push(vtx);
				mem.push(last);
			}
			b = !b;

			vertexIdx++;
		}

		mem.buffer.remove(0);
		mem.buffer.remove(0); //remove the reference primitives pushed before the process

		mesh.primitiveType = PrimitiveType.TRIS;
		mesh.vertices = new VertexArrayList(mem.buffer);
		if (updateBuffers) {
			mesh.createAndInvalidateBuffers();
		}
	}

	public static void fansToNormal(Mesh mesh) {
		if (mesh.primitiveType == PrimitiveType.TRIFANS) {
			VertexMemory mem = new VertexMemory(mesh);

			mem.push(mesh.vertices.get(0)); //the shared vertex

			for (int i = 1; i < mesh.vertices.size(); i += 2) {
				mem.push(mesh.vertices.get(i));
				mem.push(mesh.vertices.get(i + 1));
				mem.push(mem.getFirst());
			}

			mesh.primitiveType = PrimitiveType.TRIS;
			mesh.vertices = new VertexArrayList(mem.buffer);

			mesh.createAndInvalidateBuffers();
		}
	}

	public static void triangulate(Mesh mesh) {
		switch (mesh.primitiveType) {
			case QUADS:
				triangulateQuads(mesh);
				break;
			case QUADSTRIPS:
				stripsToNormal(mesh, false);
				triangulateQuads(mesh);
				break;
			case TRIFANS:
				fansToNormal(mesh);
				break;
			case TRISTRIPS:
				stripsToNormal(mesh);
				break;
		}
	}

	public static void triangulateQuads(Mesh mesh) {
		if (mesh.primitiveType == PrimitiveType.QUADS) {
			VBOProcessor.makeInline(mesh, false);
			VertexMemory mem = new VertexMemory(mesh);

			for (int i = 0; i < mesh.vertices.size(); i += 4) {
				Vertex v0 = mesh.vertices.get(i + 0);
				Vertex v1 = mesh.vertices.get(i + 1);
				Vertex v2 = mesh.vertices.get(i + 2);
				Vertex v3 = mesh.vertices.get(i + 3);

				mem.push(v0);
				mem.push(v1);
				mem.push(v2);

				mem.push(v3);
				mem.push(v0);
				mem.push(v2);
			}

			mesh.primitiveType = PrimitiveType.TRIS;
			mesh.vertices = new VertexArrayList(mem.buffer);

			mesh.createAndInvalidateBuffers();
		}
	}

	private static class VertexMemory {

		private List<Vertex> buffer = new ArrayList<>();
		private Mesh mesh;

		public VertexMemory(Mesh mesh) {
			this.mesh = mesh;
		}

		public void push(Vertex vtx) {
			buffer.add(new Vertex(vtx, mesh));
		}

		public boolean notEnoughData() {
			return buffer.size() < 2;
		}

		public Vertex getFirst() {
			return buffer.get(0);
		}

		public Vertex getLast() {
			return buffer.get(buffer.size() - 1);
		}

		public Vertex getPenultimate() {
			return buffer.get(buffer.size() - 2);
		}
	}
}
