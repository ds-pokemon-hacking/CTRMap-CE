package ctrmap.formats.ntr.common.gfx.commands.vtx;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.RGBA;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class VtxColorSet extends GECommand {

	public RGBA color;

	public VtxColorSet(RGBA color) {
		this.color = color;
	}

	public VtxColorSet(DataInput in) throws IOException {
		super(in);
		color = new GXColor(in).toRGBA();
		in.readUnsignedShort();
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.COLOR;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		new GXColor(color).write(out);
		out.writeShort(0);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.color(color);
	}

	@Override
	public String toString() {
		return "VtxColorSet(" + color + ")";
	}
}
