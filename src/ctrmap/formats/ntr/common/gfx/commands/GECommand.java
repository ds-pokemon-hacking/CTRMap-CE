
package ctrmap.formats.ntr.common.gfx.commands;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class GECommand {
	
	GEOpCode opCode;
	
	public GECommand() {
		opCode = getOpCode();
	}
	
	public GECommand(DataInput in) throws IOException {
		
	}
	
	public int sizeOf() {
		return Integer.BYTES * (1 + getOpCode().argCount);
	}
	
	public abstract void process(IGECommandProcessor processor);
	
	public abstract GEOpCode getOpCode();
	public abstract void writeParams(DataOutput out) throws IOException;
}
