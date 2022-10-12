package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxPosSetXY extends GECommand {

	public float x;
	public float y;

	public VtxPosSetXY(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public VtxPosSetXY(DataInput in) throws IOException {
		super(in);
		x = FXIO.readFX16(in);
		y = FXIO.readFX16(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.VTX_XY;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		FXIO.writeFX16(out, x, y);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.vertexXY(x, y);
	}
	
	@Override
	public String toString() {
		return "VtxPosSetXY(" + x + ", " + y + ")";
	}
}
