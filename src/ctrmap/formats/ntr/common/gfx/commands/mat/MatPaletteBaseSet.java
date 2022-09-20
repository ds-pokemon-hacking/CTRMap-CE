
package ctrmap.formats.ntr.common.gfx.commands.mat;

import ctrmap.formats.ntr.common.gfx.commands.GECommand;
import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class MatPaletteBaseSet extends GECommand {
	
	public final int base;
	
	public MatPaletteBaseSet(int base) {
		this.base = base;
	}
	
	public MatPaletteBaseSet(DataInput in) throws IOException {
		base = in.readInt() & 0x1FFF;
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.PLTT_BASE;
	}

	@Override
	public void writeParams(DataOutput out) throws IOException {
		out.writeInt(base & 0x1FFF);
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.texPaletteBase(base);
	}

}
