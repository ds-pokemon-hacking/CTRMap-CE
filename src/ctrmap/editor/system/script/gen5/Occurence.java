package ctrmap.editor.system.script.gen5;

import ctrmap.formats.ntr.common.FX;
import ctrmap.pokescript.instructions.gen5.VConstants;
import ctrmap.scriptformats.gen5.disasm.DisassembledCall;
import java.io.PrintStream;

public class Occurence {

	public final String callerName;
	public final String subroutineName;
	public final DisassembledCall call;

	public Occurence(String callerName, String subName, DisassembledCall call) {
		this.callerName = callerName;
		this.subroutineName = subName;
		this.call = call;
	}

	public void printCall(PrintStream out) {
		out.print(callerName);
		out.print(" :: ");
		out.print(subroutineName);
		out.print(" => ");
		out.print(call.command.methodNames[0]);
		out.print("(");
		for (int i = 0; i < call.args.length; i++) {
			if (i != 0) {
				out.print(", ");
			}
			int val = call.args[i];
			switch (call.definition.parameters[i].dataType) {
				case BOOL:
					out.print((val == 0) ? "false" : "true");
					break;
				case FLEX:
					if (VConstants.isWk(val)) {
						out.print("0x" + Integer.toHexString(val));
					} else {
						out.print(val);
					}
					break;
				case FX16:
				case FX32:
					out.print((val << 16 >> 16) * FX.FX_MIN);
					break;
				case S32:
				case U16:
				case U8:
					out.print(val);
					break;
				case VAR:
					out.print("0x" + Integer.toHexString(val));
					break;
			}
		}
		out.println(")");
	}
}
