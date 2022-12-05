package ctrmap.renderer.scene.model;

import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.util.Arrays;
import java.util.Objects;

public class Vertex {

	public Vec3f position = Vec3f.ZERO();
	public RGBA color = RGBA.WHITE;
	public Vec2f[] uv = new Vec2f[3];

	public Vec3f normal = null;
	public Vec3f tangent = null;

	public IntList boneIndices = new IntList();
	public FloatList weights = new FloatList();

	public Vertex() {

	}

	public Vertex(Vertex vtx, Mesh mesh) {
		position = new Vec3f(vtx.position);
		if (mesh.hasColor) {
			color = new RGBA(vtx.color);
		}
		for (int i = 0; i < 3; i++){
			if (mesh.hasUV(i)){
				uv[i] = new Vec2f(vtx.uv[i]);
			}
		}
		if (mesh.hasNormal) {
			if (vtx.normal == null) {
				normal = new Vec3f(0, 1, 0);
			} else {
				normal = new Vec3f(vtx.normal);
			}
		}
		if (mesh.hasBoneIndices) {
			boneIndices.addAll(vtx.boneIndices);
		}
		if (mesh.hasBoneWeights) {
			weights.addAll(vtx.weights);
		}
	}
	
	public void ensureMeshCompat(Mesh mesh) {
		if (mesh.hasNormal && normal == null) {
			normal = new Vec3f(0f, 0f, 1f);
		}
		if (mesh.hasColor && color == null) {
			color = RGBA.WHITE.clone();
		}
		for (int i = 0; i < uv.length; i++) {
			if (mesh.hasUV(i) && uv[i] == null) {
				uv[i] = Vec2f.ZERO();
			}
		}
		if (mesh.hasTangent && tangent == null) {
			tangent = new Vec3f(0f, 1f, 0f);
		}
		//indices and weights needn't be filled as they are lists
	}
	
	public int getActiveWeightCount() {
		for (int i = weights.size() - 1; i >= 0; i--) {
			if (weights.get(i) != 0f) {
				return Math.min(boneIndices.size(), i + 1);
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(this.position);
		hash = 37 * hash + Objects.hashCode(this.color);
		hash = 37 * hash + Arrays.deepHashCode(this.uv);
		hash = 37 * hash + Objects.hashCode(this.normal);
		hash = 37 * hash + Objects.hashCode(this.tangent);
		hash = 37 * hash + Objects.hashCode(this.boneIndices);
		hash = 37 * hash + Objects.hashCode(this.weights);
		return hash;
	}

	public boolean equals(Vertex v, boolean p, boolean c, boolean uv, boolean n) {
		if (p && !v.position.equals(position)) {
			return false;
		}
		if (c && !v.color.equals(color)) {
			return false;
		}
		if (n && !Objects.equals(v.normal, normal)) {
			return false;
		}
		if (uv && !Arrays.equals(this.uv, v.uv)) {
			return false;
		}
		return true;
	}
}
