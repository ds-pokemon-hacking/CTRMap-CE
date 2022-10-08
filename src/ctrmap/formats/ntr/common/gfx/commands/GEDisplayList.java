package ctrmap.formats.ntr.common.gfx.commands;

import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import xstandard.io.base.iface.DataInputEx;

public class GEDisplayList implements Iterable<GECommand> {

	private List<GECommand> commands = new ArrayList<>();

	public GEDisplayList() {
	}

	public GEDisplayList(DataInputEx in, int size) throws IOException {
		int endPos = in.getPosition() + size;
		int[] ops = new int[4];
		while (in.getPosition() < endPos) {
			GECommandDecoder.getPackedOpcodes(in, ops);
			for (int i = 0; i < 4; i++) {
				addCommand(GECommandDecoder.decode(ops[i], in));
			}
		}
	}

	public final void addCommand(GECommand cmd) {
		commands.add(cmd);
	}

	public void write(DataOutput out) throws IOException {
		for (int i = 0; i < commands.size(); i += 4) {
			//Write opcodes
			for (int j = 0; j < 4; j++) {
				int idx = i + j;
				if (idx < commands.size()) {
					out.write(commands.get(idx).getOpCode().cmd);
				} else {
					out.write(0); //NOP
				}
			}

			//Write parameters
			for (int j = 0; j < 4; j++) {
				int idx = i + j;
				if (idx < commands.size()) {
					commands.get(idx).writeParams(out);
				}
			}
		}

		out.writeInt(0); //The original command lists seem to always be terminated with 4 NOPs
	}

	@Override
	public Iterator<GECommand> iterator() {
		return commands.iterator();
	}
}
