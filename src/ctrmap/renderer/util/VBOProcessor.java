package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.util.collections.IntList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class VBOProcessor {

	private static final boolean VBOPROCESSOR_STRICT = true;
	
	public static void makeInline(Mesh mesh, boolean updateVBO) {
		if (mesh.useIBO) {
			List<Vertex> newVtxList = new ArrayList<>();
			HashSet<Vertex> processed = new HashSet<>();
			for (Vertex vtx : mesh) {
				if (processed.contains(vtx)) {
					vtx = new Vertex(vtx, mesh);
				}
				else {
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

	public static void makeIndexed(Mesh mesh, boolean updateVBO) {
		if (!mesh.useIBO) {
			IntList vertexPosHashes = new IntList();
			IntList vertexHashes = new IntList();

			mesh.indices.clear(); //just to be sure

			int posHash;
			int fullHash;
			int idx;
			boolean vtxAdd;
			boolean hashCollision;
			for (int i = 0; i < mesh.vertices.size(); i++) {
				Vertex v = mesh.vertices.get(i);
				posHash = v.position.hashCode();
				fullHash = v.hashCode();
				idx = vertexPosHashes.indexOf(posHash);
				HashCheck:
				while (idx != -1 && vertexHashes.get(idx) != fullHash) {
					for (int j = idx + 1; j < vertexPosHashes.size(); j++) {
						if (vertexPosHashes.get(j) == posHash) {
							idx = j;
							continue HashCheck;
						}
					}
					idx = -1;
				}
				vtxAdd = false;
				if (idx != -1) {
					hashCollision = false;
					if (VBOPROCESSOR_STRICT) {
						if (!v.position.equals(mesh.vertices.get(idx).position)) {
							//HASH COLLISION
							hashCollision = true;
						}
					}
					if (!hashCollision) {
						mesh.indices.add(idx);
						mesh.vertices.remove(i);
						i--;
					} else {
						vtxAdd = true;
					}
				} else {
					vtxAdd = true;
				}
				if (vtxAdd) {
					mesh.indices.add(vertexHashes.size());
					vertexPosHashes.add(posHash);
					vertexHashes.add(fullHash);
				}
			}
			
			mesh.useIBO = true;

			if (updateVBO) {
				mesh.createAndInvalidateBuffers();
			}
		}
	}
}
