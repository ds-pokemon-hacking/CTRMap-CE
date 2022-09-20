package ctrmap.renderer.util;

import com.jogamp.opengl.glu.gl2.GLUgl2;
import xstandard.math.vec.Vec3f;
import java.awt.event.MouseEvent;

public class ConstYPlaneRayCaster {

	public static Vec3f getZeroPlaneIntersectMouse(MouseEvent e, float[] mvMatrix, float[] projMatrix, int[] viewMatrix) {
		return getConstYPlaneIntersectMouse(e, 0f, mvMatrix, projMatrix, viewMatrix);
	}

	public static Vec3f getConstYPlaneIntersectMouse(MouseEvent e, float constPlaneHeight, float[] mvMatrix, float[] projMatrix, int[] viewMatrix) {
		GLUgl2 glu = new GLUgl2();
		float[] origin = new float[3];
		float[] target = new float[3];
		glu.gluUnProject(e.getX(), viewMatrix[3] - e.getY(), 0, mvMatrix, 0, projMatrix, 0, viewMatrix, 0, origin, 0);
		glu.gluUnProject(e.getX(), viewMatrix[3] - e.getY(), 1f, mvMatrix, 0, projMatrix, 0, viewMatrix, 0, target, 0);
		Vec3f originV = new Vec3f(origin);
		Vec3f dir = new Vec3f(target[0] - origin[0], target[1] - origin[1], target[2] - origin[2]);
		float t = (constPlaneHeight - originV.y) / dir.y;
		float x = t * dir.x + originV.x;
		float z = t * dir.z + originV.z;
		return new Vec3f(x, constPlaneHeight, z);
	}
}
