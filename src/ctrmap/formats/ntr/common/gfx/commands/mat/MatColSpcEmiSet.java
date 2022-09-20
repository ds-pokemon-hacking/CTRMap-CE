package ctrmap.formats.ntr.common.gfx.commands.mat;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.RGBA;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class MatColSpcEmiSet extends GECommand {

	public RGBA specular;
	public RGBA emission;

	public MatColSpcEmiSet(RGBA specular, RGBA emission) {
		this.specular = specular;
		this.emission = emission;
	}

	public MatColSpcEmiSet(DataInput in) throws IOException {
		specular = new GXColor(in).toRGBA();
		emission = new GXColor(in).toRGBA();
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.DIF_AMB;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		new GXColor(specular).write(out);
		new GXColor(emission).write(out);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.matSpecularEmissive(specular, emission);
	}

}
