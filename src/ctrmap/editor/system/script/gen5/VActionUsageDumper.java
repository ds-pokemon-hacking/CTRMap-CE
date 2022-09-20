package ctrmap.editor.system.script.gen5;

import ctrmap.pokescript.instructions.gen5.VConstants;
import ctrmap.pokescript.instructions.ntr.NTRInstructionCall;
import ctrmap.scriptformats.gen5.VCommandDataBase;
import ctrmap.scriptformats.gen5.disasm.DisassembledCall;
import ctrmap.scriptformats.gen5.disasm.DisassembledMethod;
import ctrmap.scriptformats.gen5.disasm.VDisassembler;
import xstandard.fs.FSFile;
import xstandard.io.util.IndentedPrintStream;
import xstandard.text.FormattingUtils;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class VActionUsageDumper {

	private static final HashSet<Integer> EXCLUDED_OPCODES = new HashSet<>(ArraysEx.asList());

	static {
		for (int i = 0; i <= 0x3B; i++) {
			EXCLUDED_OPCODES.add(i);
		}
	}

	public static void dumpFile(FSFile destFile, VScriptUsageDumperInput... sources) {
		Map<Integer, List<ActionOccurence>> occurences = new HashMap<>();

		for (VScriptUsageDumperInput in : sources) {
			VDisassembler disasm = new VDisassembler(in.script, in.commandDB);
			disasm.unsafeMode = false;
			try {
				disasm.disassemble();
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}

			for (DisassembledMethod acmdSeq : disasm.actionSequences) {
				if (!acmdSeq.instructions.isEmpty()) {
					NTRInstructionCall caller = null;

					Outer:
					for (DisassembledMethod m : disasm.methods) {
						for (DisassembledCall c : m.instructions) {
							if (c.command.type == VCommandDataBase.CommandType.ACTION_JUMP) {
								if (c.link != null && c.link.target == acmdSeq.instructions.get(0)) {
									caller = c;
									break Outer;
								}
							}
						}
					}

					for (DisassembledCall i : acmdSeq.instructions) {
						if (!EXCLUDED_OPCODES.contains(i.definition.opCode)) {
							int act = -1;
							if (caller != null) {
								act = caller.args[0];
							}
							List<ActionOccurence> occList = occurences.get(i.definition.opCode);
							if (occList == null) {
								occList = new ArrayList<>();
								occurences.put(i.definition.opCode, occList);
							}
							occList.add(new ActionOccurence(in.callerName, acmdSeq.getName(), act, i));
						}
					}
				}
			}
		}

		IndentedPrintStream out = new IndentedPrintStream(destFile.getNativeOutputStream());

		out.println();

		out.print("public enum ");
		out.print(destFile.getNameWithoutExtension());
		out.println(" {");
		out.incrementIndentLevel();

		int keyMax = -1;

		for (int key : occurences.keySet()) {
			if (key > keyMax) {
				keyMax = key;
			}
		}

		for (int i = 0; i <= keyMax; i++) {
			List<ActionOccurence> l = occurences.get(i);
			if (l == null) {
				l = new ArrayList<>();
			}
			l.sort((o1, o2) -> {
				int diffCaller = o1.callerName.compareTo(o2.callerName);
				if (diffCaller != 0) {
					return diffCaller;
				}
				int diffSub = o1.subroutineName.compareTo(o2.subroutineName);
				if (diffSub != 0) {
					return diffSub;
				}
				return o1.call.pointer - o2.call.pointer;
			});

			out.print("ACMD_" + FormattingUtils.getStrWithLeadingZeros(3, Integer.toHexString(i)));
			if (i != keyMax) {
				out.println(",");
			} else {
				out.println();
			}

			if (!l.isEmpty()) {
				out.println("//Used in:");
			} else {
				out.println("//This command is unused");
			}

			for (ActionOccurence occ : l) {
				out.print("//");
				out.print(occ.callerName);
				out.print(" :: ");
				out.print(occ.subroutineName);
				out.print(" @ Actor ");
				if (VConstants.isWk(occ.actor)) {
					out.println("from event work 0x" + Integer.toHexString(occ.actor));
				} else {
					out.println(occ.actor);
				}
			}
		}

		out.decrementIndentLevel();
		out.println("}");
		out.close();
	}

	private static class ActionOccurence extends Occurence {

		public final int actor;

		public ActionOccurence(String callerName, String subName, int actor, DisassembledCall call) {
			super(callerName, subName, call);
			this.actor = actor;
		}
	}
}
