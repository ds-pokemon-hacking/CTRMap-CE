package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Quaternion;
import xstandard.math.vec.Vec3f;

public class ModelProcessor {

	public static void upZtoY(Model mdl, boolean updateBuffers) {
		for (Mesh msh : mdl.meshes) {
			for (Vertex vtx : msh.vertices) {
				vecUpZtoY(vtx.position);
			}
			if (updateBuffers) {
				msh.createAndInvalidateBuffers();
			}
		}

		for (Joint bone : mdl.skeleton) {
			if (bone.parentName == null) {
				vecUpZtoY(bone.position);
				Quaternion rot = new Quaternion(bone.rotation);
				rot.rotateLocalX(-MathEx.HALF_PI);
				rot.getEulerRotation(bone.rotation);
			}
		}
		mdl.genBbox();
	}

	private static void vecUpZtoY(Vec3f vec) {
		float y = vec.y;
		vec.y = vec.z;
		vec.z = -y;
	}

	public static void colorToAlpha(Model model) {
		for (Mesh mesh : model.meshes) {
			MeshProcessor.colorToAlpha(mesh);
		}
	}

	public static void clearVCol(Model model) {
		for (Mesh mesh : model.meshes) {
			MeshProcessor.clearVCol(mesh);
		}
	}

	public static void scaleModel(Model model, float factor) {
		Matrix4 scale = Matrix4.createScale(factor, factor, factor);

		for (Mesh mesh : model.meshes) {
			for (Vertex v : mesh.vertices) {
				v.position.mulPosition(scale);
			}
			mesh.createAndInvalidateBuffers();
		}

		model.boundingBox.mul(factor);

		if (model.skeleton != null) {
			for (Joint j : model.skeleton.getJoints()) {
				j.position.x *= factor * j.scale.x;
				j.position.y *= factor * j.scale.y;
				j.position.z *= factor * j.scale.z;
			}

			model.skeleton.buildTransforms();
		}
	}

	public static void translateModel(Model model, Vec3f factor) {
		Matrix4 translation = Matrix4.createTranslation(factor);

		for (Mesh mesh : model.meshes) {
			for (Vertex v : mesh.vertices) {
				v.position.mulPosition(translation);
			}
			mesh.createAndInvalidateBuffers();
		}

		model.genBbox();

		if (model.skeleton != null) {
			for (Joint j : model.skeleton.getJoints()) {
				if (j.parentName == null) {
					j.position.add(factor);
				}
			}

			model.skeleton.buildTransforms();
		}
	}

	public static void updateIndicesOnJointRemoved(Model model, int removedIndex, int replacementIndex) {
		for (Mesh mesh : model.meshes) {
			for (Vertex vtx : mesh.vertices) {
				for (int i = 0; i < vtx.boneIndices.size(); i++) {
					int curVal = vtx.boneIndices.get(i);
					if (curVal > removedIndex) {
						vtx.boneIndices.set(i, curVal - 1);
					} else if (curVal == removedIndex) {
						vtx.boneIndices.set(i, replacementIndex);
					}
				}
			}
			mesh.createAndInvalidateBuffers();
		}
	}

	public static void mergeMeshesByMaterials(Model model) {
		Map<String, List<Mesh>> map = new HashMap<>();
		for (Mesh m : model.meshes) {
			List<Mesh> l = map.get(m.materialName);
			if (l == null) {
				l = new ArrayList<>();
				map.put(m.materialName, l);
			}
			l.add(m);
		}

		List<Mesh> newMeshes = new ArrayList<>();

		for (Map.Entry<String, List<Mesh>> e : map.entrySet()) {
			List<Mesh> ml = e.getValue();

			Mesh allMesh = new Mesh();
			allMesh.materialName = e.getKey();
			allMesh.name = allMesh.materialName + "_mesh";
			allMesh.primitiveType = null;

			for (Mesh config : ml) {
				allMesh.skinningType = config.skinningType;
				allMesh.mergeAttributes(config);

				PrimitiveType pt = null;

				switch (config.primitiveType) {
					case QUADS:
					case QUADSTRIPS:
						pt = PrimitiveType.QUADS;
						break;
					case TRIFANS:
					case TRIS:
					case TRISTRIPS:
						pt = PrimitiveType.TRIS;
						break;
				}

				if (allMesh.primitiveType == null || pt == PrimitiveType.TRIS) { //triangles are the highest possible
					allMesh.primitiveType = pt;
				}
			}

			for (Mesh source : ml) {
				source = allMesh.primitiveType == PrimitiveType.TRIS ? PrimitiveConverter.getTriMesh(source) : PrimitiveConverter.getTriOrQuadMesh(source);
				for (Vertex vtx : source) {
					vtx.ensureMeshCompat(allMesh);
					allMesh.vertices.add(vtx);
				}
			}

			VBOProcessor.makeIndexed(allMesh);

			model.meshes.removeAll(ml);
			newMeshes.add(allMesh);
		}

		newMeshes.sort(new Comparator<Mesh>() {
			@Override
			public int compare(Mesh o1, Mesh o2) {
				return o1.name.compareTo(o2.name);
			}
		});

		model.meshes.addAll(newMeshes);
	}

	public static void smoothSkinningToRigid(Model mdl, boolean updateBuffers) {
		Matrix4[] matrices = new Matrix4[mdl.skeleton.getJointCount()];
		for (int i = 0; i < matrices.length; i++) {
			matrices[i] = mdl.skeleton.getAbsoluteJointBindPoseMatrix(mdl.skeleton.getJoint(i)).invert();
		}
		Outer:
		for (Mesh mesh : mdl.meshes) {
			for (Vertex vtx : mesh.vertices) {
				if (vtx.boneIndices.size() > 1) {
					continue Outer;
				}
			}

			for (Vertex vtx : mesh.vertices) {
				if (!vtx.boneIndices.isEmpty()) {
					vtx.position.mulPosition(matrices[vtx.boneIndices.get(0)]);
				}
			}
			mesh.skinningType = Mesh.SkinningType.RIGID;
			if (updateBuffers) {
				mesh.createAndInvalidateBuffers();
			}
		}
	}

	public static void transplantSkeleton(Model model, Skeleton newSkeleton) {
		transplantSkeleton(model, newSkeleton, false);
	}

	public static void transplantSkeleton(Model model, Skeleton newSkeleton, boolean reuseOldSkeleton) {
		Map<Integer, Integer> jointIDRemap = new HashMap<>();

		Map<String, Integer> newNameToIndex = new HashMap<>();

		for (int i = 0; i < newSkeleton.getJointCount(); ++i) {
			newNameToIndex.put(newSkeleton.getJoint(i).name, i);
		}

		Set<Integer> actuallyUsedBones = new HashSet<>();

		for (Mesh mesh : model.meshes) {
			for (Vertex vtx : mesh.vertices) {
				for (int i = 0; i < vtx.boneIndices.size(); ++i) {
					int boneIdx = vtx.boneIndices.get(i);
					String boneName = model.skeleton.getJoint(boneIdx).name;

					if (!newNameToIndex.containsKey(boneName)) {
						throw new IllegalArgumentException("Joint " + boneName + " was not found.");
					}

					actuallyUsedBones.add(boneIdx);
				}
			}
		}

		for (int i = 0; i < model.skeleton.getJointCount(); ++i) {
			if (!actuallyUsedBones.contains(i)) {
				continue;
			}
			jointIDRemap.put(i, newNameToIndex.get(model.skeleton.getJoint(i).name));
		}

		for (Mesh mesh : model.meshes) {
			for (Vertex vtx : mesh.vertices) {
				for (int i = 0; i < vtx.boneIndices.size(); ++i) {
					vtx.boneIndices.set(i, jointIDRemap.get(vtx.boneIndices.get(i)));
				}
			}
		}

		if (reuseOldSkeleton) {
			model.skeleton.getJoints().clear();
			model.skeleton.addJoints(newSkeleton.getJoints());
		} else {
			model.skeleton = newSkeleton;
		}
	}
}
