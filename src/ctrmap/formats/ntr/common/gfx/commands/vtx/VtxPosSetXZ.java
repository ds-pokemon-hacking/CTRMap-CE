package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxPosSetXZ extends GECommand {

	public float x;
	public float z;

	public VtxPosSetXZ(float x, float z) {
		this.x = x;
		this.z = z;
	}

	public VtxPosSetXZ(DataInput in) throws IOException {
		super(in);
		x = FXIO.readFX16(in);
		z = FXIO.readFX16(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.VTX_XZ;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		FXIO.writeFX16(out, x, z);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.vertexXZ(x, z);
	}
	
	@Override
	public String toString() {
		return "VtxPosSetXZ(" + x + ", " + z + ")";
	}
}
