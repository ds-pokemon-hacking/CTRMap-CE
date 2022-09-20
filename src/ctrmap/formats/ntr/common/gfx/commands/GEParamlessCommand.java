
package ctrmap.formats.ntr.common.gfx.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public abstract class GEParamlessCommand extends GECommand {

	public GEParamlessCommand() {
		
	}
	
	public GEParamlessCommand(DataInput in) throws IOException {
		super(in);
	}

	@Override
	public void writeParams(DataOutput out) {
	}

}
