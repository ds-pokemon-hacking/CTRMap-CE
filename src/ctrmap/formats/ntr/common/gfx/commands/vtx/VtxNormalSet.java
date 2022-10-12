package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.MathEx;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxNormalSet extends GECommand {

	public Vec3f normal;

	public VtxNormalSet(Vec3f normal) {
		this.normal = normal;
	}

	public VtxNormalSet(DataInput in) throws IOException {
		super(in);
		int bitfld = in.readInt();
		normal = new Vec3f(
			FX.unfx10((bitfld >> 0) & 0x3FF, 9),
			FX.unfx10((bitfld >> 10) & 0x3FF, 9),
			FX.unfx10((bitfld >> 20) & 0x3FF, 9)
		);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.NORMAL;
	}

	private static final float FX9_MAX = 511f / 512f;

	private float limitNrmVal(float val) {
		return MathEx.clamp(-1f, FX9_MAX, val);
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		int param = FX.fx(limitNrmVal(normal.x), 1, 9) | (FX.fx(limitNrmVal(normal.y), 1, 9) << 10) | (FX.fx(limitNrmVal(normal.z), 1, 9) << 20);
		out.writeInt(param);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.normal(normal);
	}
	
	@Override
	public String toString() {
		return "VtxNormalSet" + normal;
	}
}
