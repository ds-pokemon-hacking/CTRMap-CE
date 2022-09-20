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
import java.util.List;

public class GridGenerator {

	public static G3DResource generateGrid(float step, float y, int dim, int lineWidth, RGBA color) {
		return generateGrid(step, y, dim, lineWidth, color, false);
	}

	public static G3DResource generateGrid(float step, float y, int dim, int lineWidth, RGBA color, boolean centered) {
		return new G3DResource(generateGridModel(step, y, dim, lineWidth, color, centered, false));
	}

	public static Model generateGridModel(float step, float y, int dim, int lineWidth, RGBA color, boolean centered, boolean invertYZ) {
		Mesh mesh = new Mesh();
		Material mat = new Material();
		mat.name = "grid_generic";
		mat.tevStages.stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.VCOL);
		if (color.a < 255) {
			MaterialProcessor.setAlphaBlend(mat);
		}

		mesh.renderLayer = 2;
		mesh.name = "Grid";
		mesh.materialName = mat.name;
		mesh.hasColor = true;
		mesh.primitiveType = PrimitiveType.LINES;
		mesh.metaData.putValue(ReservedMetaData.LINE_WIDTH, lineWidth);
		List<Vertex> buffer = mesh.vertices;

		float left = 0;
		float top = 0;
		if (centered) {
			float halfwidth = dim / 2f * step;
			left -= halfwidth;
			top -= halfwidth;
		}
		float right = step * dim + left;
		float bottom = step * dim + top;

		for (int x = 0; x <= dim; x++) {
			float xPos = left + step * x;

			Vertex topV = new Vertex();
			topV.position = new Vec3f(xPos, invertYZ ? top : y, invertYZ ? y : top);
			topV.color = color;
			Vertex bottomV = new Vertex();
			bottomV.position = new Vec3f(xPos, invertYZ ? bottom : y, invertYZ ? y : bottom);
			bottomV.color = color;

			buffer.add(topV);
			buffer.add(bottomV);
		}

		for (int z = 0; z <= dim; z++) {
			float zPos = left + step * z;

			Vertex leftV = new Vertex();
			leftV.position = new Vec3f(left, invertYZ ? zPos : y, invertYZ ? y : zPos);
			leftV.color = color;
			Vertex rightV = new Vertex();
			rightV.position = new Vec3f(right, invertYZ ? zPos : y, invertYZ ? y : zPos);
			rightV.color = color;

			buffer.add(leftV);
			buffer.add(rightV);
		}

		Model m = new Model();
		m.name = "GeneratedGrid_" + dim + "x" + dim + ":" + step;
		m.addMaterial(mat);
		m.addMesh(mesh);
		return m;
	}
}
