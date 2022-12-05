package ctrmap.formats.ntr.common.gfx.commands;

import java.util.HashMap;
import java.util.Map;

public enum GEOpCode {
	NOP(0x0, 0),
	
	MTX_MODE(0x10, 1),
	
	MTX_PUSH(0x11, 0),
	MTX_POP(0x12, 1),
	MTX_STORE(0x13, 1),
	MTX_RESTORE(0x14, 1),
	
	MTX_LOAD_IDENTITY(0x15, 0),
	MTX_LOAD_4X4(0x16, 4*4),
	MTX_LOAD_4X3(0x17, 4*3),
	
	MTX_MULT_4X4(0x18, 4*4),
	MTX_MULT_4X3(0x19, 4*3),
	MTX_MULT_3x3(0x1a, 3*3),
	
	MTX_SCALE(0x1b, 3),
	MTX_TRANSLATE(0x1c, 3),
	
	COLOR(0x20, 1),
	NORMAL(0x21, 1),
	TEXCOORD(0x22, 1),
	
	VTX_16(0x23, 2),
	VTX_10(0x24, 1),
	VTX_XY(0x25, 1),
	VTX_XZ(0x26, 1),
	VTX_YZ(0x27, 1),
	VTX_DIFF(0x28, 1),
	
	POLYGON_ATTR(0x29, 1),
	TEXIMAGE_PARAM(0x2a, 1),
	PLTT_BASE(0x2b, 1),
	
	DIF_AMB(0x30, 1),
	SPE_EMI(0x31, 1),
	LIGHT_VECTOR(0x32, 1),
	LIGHT_COLOR(0x33, 1),
	SHININESS(0x34, 32),
	
	BEGIN_VTXS(0x40, 1),
	END_VTXS(0x41, 0),
	
	SWAP_BUFFERS(0x50, 1),
	
	VIEWPORT(0x60, 1),
	
	BOX_TEST(0x70, 3),
	POS_TEST(0x71, 2),
	VEC_TEST(0x72, 1),
	;
	
	private static final Map<Integer, GEOpCode> map = new HashMap<>();
	
	static {
		for (GEOpCode op : values()) {
			map.put(op.cmd, op);
		}
	}

	public final int cmd;
	public final int argCount;

	private GEOpCode(int cmd, int argCount) {
		this.cmd = cmd;
		this.argCount = argCount;
	}
	
	public static GEOpCode valueOf(int cmd) {
		return map.get(cmd);
	}
}
