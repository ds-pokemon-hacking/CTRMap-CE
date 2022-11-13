
package ctrmap.formats.ntr.common.gfx.commands.mat;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.vec.RGBA;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import xstandard.math.BitMath;

/**
 *
 */
public class MatColDifAmbSet extends GECommand {

	public RGBA diffuse;
	public RGBA ambient;
	
	public boolean difAsVCol;
	
	public MatColDifAmbSet(RGBA diffuse, RGBA ambient){
		this.diffuse = diffuse;
		this.ambient = ambient;
	}
	
	public MatColDifAmbSet(DataInput in) throws IOException {
		int val = in.readUnsignedShort();
		diffuse = new GXColor(val & 0x7FFF).toRGBA();
		difAsVCol = BitMath.checkIntegerBit(val, 15);
		ambient = new GXColor(in).toRGBA();
	}
	
	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.DIF_AMB;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		new GXColor(diffuse).write(out);
		new GXColor(ambient).write(out);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.matDiffuseAmbient(diffuse, ambient, difAsVCol);
	}

}
