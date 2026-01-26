package ctrmap.renderer.backends.base;

import java.awt.Dimension;
import java.awt.Point;
import xstandard.math.vec.Vec2f;

public class ViewportInfo {

	public final Dimension surfaceDimensions;
	public final Vec2f clientPixelScale;

	public ViewportInfo(Dimension surfaceDim, Vec2f clientPixelScale) {
		this.surfaceDimensions = surfaceDim;
		this.clientPixelScale = clientPixelScale;
	}

	public int[] getViewportMatrix() {
		return new int[]{0, 0, surfaceDimensions.width, surfaceDimensions.height};
	}

	public float getAspectRatio() {
		return surfaceDimensions.width / (float) surfaceDimensions.height;
	}

	public Point clientToSurfacePixel(Point point) {
		return transformPixel(point, clientPixelScale.x, clientPixelScale.y);
	}

	public Point surfaceToClientPixel(Point point) {
		return transformPixel(point, 1f / clientPixelScale.x, 1f / clientPixelScale.y);
	}

	private static Point transformPixel(Point point, float sx, float sy) {
		return new Point((int) Math.round(point.getX() * sx), (int) Math.round(point.getY() * sy));
	}
}
