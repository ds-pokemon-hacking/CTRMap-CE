
package ctrmap.renderer.util.camcvt;

public class Skel2CamScalarSource extends Skel2CamSource {
	
	public Skel2CamScalarComp comp;
	public Skel2CamScalarOp op;
	
	public static enum Skel2CamScalarComp {
		X,
		Y,
		Z
	}
	
	public static enum Skel2CamScalarOp {
		NONE,
		NEGATE,
		ADD_HPI,
		SUB_HPI
	}
}
