package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import ctrmap.renderer.scene.model.PrimitiveType;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxListBegin extends GECommand {

	public PrimitiveType primitiveType;

	public VtxListBegin(PrimitiveType primitiveType) {
		this.primitiveType = primitiveType;
	}

	public VtxListBegin(DataInput in) throws IOException {
		super(in);
		PrimitiveType[] LUT = new PrimitiveType[]{
			PrimitiveType.TRIS,
			PrimitiveType.QUADS,
			PrimitiveType.TRISTRIPS,
			PrimitiveType.QUADSTRIPS
		};
		primitiveType = LUT[in.readInt()];
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.BEGIN_VTXS;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		int pt = 0;
		switch (primitiveType) {
			case TRIS:
				pt = 0;
				break;
			case QUADS:
				pt = 1;
				break;
			case TRISTRIPS:
				pt = 2;
				break;
			case QUADSTRIPS:
				pt = 3;
				break;
		}
		out.writeInt(pt);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.begin(primitiveType);
	}
}
