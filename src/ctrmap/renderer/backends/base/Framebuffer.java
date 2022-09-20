
package ctrmap.renderer.backends.base;

import ctrmap.renderer.backends.DriverHandle;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Framebuffer {
	public DriverHandle name = new DriverHandle();
	public DriverHandle depthName = new DriverHandle();
	
	public List<RenderTarget> renderTargets = new ArrayList<>();
}
