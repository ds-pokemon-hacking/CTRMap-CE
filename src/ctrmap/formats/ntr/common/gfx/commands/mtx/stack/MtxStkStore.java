package ctrmap.formats.ntr.common.gfx.commands.mtx.stack;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.IOException;

public class MtxStkStore extends MtxStkOpBase {

	public MtxStkStore(int targetStkPos) {
		super(targetStkPos);
	}

	public MtxStkStore(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_STORE;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.storeMatrix(pos);
	}

}
