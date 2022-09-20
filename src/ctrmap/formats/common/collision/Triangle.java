package ctrmap.formats.common.collision;

import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.PrimitiveType;
import ctrmap.renderer.scene.model.Vertex;
import xstandard.math.FAtan;
import xstandard.math.geom.Trianglef;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class used for storing vertex data in OOP triangles, mainly used for
 * accessibility in the Collision editor (as it edits them at quite a low
 * level).
 */
public class Triangle extends Trianglef {

	public Triangle(Vertex[] vertices){
		Vertex v0 = vertices[0];
		Vertex v1 = vertices[1];
		Vertex v2 = vertices[2];
		x = new float[]{v0.position.x, v1.position.x, v2.position.x};
		y = new float[]{v0.position.y, v1.position.y, v2.position.y};
		z = new float[]{v0.position.z, v1.position.z, v2.position.z};
	}
	
	public Triangle(float[] x, float[] y, float[] z) {
		super(x, y, z);
	}
	
	public Triangle(List<Vertex> vertices, int verticesOffset) {
		Vertex v0 = vertices.get(verticesOffset);
		Vertex v1 = vertices.get(verticesOffset + 1);
		Vertex v2 = vertices.get(verticesOffset + 2);
		x = new float[]{v0.position.x, v1.position.x, v2.position.x};
		y = new float[]{v0.position.y, v1.position.y, v2.position.y};
		z = new float[]{v0.position.z, v1.position.z, v2.position.z};
	}

	public Triangle(DataInput in, boolean useVec4) throws IOException {
		for (int i = 0; i < 3; i++) {
			x[i] = in.readFloat();
			y[i] = in.readFloat();
			z[i] = in.readFloat();
			if (useVec4) {
				in.skipBytes(4);
			}
		}
	}

	public static List<Triangle> meshToTriangles(Mesh mesh) {
		List<Triangle> l = new ArrayList<>();
		if (mesh.primitiveType == PrimitiveType.TRIS) {
			for (int i = 0; i < mesh.vertices.size(); i += 3) {
				l.add(new Triangle(mesh.vertices, i));
			}
		}
		return l;
	}

	@Override
	public String toString() {
		return new Vec3f(x[0], y[0], z[0]).toString() + "; " + new Vec3f(x[1], y[1], z[1]).toString() + "; " + new Vec3f(x[2], y[2], z[2]).toString();
	}

	public void makeCounterClockwise() {
		List<float[]> vertexList = new ArrayList<>();
		vertexList.add(new float[]{getX(0), getY(0), getZ(0)});
		vertexList.add(new float[]{getX(1), getY(1), getZ(1)});
		vertexList.add(new float[]{getX(2), getY(2), getZ(2)});
		float centerX = (vertexList.get(0)[0] + vertexList.get(1)[0] + vertexList.get(2)[0]) / 3f;
		float centerZ = (vertexList.get(0)[2] + vertexList.get(1)[2] + vertexList.get(2)[2]) / 3f;
		Collections.sort(vertexList, (float[] o1, float[] o2) -> {
			double baseAngle1 = (Math.toDegrees(FAtan.atan2(o1[2] - centerZ, o1[0] - centerX)) + 360) % 360;
			double baseAngle2 = (Math.toDegrees(FAtan.atan2(o2[2] - centerZ, o2[0] - centerX)) + 360) % 360;
			return (int) (baseAngle2 - baseAngle1);
		});
		float[] v0 = vertexList.get(0);
		float[] v1 = vertexList.get(1);
		float[] v2 = vertexList.get(2);
		x = new float[]{v0[0], v1[0], v2[0]};
		y = new float[]{v0[1], v1[1], v2[1]};
		z = new float[]{v0[2], v1[2], v2[2]};
	}
	
	public void write(DataOutput dos, boolean writeAsVec4) {
		try {
			for (int i = 0; i < x.length; i++) {
				dos.writeFloat(x[i]);
				dos.writeFloat(y[i]);
				dos.writeFloat(z[i]);
				if (writeAsVec4) {
					dos.writeFloat(1);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Line2D getLine(int num) {
		return new Line2D.Float(x[num], z[num], x[(num == 2) ? 0 : num + 1], z[(num == 2) ? 0 : num + 1]);
	}

	public Polygon getAWTPoly() {
		return new Polygon(new int[]{(int) x[0], (int) x[1], (int) x[2]}, new int[]{(int) z[0], (int) z[1], (int) z[2]}, 3);
	}

	public List<Vertex> getVertices() {
		List<Vertex> l = new ArrayList<>();
		for (int i = 0; i < 3; i++) {
			Vertex v = new Vertex();
			v.position = new Vec3f(x[i], y[i], z[i]);
			l.add(v);
		}
		return l;
	}
}
