package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxPosSet10 extends GECommand {

	public Vec3f position;

	public VtxPosSet10(Vec3f position) {
		this.position = position;
	}

	public VtxPosSet10(DataInput in) throws IOException {
		super(in);
		int bitfld = in.readInt();
		position = new Vec3f(
			FX.unfx10((bitfld >> 0) & 0x3FF, 6),
			FX.unfx10((bitfld >> 10) & 0x3FF, 6),
			FX.unfx10((bitfld >> 20) & 0x3FF, 6)
		);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.VTX_10;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		int param = FX.fx(position.x, 4, 6) | (FX.fx(position.y, 4, 6) << 10) | (FX.fx(position.z, 4, 6) << 20);
		out.writeInt(param);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.vertex(position);
	}
	
	@Override
	public String toString() {
		return "VtxPosSet10" + position;
	}
}
