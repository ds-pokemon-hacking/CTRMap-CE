package ctrmap.renderer.backends.base;

import java.awt.Dimension;

public class ViewportInfo {

	public final Dimension surfaceDimensions;
	public final float zNear;
	public final float zFar;

	public ViewportInfo(Dimension surfaceDim, float zNear, float zFar) {
		surfaceDimensions = surfaceDim;
		this.zNear = zNear;
		this.zFar = zFar;
	}

	public int[] getViewportMatrix() {
		return new int[]{0, 0, surfaceDimensions.width, surfaceDimensions.height};
	}

	public float getAspectRatio() {
		return surfaceDimensions.height / (float) surfaceDimensions.width;
	}
}
