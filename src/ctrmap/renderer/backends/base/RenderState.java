
package ctrmap.renderer.backends.base;

import ctrmap.renderer.backends.DriverHandle;

/**
 *
 */
public class RenderState {

	public int flags = 0;
	public int renderLayer = 0;
	public DriverHandle program = null;
	public int lightsHash = -1;

	public RenderState() {
		this(Flag.LAYERED);
	}

	public RenderState(Flag... setupFlags) {
		for (Flag flg : setupFlags) {
			flags |= (1 << flg.ordinal());
		}
	}

	public void setLayer(int l) {
		renderLayer = l;
	}

	public boolean hasFlag(Flag flg) {
		return ((flags >> flg.ordinal()) & 1) > 0;
	}

	public enum Flag {
		LAYERED,
		NO_DRAW,
		OMNI,
		UNTEXTURED
	}

}
