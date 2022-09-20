package ctrmap.editor.system.script.gen5;

import ctrmap.pokescript.instructions.gen5.VConstants;
import ctrmap.pokescript.instructions.ntr.NTRDataType;
import ctrmap.scriptformats.gen5.disasm.DisassembledCall;
import ctrmap.scriptformats.gen5.disasm.DisassembledMethod;
import ctrmap.scriptformats.gen5.disasm.MathCommands;
import ctrmap.scriptformats.gen5.disasm.VDisassembler;
import xstandard.fs.FSFile;
import xstandard.io.util.IndentedPrintStream;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class VWorkUsageDumper {

	private static final HashSet<Integer> EXCLUDED_WORKS = new HashSet<>(ArraysEx.asList());

	static {

	}

	public static void dumpFile(FSFile destFile, VScriptUsageDumperInput... sources) {
		Map<Integer, List<Occurence>> occurences = new HashMap<>();
		Map<Integer, List<Occurence>> settings = new HashMap<>();

		for (VScriptUsageDumperInput in : sources) {
			VDisassembler disasm = new VDisassembler(in.script, in.commandDB);
			disasm.unsafeMode = false;
			try {
				disasm.disassemble();
			} catch (Exception ex) {
				ex.printStackTrace();
				continue;
			}

			for (DisassembledMethod m : disasm.methods) {
				for (DisassembledCall c : m.instructions) {
					MathCommands mcmd = MathCommands.valueOf(c.definition.opCode);
					if (mcmd == MathCommands.SET_VAR || mcmd == MathCommands.SET_FLEX || mcmd == MathCommands.SET_FLEX) {
						if (VConstants.isLowWk(c.args[0])) {
							List<Occurence> l = settings.get(c.args[0]);
							if (l == null) {
								l = new ArrayList<>();
								settings.put(c.args[0], l);
							}
							l.add(new Occurence(in.callerName, m.getName(), c));
						}
					} else {
						for (int i = 0; i < c.args.length; i++) {
							if (c.definition.parameters[i].dataType == NTRDataType.VAR || c.definition.parameters[i].dataType == NTRDataType.FLEX) {
								if (VConstants.isWk(c.args[i])) {
									List<Occurence> l = occurences.get(c.args[i]);
									if (l == null) {
										l = new ArrayList<>();
										occurences.put(c.args[i], l);
									}
									l.add(new Occurence(in.callerName, m.getName(), c));
								}
							}
						}
					}
				}
			}
		}

		IndentedPrintStream out = new IndentedPrintStream(destFile.getNativeOutputStream());

		int lowkMax = -1;
		int hiwkMax = -1;

		List<Integer> keys = new ArrayList<>(occurences.keySet());
		keys.addAll(settings.keySet());

		for (int key : occurences.keySet()) {
			if (VConstants.isLowWk(key)) {
				if (key > lowkMax) {
					lowkMax = key;
				}
			} else {
				if (key > hiwkMax) {
					hiwkMax = key;
				}
			}
		}

		Comparator<Occurence> comp = (o1, o2) -> {
			int diffCaller = o1.callerName.compareTo(o2.callerName);
			if (diffCaller != 0) {
				return diffCaller;
			}
			int diffSub = o1.subroutineName.compareTo(o2.subroutineName);
			if (diffSub != 0) {
				return diffSub;
			}
			return o1.call.pointer - o2.call.pointer;
		};

		for (int i = VConstants.WKVAL_START; i <= hiwkMax; i++) {
			if (i > lowkMax && i < VConstants.WKVAL_GP_START) {
				continue;
			}
			List<Occurence> l = occurences.get(i);
			List<Occurence> setting = settings.get(i);
			if (l == null) {
				l = new ArrayList<>();
			}
			if (setting == null) {
				setting = new ArrayList<>();
			}
			l.sort(comp);
			setting.sort(comp);

			out.println("==== 0x" + Integer.toHexString(i) + " ====");
			if (l.isEmpty() && setting.isEmpty()) {
				out.println(" -- This work is unused --");
			} else {
				if (!setting.isEmpty()) {
					out.println("Explicitly set at:");
				}
				for (Occurence occ : setting) {
					occ.printCall(out);
				}
				if (!l.isEmpty()) {
					if (!setting.isEmpty()) {
						out.println("--");
					}
					out.println("Used in:");
				}
				for (Occurence occ : l) {
					occ.printCall(out);
				}
			}
			out.println();
		}
		out.close();
	}
}
