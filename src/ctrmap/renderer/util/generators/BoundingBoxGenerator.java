package ctrmap.renderer.util.generators;

import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import java.util.ArrayList;
import java.util.List;

public class BoundingBoxGenerator {

	public static Mesh generateBBox(G3DResource resource) {
		return generateBBox(resource, false);
	}

	public static Mesh generateBBox(G3DResource resource, boolean isLines) {
		float x = resource.boundingBox.min.x;
		float y = resource.boundingBox.min.y;
		float z = resource.boundingBox.min.z;
		float w = resource.boundingBox.max.x - resource.boundingBox.min.x;//width
		float d = resource.boundingBox.max.y - resource.boundingBox.min.y;//depth
		float h = resource.boundingBox.max.z - resource.boundingBox.min.z;//height
		return generateBBox(w, h, d, x, y, z, isLines, 1, RGBA.RED);
	}

	public static Mesh generateBBox(Vec3f minVector, Vec3f dimVector, boolean isLines, int lineWidth, RGBA color) {
		return generateBBox(dimVector.x, dimVector.z, dimVector.y, minVector.x, minVector.y, minVector.z, isLines, lineWidth, RGBA.RED);
	}

	public static Mesh generateBBox(float w, float h, float d, boolean centered, boolean isLines, int lineWidth, RGBA color) {
		float x = centered ? -w / 2 : 0;
		float z = centered ? -h / 2 : 0;
		float y = centered ? -d / 2 : 0;
		return generateBBox(w, h, d, x, y, z, isLines, lineWidth, color);
	}

	public static Mesh generateBBox(Mesh mesh, float dimX, float dimZ, float dimY, float x, float y, float z, boolean isLines, int lineWidth, RGBA color) {
		Vertex topLeftU = new Vertex();
		Vertex topRightU = new Vertex();
		Vertex botLeftU = new Vertex();
		Vertex botRightU = new Vertex();
		Vertex topLeftD = new Vertex();
		Vertex topRightD = new Vertex();
		Vertex botLeftD = new Vertex();
		Vertex botRightD = new Vertex();
		topLeftD.position = new Vec3f(x, y, z);
		topRightD.position = new Vec3f(x + dimX, y, z);
		botLeftD.position = new Vec3f(x, y, z + dimZ);
		botRightD.position = new Vec3f(x + dimX, y, z + dimZ);
		topLeftU.position = new Vec3f(x, y + dimY, z);
		topRightU.position = new Vec3f(x + dimX, y + dimY, z);
		botLeftU.position = new Vec3f(x, y + dimY, z + dimZ);
		botRightU.position = new Vec3f(x + dimX, y + dimY, z + dimZ);

		List<Vertex> vl = new ArrayList<>();

		if (isLines) {
			vl.addAll(createVertexList(mesh, topLeftU, topRightU, topRightU, botRightU, botRightU, botLeftU, botLeftU, topLeftU)); //top of the box
			vl.addAll(createVertexList(mesh, topLeftD, topRightD, topRightD, botRightD, botRightD, botLeftD, botLeftD, topLeftD));	//bottom of the box
			vl.addAll(createVertexList(mesh, topLeftU, topLeftD, topRightU, topRightD, botLeftU, botLeftD, botRightU, botRightD)); //lines between the top and bottom
		} else {
			vl.addAll(createVertexList(mesh, botLeftU, botRightU, topRightU, topLeftU)); //top side of the box
			vl.addAll(createVertexList(mesh, topLeftD, topRightD, botRightD, botLeftD)); //bottom side of the box
			vl.addAll(createVertexList(mesh, topLeftU, topRightU, topRightD, topLeftD)); //north side of the box
			vl.addAll(createVertexList(mesh, botRightU, botLeftU, botLeftD, botRightD)); //south side of the box
			vl.addAll(createVertexList(mesh, botLeftU, topLeftU, topLeftD, botLeftD)); //west side of the box
			vl.addAll(createVertexList(mesh, topRightU, botRightU, botRightD, topRightD)); //east side of the box
		}

		setColor(color, vl);

		mesh.vertices.addAll(vl);

		return mesh;
	}

	public static Mesh generateBBox(float dimX, float dimZ, float dimY, float x, float y, float z, boolean isLines, int lineWidth, RGBA color) {
		Mesh mesh = new Mesh();
		mesh.name = (isLines ? "Line" : "") + "Box_" + dimX + "x" + dimZ + "x" + dimY;
		mesh.renderLayer = color.a < 255 ? 1 : 0;
		mesh.hasColor = true;
		mesh.primitiveType = isLines ? PrimitiveType.LINES : PrimitiveType.QUADS;
		if (isLines) {
			mesh.metaData.putValue(ReservedMetaData.LINE_WIDTH, lineWidth);
		}

		generateBBox(mesh, dimX, dimZ, dimY, x, y, z, isLines, lineWidth, color);

		return mesh;
	}

	private static List<Vertex> createVertexList(Mesh mesh, Vertex... vertices) {
		List<Vertex> l = new ArrayList<>();
		for (Vertex v : vertices) {
			l.add(new Vertex(v, mesh));
		}
		return l;
	}

	private static void setColor(RGBA color, List<Vertex> vertices) {
		for (Vertex v : vertices) {
			v.color = color;
		}
	}
}
