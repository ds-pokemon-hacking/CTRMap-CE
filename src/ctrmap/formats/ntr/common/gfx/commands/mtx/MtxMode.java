package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MtxMode extends GECommand {

	public GEMatrixMode mode;

	public MtxMode(GEMatrixMode mode) {
		this.mode = mode;
	}
	
	public MtxMode(DataInput in) throws IOException {
		mode = GEMatrixMode.values()[in.readInt() & 0b11];
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_MODE;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.writeInt(mode.ordinal());
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.matrixMode(mode);
	}

	public static enum GEMatrixMode {
		PROJECTION,
		MODELVIEW,
		MODELVIEW_NORMAL,
		TEXTURE
	}
}
