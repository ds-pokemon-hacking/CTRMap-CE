package ctrmap.renderer.util.generators;

import ctrmap.renderer.scene.model.Joint;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.math.MatrixUtil;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceType;
import xstandard.math.vec.Matrix4;
import java.util.ArrayList;
import java.util.List;

public class SkeletonModelGenerator {

	private static final Material skeletonMaterial = new Material();
	
	private static final String SKL_MAT_NAME = "SkeletonRender";
	
	private static final String SKL_MDL_NAME = "SkeletonModel";
	private static final String SKL_MESH_NAME_JOINT = "JointCubes";
	private static final String SKL_MESH_NAME_CONNECT = "JointCubeConnectors";

	static {
		skeletonMaterial.name = SKL_MAT_NAME;
		skeletonMaterial.depthColorMask.depthFunction = MaterialParams.TestFunction.ALWAYS;
		skeletonMaterial.tevStages.stages[0] = new TexEnvStage();
		skeletonMaterial.tevStages.stages[0].rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
		skeletonMaterial.tevStages.stages[0].alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
		skeletonMaterial.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		skeletonMaterial.constantColors[0] = new RGBA(252, 233, 3, 255);
	}

	public static Model generateSkeletonModel(Skeleton src, G3DResource model) {
		Model sklMdl = new Model();
		if (src == null) {
			return sklMdl;
		}
		sklMdl.name = SKL_MDL_NAME;
		sklMdl.addMaterial(skeletonMaterial);
		sklMdl.skeleton = src;

		Mesh jointM = new Mesh();
		Mesh connectM = new Mesh();
		jointM.name = SKL_MESH_NAME_JOINT;
		connectM.name = SKL_MESH_NAME_CONNECT;
		jointM.materialName = SKL_MAT_NAME;
		connectM.materialName = SKL_MAT_NAME;
		jointM.primitiveType = PrimitiveType.LINES;
		connectM.primitiveType = PrimitiveType.LINES;
		jointM.hasBoneIndices = true;
		jointM.hasBoneWeights = true;
		connectM.hasBoneIndices = true;
		connectM.hasBoneWeights = true;

		updateSkeletonModelMeshes(jointM, connectM, src);

		sklMdl.addMesh(jointM);
		sklMdl.addMesh(connectM);
		return sklMdl;
	}
	
	private static void updateSkeletonModelMeshes(Mesh jointM, Mesh connectM, Skeleton skeleton) {
		Vec3f vec000 = Vec3f.ZERO();

		Mesh jointMeshBase = BoundingBoxGenerator.generateBBox(1f, 1f, 1f, true, true, 1, RGBA.WHITE);
		List<Vertex> jointBaseVertices = jointMeshBase.vertices;

		float lastMul = 1f;
		
		jointM.vertices.clear();
		connectM.vertices.clear();
		
		for (Joint j : skeleton) {
			List<Vertex> vb = new ArrayList<>();
			int jointIndex = j.getIndex();
			Matrix4 transform = skeleton.getAbsoluteJointBindPoseMatrix(j);
			for (Vertex base : jointBaseVertices) {
				Vertex out = new Vertex();
				float mul = j.position.getHighestAbsComponent() * 0.1f;
				if (mul == 0f) {
					mul = lastMul;
				}
				lastMul = mul;
				out.position = base.position.clone().mul(mul);
				out.position.mulPosition(transform);
				out.boneIndices.add(jointIndex);
				out.weights.add(1f);
				vb.add(out);
			}
			jointM.vertices.addAll(vb);

			//Create the line between this and the parent
			if (j.parentName != null) {
				Joint parent = j.getParent();
				if (parent != null) {
					Vertex p = new Vertex();
					Vertex t = new Vertex();
					skeleton.getAbsoluteJointBindPoseMatrix(parent).getTranslation(p.position);
					transform.getTranslation(t.position);
					p.boneIndices.add(parent.getIndex());
					p.weights.add(1f);
					t.boneIndices.add(jointIndex);
					t.weights.add(1f);
					connectM.vertices.add(t);
					connectM.vertices.add(p);
				}
			}
		}
	}
	
	public static void updateSkeletonModel(G3DResource skeletonModel) {
		Model mdl = (Model) skeletonModel.getNamedResource("SkeletonModel", G3DResourceType.MODEL);
		if (mdl != null) {
			Mesh jointM = mdl.getMeshByName(SKL_MESH_NAME_JOINT);
			Mesh connectM = mdl.getMeshByName(SKL_MESH_NAME_CONNECT);
			if (jointM != null && connectM != null) {
				updateSkeletonModelMeshes(jointM, connectM, mdl.skeleton);
				jointM.createAndInvalidateBuffers();
				connectM.createAndInvalidateBuffers();
			}
		}
	}
}
