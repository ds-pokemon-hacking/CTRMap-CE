
package ctrmap.formats.ntr.common.gfx.commands.mtx;

import ctrmap.formats.ntr.common.gfx.commands.GEOpCode;
import ctrmap.formats.ntr.common.gfx.commands.GEParamlessCommand;
import ctrmap.formats.ntr.common.gfx.commands.IGECommandProcessor;
import java.io.DataInput;

/**
 *
 */
public class MtxLoadIdentity extends GEParamlessCommand {

	public MtxLoadIdentity() {
		
	}
	
	public MtxLoadIdentity(DataInput in) {
		
	}
	
	@Override
	public GEOpCode getOpCode() {
		return GEOpCode.MTX_LOAD_IDENTITY;
	}

	@Override
	public void process(IGECommandProcessor processor) {
		processor.loadIdentity();
	}
	
}
