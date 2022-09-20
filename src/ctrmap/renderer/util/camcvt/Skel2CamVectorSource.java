
package ctrmap.renderer.util.camcvt;

public class Skel2CamVectorSource extends Skel2CamSource {
	
	public Skel2CamVectorOp op;
	
	public static enum Skel2CamVectorOp {
		NONE,
		ROTX_HPINEG,
		ROTX_HPIPOS
	}
}
