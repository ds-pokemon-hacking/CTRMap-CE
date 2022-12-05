
package ctrmap.formats.ntr.common.gfx.commands.poly;

import ctrmap.formats.ntr.common.gfx.GXColor;
import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import xstandard.math.BitMath;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class PolyAttrSet extends GECommand {

	public final boolean[] lightsEnabled = new boolean[4];
	
	public GEPolygonMode polygonMode = GEPolygonMode.MODULATE;
	public boolean drawFrontFace;
	public boolean drawBackFace;
	
	public GEXLUDepthMode xluDepthMode = GEXLUDepthMode.KEEP;
	public GEFarClipMode farClipMode = GEFarClipMode.SKIP;
	public GE1DotOverMode dot1OverMode = GE1DotOverMode.SKIP;
	public GEDepthFunction depthFunc = GEDepthFunction.LESS;
	
	public boolean enableFog = true;
	
	public int constVertexAlpha = 255; //0 to 255 since we're mostly using modern structs at this point
	
	public int polygonId = 0;
	
	public PolyAttrSet() {
		
	}
	
	public PolyAttrSet(DataInput in) throws IOException {
		int param = in.readInt();
		for (int i = 0; i < 4; i++) {
			lightsEnabled[i] = BitMath.checkIntegerBit(param, i);
		}
		polygonMode = GEPolygonMode.values()[BitMath.getIntegerBits(param, 4, 2)];
		drawBackFace = BitMath.checkIntegerBit(param, 6);
		drawFrontFace = BitMath.checkIntegerBit(param, 7);
		xluDepthMode = GEXLUDepthMode.values()[BitMath.getIntegerBits(param, 11, 1)];
		farClipMode = GEFarClipMode.values()[BitMath.getIntegerBits(param, 12, 1)];
		dot1OverMode = GE1DotOverMode.values()[BitMath.getIntegerBits(param, 13, 1)];
		depthFunc = GEDepthFunction.values()[BitMath.getIntegerBits(param, 14, 1)];
		enableFog = BitMath.checkIntegerBit(param, 15);
		constVertexAlpha = GXColor.bit5to8(BitMath.getIntegerBits(param, 16, 5));
		polygonId = BitMath.getIntegerBits(param, 24, 5);
	}
	
	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.POLYGON_ATTR;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		int param = 0;
		for (int i = 0; i < 4; i++){
			param = BitMath.setIntegerBit(param, i, lightsEnabled[i]);
		}
		param = BitMath.setIntegerBits(param, 4, 2, polygonMode.ordinal());
		param = BitMath.setIntegerBit(param, 6, drawBackFace);
		param = BitMath.setIntegerBit(param, 7, drawFrontFace);
		param = BitMath.setIntegerBit(param, 11, xluDepthMode.ordinal());
		param = BitMath.setIntegerBit(param, 12, farClipMode.ordinal());
		param = BitMath.setIntegerBit(param, 13, dot1OverMode.ordinal());
		param = BitMath.setIntegerBit(param, 14, depthFunc.ordinal());
		param = BitMath.setIntegerBit(param, 15, enableFog);
		param = BitMath.setIntegerBits(param, 16, 5, (constVertexAlpha >> 3));
		param = BitMath.setIntegerBits(param, 24, 5, polygonId);
		out.writeInt(param);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.polygonMode(polygonMode);
		processor.polygonId(polygonId);
		processor.polygonAlpha(constVertexAlpha / 255f);
		processor.cullFace(drawFrontFace, drawBackFace);
		processor.depthFunc(depthFunc);
		processor.xluDepthMode(xluDepthMode);
		processor.farClipMode(farClipMode);
		processor.dot1OverMode(dot1OverMode);
		processor.setFogEnable(enableFog);
		for (int i = 0; i < 4; i++) {
			processor.setLightEnable(i, lightsEnabled[i]);
		}
	}

	public static enum GEPolygonMode {
		MODULATE,
		DECAL,
		TOON,
		SHADOW
	}
	
	public static enum GEXLUDepthMode {
		KEEP,
		REPLACE
	}
	
	public static enum GEFarClipMode {
		SKIP,
		CLIP
	}
	
	public static enum GE1DotOverMode {
		SKIP,
		RENDER
	}
	
	public static enum GEDepthFunction {
		LESS,
		EQUAL
	}
}
