package ctrmap.formats.ntr.common.gfx.commands;

import ctrmap.formats.ntr.common.gfx.commands.mat.MatColDifAmbSet;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatColSpcEmiSet;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatPaletteBaseSet;
import ctrmap.formats.ntr.common.gfx.commands.mat.MatTexImageParamSet;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxLoad4x3;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxLoad4x4;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxLoadIdentity;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMode;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMult3x3;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMult4x3;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxMult4x4;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxScale;
import ctrmap.formats.ntr.common.gfx.commands.mtx.MtxTranslate;
import ctrmap.formats.ntr.common.gfx.commands.mtx.stack.MtxStkLoad;
import ctrmap.formats.ntr.common.gfx.commands.mtx.stack.MtxStkPop;
import ctrmap.formats.ntr.common.gfx.commands.mtx.stack.MtxStkPush;
import ctrmap.formats.ntr.common.gfx.commands.mtx.stack.MtxStkStore;
import ctrmap.formats.ntr.common.gfx.commands.poly.PolyAttrSet;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxColorSet;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxListBegin;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxListEnd;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxNormalSet;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSet10;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSet16;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetDiff;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetXY;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetXZ;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxPosSetYZ;
import ctrmap.formats.ntr.common.gfx.commands.vtx.VtxUvSet;
import xstandard.io.base.iface.DataInputEx;
import java.io.DataInput;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GECommandDecoder {

	private static final Map<Integer, Class<? extends GECommand>> CLASS_MAP = new HashMap<>();

	private static void registClass(GEOpCode op, Class<? extends GECommand> cls) {
		CLASS_MAP.put(op.cmd, cls);
	}

	static {
		registClass(GEOpCode.BEGIN_VTXS, VtxListBegin.class);
		registClass(GEOpCode.END_VTXS, VtxListEnd.class);
		registClass(GEOpCode.NOP, GENop.class);
		registClass(GEOpCode.COLOR, VtxColorSet.class);
		registClass(GEOpCode.DIF_AMB, MatColDifAmbSet.class);
		registClass(GEOpCode.SPE_EMI, MatColSpcEmiSet.class);
		registClass(GEOpCode.TEXIMAGE_PARAM, MatTexImageParamSet.class);
		registClass(GEOpCode.PLTT_BASE, MatPaletteBaseSet.class);
		registClass(GEOpCode.TEXCOORD, VtxUvSet.class);
		registClass(GEOpCode.VTX_10, VtxPosSet10.class);
		registClass(GEOpCode.VTX_16, VtxPosSet16.class);
		registClass(GEOpCode.VTX_DIFF, VtxPosSetDiff.class);
		registClass(GEOpCode.VTX_XY, VtxPosSetXY.class);
		registClass(GEOpCode.VTX_XZ, VtxPosSetXZ.class);
		registClass(GEOpCode.VTX_YZ, VtxPosSetYZ.class);
		registClass(GEOpCode.NORMAL, VtxNormalSet.class);
		registClass(GEOpCode.POLYGON_ATTR, PolyAttrSet.class);
		registClass(GEOpCode.MTX_MODE, MtxMode.class);
		registClass(GEOpCode.MTX_PUSH, MtxStkPush.class);
		registClass(GEOpCode.MTX_POP, MtxStkPop.class);
		registClass(GEOpCode.MTX_STORE, MtxStkStore.class);
		registClass(GEOpCode.MTX_RESTORE, MtxStkLoad.class);
		registClass(GEOpCode.MTX_LOAD_IDENTITY, MtxLoadIdentity.class);
		registClass(GEOpCode.MTX_TRANSLATE, MtxTranslate.class);
		registClass(GEOpCode.MTX_SCALE, MtxScale.class);
		registClass(GEOpCode.MTX_LOAD_4X3, MtxLoad4x3.class);
		registClass(GEOpCode.MTX_LOAD_4X4, MtxLoad4x4.class);
		registClass(GEOpCode.MTX_MULT_3x3, MtxMult3x3.class);
		registClass(GEOpCode.MTX_MULT_4X3, MtxMult4x3.class);
		registClass(GEOpCode.MTX_MULT_4X4, MtxMult4x4.class);
	}

	public static GECommand decode(int opcode, DataInput data) throws IOException {
		Class<? extends GECommand> cls = CLASS_MAP.get(opcode);
		if (cls == null) {
			throw new IOException("Could not find GE command class for 0x" + Integer.toHexString(opcode));
		}
		try {
			Constructor<? extends GECommand> constructor = cls.getConstructor(DataInput.class);
			GECommand cmd = constructor.newInstance(data);
			cmd.opCode = GEOpCode.valueOf(opcode);
			return cmd;
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			throw new IOException(ex);
		}
	}

	public static void getPackedOpcodes(DataInputEx data, int[] dest) throws IOException {
		int packed = data.readInt();
		for (int i = 0; i < 4; i++) {
			dest[i] = packed & 0xFF;
			packed >>= 8;
		}
	}

	public static List<GECommand> decodePacked(DataInputEx data, int sizeLimit) throws IOException {
		List<GECommand> dest = new ArrayList<>();
		int endPos = data.getPosition() + sizeLimit;
		while (data.getPosition() < endPos) {
			int packed = data.readInt();
			for (int i = 0; i < 4; i++) {
				dest.add(decode(packed & 0xFF, data));
				packed >>= 8;
			}
		}
		return dest;
	}
}
