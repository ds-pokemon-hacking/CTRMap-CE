package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxPosSetYZ extends GECommand {

	public float y;
	public float z;

	public VtxPosSetYZ(float y, float z) {
		this.y = y;
		this.z = z;
	}

	public VtxPosSetYZ(DataInput in) throws IOException {
		super(in);
		y = FXIO.readFX16(in);
		z = FXIO.readFX16(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.VTX_YZ;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		FXIO.writeFX16(out, y, z);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.vertexYZ(y, z);
	}
}
