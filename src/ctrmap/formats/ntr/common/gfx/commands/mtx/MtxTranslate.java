package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.Vec3f;
import java.io.DataInput;
import java.io.IOException;

public class MtxTranslate extends MtxScale {

	public MtxTranslate(Vec3f vec) {
		super(vec);
	}

	public MtxTranslate(float x, float y, float z) {
		super(x, y, z);
	}

	public MtxTranslate(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_TRANSLATE;
	}
	
	@Override
	public void process(IGECommandProcessor processor) {
		processor.translate(vec.x, vec.y, vec.z);
	}
}
