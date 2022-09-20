package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Face;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TristripConverter {

	public static final boolean TS_DEBUG = false;

	public static List<Mesh> makeTristrips(Mesh mesh) {
		return makeTristrips(mesh, -1);
	}
	
	public static List<Mesh> makeTristrips(Mesh mesh, int minimumSuccessiveTriCount) {
		if (minimumSuccessiveTriCount == -1){
			minimumSuccessiveTriCount = 4;
		}
		if (mesh.primitiveType == PrimitiveType.TRIS) {
			/*for (Vertex v : mesh.vertices) {
				System.out.println(v.position);
			}*/

			List<Tri> tris = new ArrayList<>();

			for (Face face : mesh.faces()) {
				tris.add(new Tri(face));
			}
			
			mesh.useIBO = false;
			mesh.indices.clear();

			//First, eliminate the quads that can't be stripped to reduce cycles for the next process
			List<Tri> lonelyTris = new ArrayList<>();

			for (int i = 0; i < tris.size(); i++) {
				Tri t = tris.get(i);

				boolean canStrip = false;

				for (Tri t2 : tris) {
					if (t != t2 && t.getSharedEdgeNo(t2) != -1) {
						canStrip = true;
						break;
					}
				}

				if (!canStrip) {
					tris.remove(i);
					i--;
					lonelyTris.add(t);
				}
			}

			List<Tristrip> tristrips = new ArrayList<>();

			while (!tris.isEmpty()) {
				TriLoop:
				for (Tri t : tris) {
					for (Tri t2 : tris) {
						if (t == t2) {
							continue;
						}
						int sen = t.getSharedEdgeNo(t2);
						if (sen != -1) {
							Tristrip ts = new Tristrip(t, t2, tris);
							if (ts.buffer.get(0).ts_InvWinding) {
								if ((ts.buffer.size() & 1) == 1) {
									ts.pleaseReverseMe = true;
								} else {
									tris.add(ts.buffer.get(0));
									ts.buffer.remove(0);
								}
							}
							if (ts.buffer.size() >= minimumSuccessiveTriCount) {
								tristrips.add(ts);
							} else {
								tris.addAll(ts.buffer);
								lonelyTris.add(t);
								tris.remove(t);
							}
							break TriLoop;
						}
					}

					lonelyTris.add(t);
					tris.remove(t);
					break;
				}
			}

			List<Mesh> meshes = new ArrayList<>();

			for (Tristrip ts : tristrips) {
				Mesh newMesh = new Mesh();
				newMesh.setAttributes(mesh);
				newMesh.primitiveType = PrimitiveType.TRISTRIPS;

				newMesh.vertices = ts.getVertices();

				meshes.add(newMesh);
			}

			mesh.vertices = triListToVerts(lonelyTris);

			meshes.add(mesh);

			for (int i = 0; i < meshes.size(); i++) {
				if (meshes.get(i).vertices.isEmpty()) {
					meshes.remove(i);
				}
			}

			if (TS_DEBUG) {
				for (Mesh m : meshes) {
					System.out.println(m.primitiveType);

					for (Vertex v : m.vertices) {
						System.out.println(v.position);
					}
				}
			}
			return meshes;
		}
		return ArraysEx.asList(mesh);
	}

	private static VertexArrayList triListToVerts(List<Tri> quads) {
		VertexArrayList l = new VertexArrayList();

		for (Tri q : quads) {
			l.addAll(ArraysEx.asList(q.verts));
		}

		return l;
	}

	private static class Tristrip {

		private boolean pleaseReverseMe = false;
		private List<Tri> buffer = new ArrayList<>();

		public Tristrip(Tri t, Tri t2, List<Tri> tris) {
			buffer.add(t);
			tris.remove(t);
			t.ts_InvWinding = false;
			Edge sharedEdge = t.edges[t.getSharedEdgeNo(t2)];
			t.reorderVertsForSharedEdgeWithNext(sharedEdge);
			if (TS_DEBUG) {
				System.out.println("two initial tris share edge " + sharedEdge);
			}

			t.ts_SharedEdgeWithPrev = t.getEdgeByVerts(t.getVertNotInEdge(sharedEdge), sharedEdge.vert1);
			t.ts_LastVertex = t.getVertNotInEdge(t.ts_SharedEdgeWithPrev);

			addNextTri(t, sharedEdge, tris, true);
			addPrevTri(t, t.ts_SharedEdgeWithPrev, tris, true);
		}

		public final void addNextTri(Tri lastTri, Edge edge, List<Tri> tris, boolean invWinding) {
			for (Tri t : tris) {
				if (t != lastTri) {
					Edge e2 = t.getEdgeByEdge(edge, true);
					if (e2 != null) {
						//System.out.println("next");
						t.reorderVertsForSharedEdgeWithPrev(e2);
						t.ts_SharedEdgeWithPrev = e2;
						t.ts_LastVertex = t.getVertNotInEdge(e2);
						t.ts_InvWinding = invWinding;

						tris.remove(t);
						buffer.add(t);

						Edge nextSharedEdge = t.getEdgeByVerts(t.ts_LastVertex, invWinding ? t.ts_SharedEdgeWithPrev.vert1 : t.ts_SharedEdgeWithPrev.vert2); //vert1 is the one valid for CCW winding
						if (TS_DEBUG) {
							System.out.println("searching for next edge by shared : " + nextSharedEdge + " prev edge " + t.ts_SharedEdgeWithPrev);
						}

						addNextTri(t, nextSharedEdge, tris, !invWinding);

						break;
					}
				}
			}
		}

		public final void addPrevTri(Tri lastTri, Edge edge, List<Tri> tris, boolean invWinding) {
			for (Tri t : tris) {
				if (t != lastTri) {
					Edge e2 = t.getEdgeByEdge(edge, true);
					if (e2 != null) {
						t.reorderVertsForSharedEdgeWithNext(e2);
						t.ts_LastVertex = invWinding ? e2.vert1 : e2.vert2;
						t.ts_SharedEdgeWithPrev = t.getEdgeByVerts(t.getVertNotInEdge(e2), invWinding ? e2.vert2 : e2.vert1);
						t.ts_InvWinding = invWinding;

						tris.remove(t);
						buffer.add(0, t);
						if (TS_DEBUG) {
							System.out.println("prev, attached to edge: " + e2 + ", searching for another " + t.ts_SharedEdgeWithPrev);
						}

						addPrevTri(t, t.ts_SharedEdgeWithPrev, tris, !invWinding);

						break;
					}
				}
			}
		}

		public VertexArrayList getVertices() {
			VertexArrayList l = new VertexArrayList();

			Vertex[] verts0 = buffer.get(0).verts;
			if (buffer.get(0).ts_InvWinding) {
				l.add(verts0[0]);
				l.add(verts0[2]);
				l.add(verts0[1]);
			} else {
				l.add(verts0[0]);
				l.add(verts0[1]);
				l.add(verts0[2]);
			}

			for (int i = 1; i < buffer.size(); i++) {
				l.add(buffer.get(i).ts_LastVertex);
			}
			
			if (pleaseReverseMe){
				Collections.reverse(l);
				if (TS_DEBUG){
					System.out.println("reversez-moi, s'il vous plait ~~");
				}
			}

			return l;
		}
	}

	private static class Tri {

		private int dbg_TriId = -1;

		private Vertex[] verts = new Vertex[3];
		private Edge[] edges = new Edge[3];

		private Edge ts_SharedEdgeWithPrev;
		private Vertex ts_LastVertex;

		private boolean ts_InvWinding;

		public Tri(Face face) {
			verts = face.vertices;
			edges[0] = new Edge(verts[0], verts[1]);
			edges[1] = new Edge(verts[1], verts[2]);
			edges[2] = new Edge(verts[2], verts[0]);
			dbg_TriId = face.vertexBufferOffset / 3;
			/*System.out.println("Triangle --- " + dbg_TriId);
			System.out.println(edges[0]);
			System.out.println(edges[1]);
			System.out.println(edges[2]);*/
		}

		public void reorderVertsForSharedEdgeWithNext(Edge e) {
			//The edge has to be the last 3 verts
			Vertex v3 = getVertNotInEdge(e);
			verts[0] = v3;
			verts[1] = e.vert1;
			verts[2] = e.vert2;

			Edge e0 = getEdgeByVerts(v3, e.vert1);
			Edge e2 = getEdgeByVerts(e.vert2, v3);
			edges[0] = e0;
			edges[1] = e;
			edges[2] = e2;
		}

		public void reorderVertsForSharedEdgeWithPrev(Edge e) {
			//The edge has to be the last 3 verts
			Vertex v3 = getVertNotInEdge(e);
			verts[0] = e.vert1;
			verts[1] = e.vert2;
			verts[2] = v3;

			Edge e1 = getEdgeByVerts(verts[1], verts[2]);
			Edge e2 = getEdgeByVerts(verts[2], verts[0]);
			edges[0] = e;
			edges[1] = e1;
			edges[2] = e2;
		}

		public Edge getEdgeByEdge(Edge e, boolean invWinding) {
			for (Edge e2 : edges) {
				if (e2.hasSameVerts(e, invWinding)) {
					return e2;
				}
			}
			return null;
		}

		public Edge getEdgeByVerts(Vertex v1, Vertex v2) {
			for (Edge e : edges) {
				if ((e.vert1 == v1 && e.vert2 == v2) || (e.vert1 == v2 && e.vert2 == v1)) {
					return e;
				}
			}
			return null;
		}

		public Vertex getVertNotInEdge(Edge e) {
			for (Vertex v : verts) {
				if (v != e.vert1 && v != e.vert2) {
					return v;
				}
			}
			return null;
		}

		public int getSharedEdgeNo(Tri t2) {
			for (int i = 0; i < edges.length; i++) {
				Edge e = edges[i];
				for (Edge e2 : t2.edges) {
					if (e.hasSameVerts(e2, true)) {
						return i;
					}
				}
			}
			return -1;
		}
	}

	private static class Edge {

		public Vertex vert1;
		public Vertex vert2;

		public Edge(Vertex v0, Vertex v1) {
			vert1 = v0;
			vert2 = v1;
		}

		public boolean hasSameVerts(Edge e2, boolean invWinding) {
			if (invWinding) {
				if (vert2.equals(e2.vert1, true, true, true, true)) {
					return vert1.equals(e2.vert2, true, true, true, true);
				}
			} else {
				if (vert1.equals(e2.vert1, true, true, true, true)) {
					return vert2.equals(e2.vert2, true, true, true, true);
				}
			}
			return false;
		}

		@Override
		public String toString() {
			return vert1.position + "; " + vert2.position;
		}
	}
}
