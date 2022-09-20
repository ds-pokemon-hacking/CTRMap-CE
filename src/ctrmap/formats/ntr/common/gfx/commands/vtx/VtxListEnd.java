package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.GEParamlessCommand;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.IOException;

public class VtxListEnd extends GEParamlessCommand {

	public VtxListEnd() {
		super();
	}

	public VtxListEnd(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.END_VTXS;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.end();
	}
}
