package ctrmap.formats.ntr.common.gfx.commands.mtx.stack;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.IOException;

/**
 *
 */
public class MtxStkPop extends MtxStkOpBase {

	public MtxStkPop(int count) {
		super(count);
	}

	public MtxStkPop(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_POP;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.popMatrix(pos);
	}
}
