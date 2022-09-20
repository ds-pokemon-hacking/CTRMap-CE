package ctrmap.editor.system.script.gen5;

import ctrmap.pokescript.LangConstants;
import ctrmap.pokescript.instructions.gen5.VOpCode;
import ctrmap.pokescript.instructions.ntr.NTRAnnotations;
import ctrmap.scriptformats.gen5.VCommandDataBase;
import ctrmap.scriptformats.gen5.disasm.DisassembledCall;
import ctrmap.scriptformats.gen5.disasm.DisassembledMethod;
import ctrmap.scriptformats.gen5.disasm.VDisassembler;
import xstandard.fs.FSFile;
import xstandard.io.util.IndentedPrintStream;
import xstandard.text.StringEx;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class VScriptUsageDumper {

	private static final HashSet<Integer> EXCLUDED_OPCODES = new HashSet<>(ArraysEx.asList(
		0x64, //apply movement
		0x65 //wait for action finish
	));

	static {
		for (VOpCode op : VOpCode.VALUES) {
			switch (op) {
				case GlobalCall:
				case GlobalCallAsync:
				case ReserveScript:
				case StoreMapTypeChange:
					continue;
				case ReadVar:
				case PopAndDiscard:
				case PopStackAndReadVar:
				case VMVarSetU32:
				case VMVarSetU8:
				case VMVarSetVMVar:
				case CmpVMVarConst:
				case CmpVMVarVMVar:
				case Halt2:
					continue;
				default:
					EXCLUDED_OPCODES.add(op.ordinal());
			}
		}
	}

	public static void dumpFile(FSFile destROOT, VScriptUsageDumperInput... sources) {
		Map<VCommandDataBase.VCommand, List<Occurence>> occurences = new HashMap<>();

		for (VScriptUsageDumperInput in : sources) {
			System.out.println("Loading file " + in.callerName);
			VDisassembler disasm = new VDisassembler(in.script, in.commandDB);
			disasm.unsafeMode = false;
			try {
				disasm.disassemble();
			} catch (Exception ex) {
				ex.printStackTrace();
				System.err.println("Error while decompiling " + in.script.getSourceFile() + ", skipping...");
				continue;
			}

			for (DisassembledMethod subroutine : disasm.methods) {
				for (DisassembledCall i : subroutine.instructions) {
					if (!EXCLUDED_OPCODES.contains(i.definition.opCode)) {
						VCommandDataBase.VCommand cmd = i.command;

						List<Occurence> occList = occurences.get(cmd);
						if (occList == null) {
							occList = new ArrayList<>();
							occurences.put(cmd, occList);
						}
						if (subroutine.getName() == null) {
							throw new NullPointerException("Null subroutine name at " + Integer.toHexString(subroutine.ptr) + " file " + in.callerName);
						}
						occList.add(new Occurence(in.callerName, subroutine.getName(), i));
					}
				}
			}
		}

		Map<String, List<Occurence>> occurencesPerClass = new HashMap<>();

		for (Map.Entry<VCommandDataBase.VCommand, List<Occurence>> e : occurences.entrySet()) {
			VCommandDataBase.VCommand cmd = e.getKey();

			List<Occurence> occList = occurencesPerClass.get(cmd.classPath);
			if (occList == null) {
				occList = new ArrayList<>();
				occurencesPerClass.put(cmd.classPath, occList);
			}
			occList.addAll(e.getValue());
		}

		for (Map.Entry<String, List<Occurence>> e : occurencesPerClass.entrySet()) {
			FSFile file = destROOT.getChild(e.getKey().replace('.', '/') + LangConstants.LANG_GENERAL_HEADER_EXTENSION);
			file.getParent().mkdirs();

			IndentedPrintStream out = new IndentedPrintStream(file.getNativeOutputStream());

			String pkg = StringEx.deleteAllString(file.getPathRelativeTo(destROOT), "/" + file.getName()).replace('/', '.');
			if (file.getParent().equals(destROOT)) {
				pkg = "";
			}

			if (!pkg.isEmpty()) {
				out.print("package ");
				out.print(pkg);
				out.println(";");
			}

			out.println();

			out.print("public class ");
			out.print(file.getNameWithoutExtension());
			out.println(" {");
			out.incrementIndentLevel();

			List<Occurence> l = e.getValue();
			l.sort((o1, o2) -> {
				int diffOpcode = o1.call.definition.opCode - o2.call.definition.opCode;
				if (diffOpcode != 0) {
					return diffOpcode;
				}
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

			VCommandDataBase.VCommand lastCmd = null;

			for (Occurence occ : l) {
				if (occ.call.command != lastCmd) {
					lastCmd = occ.call.command;

					for (int i = 0; i < lastCmd.def.parameters.length; i++) {
						int lenOvr = -1;
						switch (lastCmd.def.parameters[i].dataType) {
							case FX32:
							case S32:
								lenOvr = 4;
								break;
							case U8:
								lenOvr = 1;
								break;
						}
						if (lenOvr != -1) {
							out.print("@");
							out.print(NTRAnnotations.NAME_ARG_BYTES_OVERRIDE);
							out.print("(");
							out.print(NTRAnnotations.ARG_ARG_NAME);
							out.print(" = ");
							out.print(lastCmd.paramNames[i] == null ? ("a" + (i + 1)) : lastCmd.paramNames[i]);
							out.print(", ");
							out.print(NTRAnnotations.ARG_BYTES);
							out.print(" = ");
							out.print(lenOvr);
							out.println(")");
						}
					}

					out.print("static native void ");
					out.print(lastCmd.methodNames[0]);
					out.print("(");
					for (int i = 0; i < lastCmd.def.parameters.length; i++) {
						if (i != 0) {
							out.print(", ");
						}
						String name = lastCmd.paramNames[i];
						if (name == null) {
							name = "a" + (i + 1);
						}
						String type = "int";
						switch (lastCmd.def.parameters[i].dataType) {
							case BOOL:
								type = "boolean";
								break;
							case S32:
							case U8:
							case U16:
								type = "final int";
								break;
							case FLEX:
							case VAR:
								type = "int";
								break;
							case FX16:
							case FX32:
								type = "float";
								break;
						}
						out.print(type);
						out.print(" ");
						out.print(name);
					}
					out.print(") : 0x");
					out.print(Integer.toHexString(lastCmd.def.opCode));
					out.println(";");
					out.println("//Used in:");
				}

				out.print("//");
				occ.printCall(out);
			}

			out.decrementIndentLevel();
			out.println("}");

			out.close();
		}
	}
}
