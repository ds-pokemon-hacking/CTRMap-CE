
package ctrmap.formats.ntr.common.gfx.commands.mtx.stack;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;
import java.io.IOException;

/**
 *
 */
public class MtxStkLoad extends MtxStkOpBase {
	
	public MtxStkLoad(int sourceStkPos){
		super(sourceStkPos);
	}
	
	public MtxStkLoad(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_RESTORE;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.loadMatrix(pos);
	}
}
