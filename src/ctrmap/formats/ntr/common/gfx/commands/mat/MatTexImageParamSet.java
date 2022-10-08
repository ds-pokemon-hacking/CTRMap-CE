package ctrmap.formats.ntr.common.gfx.commands.mat;

import ctrmap.formats.ntr.common.gfx.texture.GETextureFormat;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.TemporaryValueShort;
import xstandard.math.BitMath;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class MatTexImageParamSet extends GECommand {

	public int texVRAMOffs;
	private TemporaryValueShort texVRAMOffsTemp;

	public boolean repeatU;
	public boolean repeatV;
	public boolean mirrorU;
	public boolean mirrorV;

	public int width;
	public int height;

	public GETextureFormat format = GETextureFormat.NULL;

	public boolean palCol0IsTransparent = false;

	public GETexcoordGenMode texGenMode = GETexcoordGenMode.NONE; //idk why it's not texcoord, but most materials AND textures have these bits cleared

	public MatTexImageParamSet() {
		super();
	}
	
	public MatTexImageParamSet(DataInput in) throws IOException {
		int param = in.readInt();
		texVRAMOffs = BitMath.getIntegerBits(param, 0, 16);
		
	}
	
	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.TEXIMAGE_PARAM;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		int param = texVRAMOffs;

		param = BitMath.setIntegerBit(param, 16, repeatU);
		param = BitMath.setIntegerBit(param, 17, repeatV);
		param = BitMath.setIntegerBit(param, 18, mirrorU);
		param = BitMath.setIntegerBit(param, 19, mirrorV);
		//Let it be known that there is definitely a faster way to do this logarithm using bitwise ops, but since this isn't used in perf code, I think it looks cleaner
		param = BitMath.setIntegerBits(param, 20, 3, (int)((Math.log(width) / Math.log(2)) - 3));
		param = BitMath.setIntegerBits(param, 23, 3, (int)((Math.log(height) / Math.log(2)) - 3));
		param = BitMath.setIntegerBits(param, 26, 3, format.ordinal());
		param = BitMath.setIntegerBit(param, 29, palCol0IsTransparent);
		param = BitMath.setIntegerBits(param, 30, 2, texGenMode.ordinal());

		if (out instanceof DataIOStream) {
			texVRAMOffsTemp = new TemporaryValueShort((DataIOStream)out);
		}
		else {
			out.writeShort(texVRAMOffs);
		}
		out.writeShort(param >> 16);
	}

	public TemporaryValueShort getTexVRAMOffsTempOffs() {
		return texVRAMOffsTemp;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.texImage2D(width, height, format, texVRAMOffs);
		processor.texColor0Transparent(palCol0IsTransparent);
		processor.texGenMode(texGenMode);
		processor.texMap(repeatU, repeatV, mirrorU, mirrorV);
	}

	public static enum GETexcoordGenMode {
		NONE,
		TEXCOORD,
		NORMAL,
		POSITION
	}
}
