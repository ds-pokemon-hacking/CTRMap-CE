package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.FXIO;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.joml.Matrix4x3f;

public class MtxLoad4x3 extends GECommand {

	public Matrix4x3f matrix;

	public MtxLoad4x3(Matrix4x3f matrix) {
		this.matrix = matrix;
	}

	public MtxLoad4x3(DataInput in) throws IOException {
		matrix = new Matrix4x3f();
		float[] arr = new float[4 * 3];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = FXIO.readFX32(in);
		}
		matrix.set(arr);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_LOAD_4X3;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		float[] flt = new float[4 * 3];
		matrix.get(flt);
		FXIO.writeFX32(out, flt);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.loadMatrix4x3(matrix);
	}

}
