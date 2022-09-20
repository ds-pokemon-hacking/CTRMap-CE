package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FX;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Vec2f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxUvSet extends GECommand {

	private Vec2f vec;

	public VtxUvSet(Vec2f vec, Vec2f textureDim) {
		this.vec = new Vec2f(vec.x * textureDim.x, (1f - vec.y) * textureDim.y);
	}

	public VtxUvSet(DataInput in) throws IOException {
		super(in);
		this.vec = new Vec2f(
			FX.unfx(in.readShort(), 11, 4),
			FX.unfx(in.readShort(), 11, 4)
		);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.TEXCOORD;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.writeShort(FX.fx(vec.x, 11, 4));
		out.writeShort(FX.fx(vec.y, 11, 4));
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.texCoord(vec);
	}
}
