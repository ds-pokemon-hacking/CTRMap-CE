package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.vtxlist.AbstractVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import xstandard.util.collections.IntList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import xstandard.math.vec.Vec3f;

public class VBOProcessor {

	public static void makeInline(Mesh mesh, boolean updateVBO) {
		if (mesh.useIBO) {
			List<Vertex> newVtxList = new ArrayList<>();
			HashSet<Vertex> processed = new HashSet<>();
			for (Vertex vtx : mesh) {
				if (processed.contains(vtx)) {
					vtx = new Vertex(vtx, mesh);
				} else {
					processed.add(vtx);
				}
				newVtxList.add(vtx);
			}
			mesh.vertices.clear();
			mesh.vertices.addAll(newVtxList);
			if (updateVBO) {
				mesh.createAndInvalidateBuffers();
			}
			mesh.useIBO = false;
			mesh.indices.clear();
		}
	}

	public static void makeIndexed(Mesh mesh) {
		makeIndexed(mesh, true);
	}

	public static int[] createIndexMap(boolean positionOnly, AbstractVertexList... vtxSources) {
		Map<Object, Integer> indexMap = new HashMap<>();

		int vtxMax = 0;
		for (AbstractVertexList l : vtxSources) {
			if (l.size() > vtxMax) {
				vtxMax = l.size();
			}
		}
		int[] result = new int[vtxMax];

		int ascIndex = 0;
		int outIndex;
		for (int i = 0; i < vtxMax; i++) {
			Object k = positionOnly ? new VertexPosKey(vtxSources, i) : new VertexSetKey(vtxSources, i);
			outIndex = indexMap.getOrDefault(k, -1);
			if (outIndex == -1) {
				indexMap.put(k, ascIndex);
				result[i] = ascIndex;
				ascIndex++;
			} else {
				result[i] = outIndex;
			}
		}

		return result;
	}

	public static void makeIndexed(Mesh mesh, boolean updateVBO) {
		if (!mesh.useIBO) {
			Map<VertexSetKey, Integer> indexMap = new HashMap<>();

			mesh.indices.clear(); //just to be sure

			AbstractVertexList[] vtxSources = mesh.getVertexArrays();

			int vtxMax = 0;
			for (AbstractVertexList l : vtxSources) {
				if (l.size() > vtxMax) {
					vtxMax = l.size();
				}
			}

			int ascIndex = 0;
			int outIndex;
			for (int i = 0; i < vtxMax;) {
				VertexSetKey k = new VertexSetKey(vtxSources, i);
				outIndex = indexMap.getOrDefault(k, -1);
				if (outIndex == -1) {
					mesh.indices.add(ascIndex);
					indexMap.put(k, ascIndex);
					ascIndex++;
					i++;
				} else {
					mesh.indices.add(outIndex);
					for (AbstractVertexList l : vtxSources) {
						l.remove(i);
					}
					vtxMax--;
				}
			}

			mesh.useIBO = true;

			if (updateVBO) {
				mesh.createAndInvalidateBuffers();
			}
		}
	}

	private static class VertexSetKey {

		private final Vertex[] verts;

		public VertexSetKey(AbstractVertexList[] src, int index) {
			this.verts = new Vertex[src.length];
			for (int i = 0; i < verts.length; i++) {
				verts[i] = src[i].get(index);
			}
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(verts);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			final VertexSetKey other = (VertexSetKey) obj;
			return Arrays.equals(this.verts, other.verts);
		}
	}
	
	private static class VertexPosKey {

		private final Vec3f[] positions;
		private final Vec3f[] normals;
		private final Vec3f[] tangents;

		public VertexPosKey(AbstractVertexList[] src, int index) {
			this.positions = new Vec3f[src.length];
			this.normals = new Vec3f[src.length];
			this.tangents = new Vec3f[src.length];
			for (int i = 0; i < src.length; i++) {
				positions[i] = src[i].get(index).position;
				normals[i] = src[i].get(index).normal;
				tangents[i] = src[i].get(index).tangent;
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(Arrays.hashCode(positions), Arrays.hashCode(normals), Arrays.hashCode(tangents));
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			final VertexPosKey other = (VertexPosKey) obj;
			return Arrays.equals(this.positions, other.positions) 
				&& Arrays.equals(this.normals, other.normals) 
				&& Arrays.equals(this.tangents, other.tangents);
		}
	}
}
