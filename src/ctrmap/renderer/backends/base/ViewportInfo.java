package ctrmap.renderer.backends.base;

import java.awt.Dimension;

public class ViewportInfo {

	public final Dimension surfaceDimensions;

	public ViewportInfo(Dimension surfaceDim) {
		surfaceDimensions = surfaceDim;
	}

	public int[] getViewportMatrix() {
		return new int[]{0, 0, surfaceDimensions.width, surfaceDimensions.height};
	}

	public float getAspectRatio() {
		return surfaceDimensions.width / (float) surfaceDimensions.height;
	}
}
