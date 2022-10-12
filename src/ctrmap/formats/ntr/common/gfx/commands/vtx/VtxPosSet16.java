package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxPosSet16 extends GECommand {

	public Vec3f position;

	public VtxPosSet16(Vec3f position) {
		this.position = position;
	}

	public VtxPosSet16(DataInput in) throws IOException {
		super(in);
		position = new Vec3f(FXIO.readFX16(in), FXIO.readFX16(in), FXIO.readFX16(in));
		in.readShort();
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.VTX_16;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		FXIO.writeVecFX32(out, position);
		out.writeShort(0);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.vertex(position);
	}
	
	@Override
	public String toString() {
		return "VtxPosSet16" + position;
	}
}
