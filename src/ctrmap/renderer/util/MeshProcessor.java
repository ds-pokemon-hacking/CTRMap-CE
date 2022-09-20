package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.mesh.NormalBufferComponent;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;

public class MeshProcessor {

	public static void transformRigidSkinningToSmooth(Mesh mesh, Skeleton skeleton) {
		transformRigidSkinningToSmooth(mesh, skeleton, false);
	}

	public static void transformRigidSkinningToSmooth(Mesh mesh, Skeleton skeleton, boolean allowMoreIndices) {
		transformRigidSkinningToSmooth(mesh, skeleton, allowMoreIndices, false);
	}

	public static void transformRigidSkinningToSmooth(Mesh mesh, Skeleton skeleton, boolean allowMoreIndices, boolean onlyVerticesWith1Index) {
		if (mesh.skinningType == Mesh.SkinningType.RIGID && !mesh.vertices.isEmpty()) {
			if (!allowMoreIndices) {
				Vertex referenceVtx = mesh.vertices.get(0);
				if (!referenceVtx.boneIndices.isEmpty()) {
					int boneIndex = referenceVtx.boneIndices.get(0);
					Matrix4 transform = skeleton.getAbsoluteJointBindPoseMatrix(skeleton.getJoint(boneIndex));
					for (Vertex v : mesh.vertices) {
						v.position.mulPosition(transform);
						if (v.normal != null) {
							v.normal.mulDirection(transform);
						}
					}
				}
			} else {
				Matrix4[] absoluteJointTransforms = new Matrix4[skeleton.getJoints().size()];
				for (int i = 0; i < absoluteJointTransforms.length; i++) {
					absoluteJointTransforms[i] = skeleton.getAbsoluteJointBindPoseMatrix(skeleton.getJoint(i));
				}
				for (Vertex v : mesh.vertices) {
					if (!v.boneIndices.isEmpty() && (!onlyVerticesWith1Index || v.boneIndices.size() == 1)) {
						int boneIdx = v.boneIndices.get(0);
						Matrix4 transform = absoluteJointTransforms[boneIdx];

						v.position.mulPosition(transform);
						if (v.normal != null) {
							v.normal.mulDirection(transform);
						}
					}
				}
			}

		}
		mesh.skinningType = Mesh.SkinningType.SMOOTH;
	}

	public static void set010Normal(Mesh mesh) {
		for (Vertex v : mesh.vertices) {
			v.normal = new Vec3f(0f, 1f, 0f);
		}
		mesh.hasNormal = true;
		mesh.buffers.vbo.updateComponent(NormalBufferComponent.class);
	}
}
