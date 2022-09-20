package ctrmap.renderer.util.generators;

import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.util.MaterialProcessor;
import java.util.ArrayList;
import java.util.List;

public class DashedLineGenerator {

	public static G3DResource generateDashedRectangleModel(float w, float h, float y, float dashSize, float gapSize, float lineWidth, RGBA color, boolean centered) {
		Model mdl = new Model();

		Mesh mesh = generateDashedRectangle(w, h, y, dashSize, gapSize, lineWidth, color, centered);

		Material mat = new Material();
		mat.name = "line_generic";
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.VCOL);
		if (color.a < 255) {
			MaterialProcessor.setAlphaBlend(mat);
		}

		mesh.materialName = mat.name;
		mdl.addMaterial(mat);
		mdl.addMesh(mesh);
		mdl.name = mesh.name + "_mdl";

		return new G3DResource(mdl);
	}

	public static Mesh generateDashedRectangle(float w, float h, float y, float dashSize, float gapSize, float lineWidth, RGBA color, boolean centered) {
		Mesh mesh = new Mesh();
		mesh.hasColor = true;
		mesh.primitiveType = PrimitiveType.LINES;
		mesh.name = "GeneratedDashedRectangle_" + w + "x" + h;
		mesh.metaData.putValue(ReservedMetaData.LINE_WIDTH, lineWidth);

		float xOffs = centered ? -w / 2 : 0;
		float zOffs = centered ? -h / 2 : 0;

		Vec3f topLeft = new Vec3f(xOffs, y, zOffs);
		Vec3f topRight = new Vec3f(xOffs + w, y, zOffs);
		Vec3f bottomLeft = new Vec3f(xOffs, y, zOffs + h);
		Vec3f bottomRight = new Vec3f(xOffs + w, y, zOffs + h);

		mesh.vertices.addAll(generateDashedLine(topLeft, topRight, dashSize, gapSize, color));
		mesh.vertices.addAll(generateDashedLine(topRight, bottomRight, dashSize, gapSize, color));
		mesh.vertices.addAll(generateDashedLine(bottomLeft, bottomRight, dashSize, gapSize, color));
		mesh.vertices.addAll(generateDashedLine(topLeft, bottomLeft, dashSize, gapSize, color));

		return mesh;
	}

	public static List<Vertex> generateDashedLine(Vec3f start, Vec3f end, float dashSize, float gapSize, RGBA color) {
		float step = dashSize + gapSize;
		float vecDist = start.dist(end);
		float count = vecDist / step;
		float stepX = (end.x - start.x) / count;
		float stepY = (end.y - start.y) / count;
		float stepZ = (end.z - start.z) / count;

		float dashRatio = dashSize / (dashSize + gapSize);

		List<Vertex> vertexBuffer = new ArrayList<>();

		for (float i = 0; i < count; i++) {
			Vertex dashStart = new Vertex();
			Vertex dashEnd = new Vertex();
			dashStart.color = color;
			dashEnd.color = color;
			dashStart.position = new Vec3f();
			dashEnd.position = new Vec3f();

			dashStart.position.x = start.x + stepX * i;
			dashStart.position.y = start.y + stepY * i;
			dashStart.position.z = start.z + stepZ * i;

			dashEnd.position.x = dashStart.position.x + stepX * dashRatio;
			dashEnd.position.y = dashStart.position.y + stepY * dashRatio;
			dashEnd.position.z = dashStart.position.z + stepZ * dashRatio;

			if (i == count - 1) {
				//Trim the dashes not to go beyond the final vertex
				dashEnd.position.min(end);
			}

			vertexBuffer.add(dashStart);
			vertexBuffer.add(dashEnd);
		}

		return vertexBuffer;
	}
}
