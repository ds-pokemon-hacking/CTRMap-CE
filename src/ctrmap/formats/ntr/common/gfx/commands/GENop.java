package ctrmap.formats.ntr.common.gfx.commands;

import java.io.DataInput;
import java.io.IOException;

public class GENop extends GEParamlessCommand {

	public GENop() {

	}

	public GENop(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.NOP;
	}

	@Override
	public void process(IGECommandProcessor processor) {
	}
}
