package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.mesh.NormalBufferComponent;
import java.util.ArrayList;
import java.util.List;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec2f;
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

	//https://gamedev.stackexchange.com/questions/68612/how-to-compute-tangent-and-bitangent-vectors
	public static void calculateTangents(Mesh mesh, int uvSet) {
		if (mesh.hasUV(uvSet) && mesh.primitiveType == PrimitiveType.TRIS) {
			mesh.hasTangent = false;
			//reindex, but ignore tangents
			VBOProcessor.makeInline(mesh, false);
			VBOProcessor.makeIndexed(mesh, false);
			mesh.hasTangent = true;
			
			List<Vertex> verticesUnindexed = new ArrayList<>();
			for (Vertex v : mesh) {
				verticesUnindexed.add(v);
			}
			Vertex[] triVertices = new Vertex[3];

			Vec3f edge1 = new Vec3f();
			Vec3f edge2 = new Vec3f();
			Vec2f deltaUV1 = new Vec2f();
			Vec2f deltaUV2 = new Vec2f();
			Vec3f tangent = new Vec3f();
			
			for (Vertex v : verticesUnindexed) {
				v.tangent = new Vec3f();
			}

			for (int ti = 0; ti < verticesUnindexed.size(); ti += 3) {
				triVertices[0] = verticesUnindexed.get(ti);
				triVertices[1] = verticesUnindexed.get(ti + 1);
				triVertices[2] = verticesUnindexed.get(ti + 2);

				triVertices[1].position.sub(triVertices[0].position, edge1);
				triVertices[2].position.sub(triVertices[0].position, edge2);
				triVertices[1].uv[uvSet].sub(triVertices[0].uv[uvSet], deltaUV1);
				triVertices[2].uv[uvSet].sub(triVertices[0].uv[uvSet], deltaUV2);

				float r = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV1.y * deltaUV2.x);
				tangent.set(
					edge1.x * deltaUV2.y - edge2.x * deltaUV1.y,
					edge1.y * deltaUV2.y - edge2.y * deltaUV1.y,
					edge1.z * deltaUV2.y - edge2.z * deltaUV1.y
				);
				tangent.mul(r);

				for (Vertex tv : triVertices) {
					tv.tangent.add(tangent);
				}
			}
			
			Vec3f temp = new Vec3f();
			for (Vertex v : mesh.vertices) {
				v.normal.mul(v.normal.dot(v.tangent), temp);
				v.tangent.sub(temp);
				v.tangent.normalize();
			}
			
			mesh.createAndInvalidateBuffers();
		}
	}
}
