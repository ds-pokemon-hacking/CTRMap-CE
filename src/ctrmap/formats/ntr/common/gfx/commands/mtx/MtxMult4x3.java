
package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import org.joml.Matrix4x3f;

public class MtxMult4x3 extends MtxLoad4x3 {
	public MtxMult4x3(Matrix4x3f matrix) {
		super(matrix);
	}
	
	@Override
	public GEOpCode getOpCode(){
		return GEOpCode.MTX_MULT_4X3;
	}
	
	@Override
	public void process(IGECommandProcessor processor) {
		processor.loadMatrix4x3(matrix);
	}
}
