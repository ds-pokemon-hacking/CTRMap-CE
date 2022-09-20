package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Matrix3f;

public class MtxMult3x3 extends GECommand {

	public Matrix3f matrix;

	public MtxMult3x3(Matrix3f matrix) {
		this.matrix = matrix;
	}

	public MtxMult3x3(DataInput in) throws IOException {
		matrix = new Matrix3f();
		float[] arr = new float[3 * 3];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = FXIO.readFX32(in);
		}
		matrix.set(arr);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_MULT_3x3;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		float[] flt = new float[3 * 3];
		matrix.get(flt);
		FXIO.writeFX32(out, flt);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.multMatrix3x3(matrix);
	}

}
