package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class MtxScale extends GECommand {

	public Vec3f vec;

	public MtxScale(Vec3f vec) {
		this.vec = vec;
	}

	public MtxScale(float x, float y, float z) {
		this.vec = new Vec3f(x, y, z);
	}

	public MtxScale(DataInput in) throws IOException {
		vec = FXIO.readVecFX32(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_SCALE;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		FXIO.writeVecFX32(out, vec);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.scale(vec.x, vec.y, vec.z);
	}

}
