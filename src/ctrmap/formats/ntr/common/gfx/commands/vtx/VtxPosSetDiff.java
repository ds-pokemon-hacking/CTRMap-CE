package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxPosSetDiff extends GECommand {

	public Vec3f difference;

	public VtxPosSetDiff(Vec3f difference) {
		this.difference = difference;
	}

	public VtxPosSetDiff(DataInput in) throws IOException {
		super(in);
		int bitfld = in.readInt();
		difference = new Vec3f(
			FX.unfx10((bitfld >> 0) & 0x3FF, 12),
			FX.unfx10((bitfld >> 10) & 0x3FF, 12),
			FX.unfx10((bitfld >> 20) & 0x3FF, 12)
		);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.VTX_DIFF;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		int param
			= (FX.fx(difference.x, 0, 12) & 0x3FF)
			| ((FX.fx(difference.y, 0, 12) & 0x3FF) << 10)
			| ((FX.fx(difference.z, 0, 12) & 0x3FF) << 20);
		out.writeInt(param);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.vertexDiff(difference);
	}
	
	@Override
	public String toString() {
		return "VtxPosSetDiff" + difference;
	}
}
