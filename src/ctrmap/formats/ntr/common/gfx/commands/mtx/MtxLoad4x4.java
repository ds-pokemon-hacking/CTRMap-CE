package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Matrix4;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MtxLoad4x4 extends GECommand {

	public Matrix4 matrix;

	public MtxLoad4x4(Matrix4 matrix) {
		this.matrix = matrix;
	}

	public MtxLoad4x4(DataInput in) throws IOException {
		matrix = new Matrix4();
		float[] arr = new float[4 * 4];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = FXIO.readFX32(in);
		}
		matrix.set(arr);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_LOAD_4X4;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		float[] flt = new float[4 * 4];
		matrix.get(flt);
		FXIO.writeFX32(out, flt);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.loadMatrix4x4(matrix);
	}
}
