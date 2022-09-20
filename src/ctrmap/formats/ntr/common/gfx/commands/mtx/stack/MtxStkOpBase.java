package ctrmap.formats.ntr.common.gfx.commands.mtx.stack;

import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class MtxStkOpBase extends GECommand {

	public int pos;

	public MtxStkOpBase(int pos) {
		this.pos = pos;
	}

	public MtxStkOpBase(DataInput in) throws IOException {
		pos = in.readInt() & 31;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.writeInt(pos & 31);
	}

}
