package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.IOException;
import xstandard.math.vec.Matrix4;

public class MtxMult4x4 extends MtxLoad4x4 {

	public MtxMult4x4(Matrix4 matrix) {
		super(matrix);
	}

	public MtxMult4x4(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_MULT_4X4;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.multMatrix4x4(matrix);
	}
}
