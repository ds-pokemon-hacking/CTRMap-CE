package ctrmap.formats.ntr.common.gfx.commands.mtx.stack;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.GEParamlessCommand;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.IOException;

public class MtxStkPush extends GEParamlessCommand {

	public MtxStkPush() {
		super();
	}

	public MtxStkPush(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_PUSH;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.pushMatrix();
	}

}
