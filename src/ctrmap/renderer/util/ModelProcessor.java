package ctrmap.renderer.util;

import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
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
				bone.rotation.x += MathEx.HALF_PI;
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
			colorToAlpha(mesh);
		}
	}

	public static void colorToAlpha(Mesh mesh) {
		for (Vertex v : mesh.vertices) {
			v.color = new RGBA(mesh.hasColor ? v.color : RGBA.WHITE);
			v.color.a = (short) MathEx.average(v.color.r, v.color.g, v.color.b);
		}
		mesh.hasColor = true;
		mesh.createAndInvalidateBuffers();
	}

	public static void clearVCol(Model model) {
		for (Mesh mesh : model.meshes) {
			clearVCol(mesh);
		}
	}

	public static void clearVCol(Mesh mesh) {
		mesh.hasColor = true;
		for (Vertex v : mesh.vertices) {
			v.color = new RGBA(RGBA.WHITE);
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
}
