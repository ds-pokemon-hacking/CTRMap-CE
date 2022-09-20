package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Face;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;

public class QuadstripConverter {

	public static List<Mesh> makeQuadstrips(Mesh mesh) {
		return makeQuadstrips(mesh, -1);
	}

	public static List<Mesh> makeQuadstrips(Mesh mesh, int minimumSuccessiveQuadCount) {
		if (minimumSuccessiveQuadCount == -1) {
			minimumSuccessiveQuadCount = 3;
		}
		if (mesh.primitiveType == PrimitiveType.QUADS) {
			/*for (Vertex v : mesh.vertices) {
				System.out.println(v.position);
			}*/

			List<Quad> quads = new ArrayList<>();

			for (Face face : mesh.faces()) {
				quads.add(new Quad(face));
			}

			mesh.useIBO = false;
			mesh.indices.clear();

			//First, eliminate the quads that can't be stripped to reduce cycles for the next process
			List<Quad> lonelyQuads = new ArrayList<>();

			for (int i = 0; i < quads.size(); i++) {
				Quad q = quads.get(i);

				boolean canStrip = false;

				for (Quad q2 : quads) {
					if (q != q2 && q.getSharedEdgeNo(q2) != -1) {
						canStrip = true;
						break;
					}
				}

				if (!canStrip) {
					quads.remove(i);
					i--;
					lonelyQuads.add(q);
				}
			}

			List<Quadstrip> quadstrips = new ArrayList<>();

			while (!quads.isEmpty()) {
				QuadLoop:
				for (Quad q : quads) {
					for (Quad q2 : quads) {
						if (q == q2) {
							continue;
						}
						int sen = q.getSharedEdgeNo(q2);
						if (sen != -1) {
							Quadstrip qs = new Quadstrip(q, q2, quads);
							if (qs.buffer.size() >= minimumSuccessiveQuadCount) {
								quadstrips.add(qs);
							} else {
								quads.addAll(qs.buffer);
								lonelyQuads.add(q);
								quads.remove(q);
							}
							break QuadLoop;
						}
					}

					lonelyQuads.add(q);
					quads.remove(q);
					break;
				}
			}

			mesh.vertices = quadListToVerts(lonelyQuads);

			List<Mesh> meshes = new ArrayList<>();

			for (Quadstrip qs : quadstrips) {
				Mesh newMesh = new Mesh();
				newMesh.setAttributes(mesh);
				newMesh.primitiveType = PrimitiveType.QUADSTRIPS;

				newMesh.vertices = qs.getVertices(mesh);

				meshes.add(newMesh);
			}

			meshes.add(mesh);

			for (int i = 0; i < meshes.size(); i++) {
				if (meshes.get(i).vertices.isEmpty()) {
					meshes.remove(i);
				}
			}

			/*for (Mesh m : meshes) {
				System.out.println(m.primitiveType);

				for (Vertex v : m.vertices) {
					System.out.println(v.position);
				}
			}*/
			return meshes;
		}
		return ArraysEx.asList(mesh);
	}

	private static VertexArrayList quadListToVerts(List<Quad> quads) {
		VertexArrayList l = new VertexArrayList();

		for (Quad q : quads) {
			l.addAll(ArraysEx.asList(q.verts));
		}

		return l;
	}

	private static class Quadstrip {

		private List<Quad> buffer = new ArrayList<>();

		public Quadstrip(Quad q, Quad q2, List<Quad> quads) {
			buffer.add(q);
			quads.remove(q);
			q.setAutoLRForConnection(q.getSharedEdgeNo(q2));
			addNextQuad(q, quads);
			addPreviousQuad(q, quads);
		}

		private void addNextQuad(Quad quad, List<Quad> quads) {
			Edge rEdge = quad.qs_RightEdge;
			for (Quad nextQuad : quads) {
				if (nextQuad != quad) {
					Edge nextLEdge = nextQuad.getEdgeByEdge(rEdge);
					if (nextLEdge != null) {
						nextQuad.qs_LeftEdge = nextLEdge;
						nextQuad.setRightEdgeByLeftEdge();
						if (nextLEdge.correctOrientation(rEdge)) {
							nextQuad.qs_RightEdge.swapVertices();
						}
						quads.remove(nextQuad);
						buffer.add(nextQuad);
						addNextQuad(nextQuad, quads);
						break;
					}
				}
			}
		}

		private void addPreviousQuad(Quad quad, List<Quad> quads) {
			Edge lEdge = quad.qs_LeftEdge;
			for (Quad q : quads) {
				if (q != quad) {
					Edge e2 = q.getEdgeByEdge(lEdge);
					if (e2 != null) {
						q.qs_RightEdge = e2;
						q.setLeftEdgeByRightEdge();
						if (e2.correctOrientation(lEdge)) {
							q.qs_LeftEdge.swapVertices();
						}
						quads.remove(q);
						buffer.add(0, q);
						addPreviousQuad(q, quads);
						break;
					}
				}
			}
		}

		private void addVtx(VertexArrayList l, Vertex v, Mesh mesh) {
			l.add(/*new Vertex(v, mesh)*/v);
		}

		public VertexArrayList getVertices(Mesh mesh) {
			VertexArrayList l = new VertexArrayList();

			addVtx(l, buffer.get(0).qs_LeftEdge.vert1, mesh);
			addVtx(l, buffer.get(0).qs_LeftEdge.vert2, mesh);

			for (int i = 0; i < buffer.size(); i++) {
				addVtx(l, buffer.get(i).qs_RightEdge.vert1, mesh);
				addVtx(l, buffer.get(i).qs_RightEdge.vert2, mesh);
			}

			return l;
		}
	}

	private static class Quad {

		private Vertex[] verts = new Vertex[4];
		private Edge[] edges = new Edge[4];

		private Edge qs_LeftEdge;
		private Edge qs_RightEdge;

		public Quad(Face face) {
			//Invert the 1st two edges so that they are wound in the correct order
			verts = face.vertices;
			edges[0] = new Edge(verts[1], verts[0], true);
			edges[1] = new Edge(verts[2], verts[1], true);
			edges[2] = new Edge(verts[2], verts[3], false);
			edges[3] = new Edge(verts[3], verts[0], false);
		}

		public void setAutoLRForConnection(int connEdgeIdx) {
			Edge e = edges[connEdgeIdx];
			Edge invE = edges[getAnotherSharedEdgeForStripByEdgeNo(connEdgeIdx)];
			if (connEdgeIdx < 2) {
				qs_RightEdge = e;
				qs_LeftEdge = invE;
			} else {
				qs_LeftEdge = e;
				qs_RightEdge = invE;
			}
		}

		public void setLeftEdgeByRightEdge() {
			for (int i = 0; i < edges.length; i++) {
				if (edges[i] == qs_RightEdge) {
					qs_LeftEdge = edges[getAnotherSharedEdgeForStripByEdgeNo(i)];
					break;
				}
			}
		}

		public void setRightEdgeByLeftEdge() {
			for (int i = 0; i < edges.length; i++) {
				if (edges[i] == qs_LeftEdge) {
					qs_RightEdge = edges[getAnotherSharedEdgeForStripByEdgeNo(i)];
					break;
				}
			}
		}

		public Edge getEdgeByEdge(Edge e) {
			for (Edge e2 : edges) {
				if (e2.hasSameVerts(e)) {
					return e2;
				}
			}
			return null;
		}

		public int getSharedEdgeNo(Quad q2) {
			for (int i = 0; i < edges.length; i++) {
				Edge e = edges[i];
				for (Edge e2 : q2.edges) {
					if (e.hasSameVertsInvOnly(e2)) {
						return i;
					}
				}
			}
			return -1;
		}

		public static int getAnotherSharedEdgeForStripByEdgeNo(int edgeNo) {
			edgeNo += 2;
			if (edgeNo > 3) {
				edgeNo -= 4;
			}
			return edgeNo;
		}
	}

	private static class Edge {

		public Vertex vert1;
		public Vertex vert2;

		private Vertex a_vert1;
		private Vertex a_vert2;

		public Edge(Vertex v1, Vertex v2, boolean invertActualOrder) {
			vert1 = v1;
			vert2 = v2;
			if (invertActualOrder) {
				a_vert1 = vert2;
				a_vert2 = vert1;
			} else {
				a_vert1 = vert1;
				a_vert2 = vert2;
			}
		}

		public boolean correctOrientation(Edge e) {
			if (vert1.position.equals(e.vert2.position)) {
				swapVertices();
				return true;
			}
			return false;
		}

		public void swapVertices() {
			Vertex temp = vert1;
			vert1 = vert2;
			vert2 = temp;
		}

		public boolean hasSameVerts(Edge e2) {
			if (vert1.equals(e2.vert1, true, true, true, true)) {
				return vert2.equals(e2.vert2, true, true, true, true);
			} else if (vert2.equals(e2.vert1, true, true, true, true)) {
				return vert1.equals(e2.vert2, true, true, true, true);
			}
			return false;
		}

		public boolean hasSameVertsInvOnly(Edge e2) {
			if (a_vert2.equals(e2.a_vert1, true, true, true, true)) {
				return a_vert1.equals(e2.a_vert2, true, true, true, true);
			}
			return false;
		}
	}
}
