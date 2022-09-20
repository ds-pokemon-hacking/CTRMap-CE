package ctrmap.renderer.util.generators;

import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.math.vec.RGBA;

public class PlaneGenerator {
	
	public static G3DResource generateQuadPlaneRsc(float w, float h, float y, boolean centered, boolean invertYZ, RGBA color) {
		return new G3DResource(generateQuadPlaneModel(w, h, y, centered, invertYZ, color));
	}
	
	public static Model generateQuadPlaneModel(float w, float h, float y, boolean centered, boolean invertYZ, RGBA color) {
		Model mdl = new Model();
		Mesh mesh = generateQuadPlaneMesh(w, h, y, centered, invertYZ);
		mdl.addMesh(mesh);
		Material mat = new Material();
		mat.name = "QuadPlane";
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.CCOL);
		mat.tevStages.stages[0].constantColor = MaterialColorType.CONSTANT0;
		mat.constantColors[0] = color;
		mesh.materialName = mat.name;
		mdl.addMaterial(mat);
		return mdl;
	}
	
	public static Mesh generateQuadPlaneMesh(float w, float h, float y, boolean centered) {
		return generateQuadPlaneMesh(w, h, y, centered, false);
	}

	public static Mesh generateQuadPlaneMesh(float w, float h, float y, boolean centered, boolean invertYZ) {
		float x = centered ? -w / 2 : 0;
		float z = centered ? -h / 2 : 0;
		Vertex topLeft = new Vertex();
		Vertex topRight = new Vertex();
		Vertex bottomLeft = new Vertex();
		Vertex bottomRight = new Vertex();
		if (!invertYZ) {
			topLeft.position = new Vec3f(x, y, z);
			topRight.position = new Vec3f(x + w, y, z);
			bottomLeft.position = new Vec3f(x, y, z + h);
			bottomRight.position = new Vec3f(x + w, y, z + h);
		} else {
			topLeft.position = new Vec3f(x, z, y);
			topRight.position = new Vec3f(x + w, z, y);
			bottomLeft.position = new Vec3f(x, z + h, y);
			bottomRight.position = new Vec3f(x + w, z + h, y);
		}
		topLeft.uv[0] = new Vec2f(0, 1f);
		topRight.uv[0] = new Vec2f(1f, 1f);
		bottomLeft.uv[0] = new Vec2f(0, 0f);
		bottomRight.uv[0] = new Vec2f(1f, 0f);

		Mesh mesh = new Mesh();
		mesh.primitiveType = PrimitiveType.QUADS;
		mesh.hasUV[0] = true;
		mesh.name = (centered ? "Centered" : "") + "Plane_" + w + "x" + h;
		mesh.vertices.add(bottomLeft);
		mesh.vertices.add(bottomRight);
		mesh.vertices.add(topRight);
		mesh.vertices.add(topLeft);

		return mesh;
	}
}
