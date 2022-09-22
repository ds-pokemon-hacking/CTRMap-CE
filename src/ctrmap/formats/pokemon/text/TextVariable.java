package ctrmap.formats.pokemon.text;

import xstandard.text.FormattingUtils;
import xstandard.util.ParsingUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextVariable {

	private TextVariableCode code;
	private MessageHandler hnd;
	private int cmdBinary;
	private List<Integer> args = new ArrayList<>();

	public TextVariable(String text, MessageHandler hnd) {
		this.hnd = hnd;
		int codeStart;
		if (text.contains("VAR")) {
			codeStart = text.indexOf("VAR") + 3;
		} else {
			codeStart = text.indexOf('[') + 1;
		}

		for (; codeStart < text.length(); codeStart++) {
			if (!Character.isWhitespace(text.charAt(codeStart))) {
				break;
			}
		}

		int codeEnd = codeStart;
		for (; codeEnd < text.length(); codeEnd++) {
			char c = text.charAt(codeEnd);
			if (c != '_' && !Character.isLetterOrDigit(c)) {
				break;
			}
		}
		String codeName = text.substring(codeStart, codeEnd).trim();
		cmdBinary = ParsingUtils.parseBasedIntOrDefault(codeName, -1);
		if (cmdBinary == -1) {
			code = findEnumByLowerCase(hnd.getVarCodeEnums(), codeName);
			cmdBinary = code == null ? -1 : code.getBinary();
		}

		int argStart = text.indexOf('(', codeEnd);
		int argEnd = text.indexOf(')', argStart);
		if (argStart != -1 && argEnd != -1 && argStart != argEnd) {
			String argSrc = text.substring(argStart + 1, argEnd);
			String[] argCmds = argSrc.split(",");
			for (String a : argCmds) {
				a = a.trim();
				try {
					args.add(Integer.parseInt(a));
				} catch (NumberFormatException ex) {
					System.err.println("Illegal argument " + a + ": Not a number.");
				}
			}
		}
	}

	private static TextVariableCode findEnumByLowerCase(TextVariableCode[] enums, String friendlyName) {
		String lc = friendlyName.toLowerCase();
		for (TextVariableCode c : enums) {
			if (c.getName().toLowerCase().equals(lc)) {
				return c;
			}
		}
		return null;
	}

	private static TextVariableCode findCodeByBinary(TextVariableCode[] enums, int binary) {
		for (TextVariableCode c : enums) {
			if (c.getBinary() == binary) {
				return c;
			}
		}
		return null;
	}

	public TextVariable(GFMessageStream strm, MessageHandler hnd) throws IOException {
		int argCount;
		this.hnd = hnd;
		if (hnd.getHasVarLengthFirst()) {
			argCount = strm.readChar() - 1;
			if (argCount != -1) {
				cmdBinary = strm.readChar();
			} else {
				cmdBinary = -1;
			}
		} else {
			cmdBinary = strm.readChar();
			argCount = strm.readChar();
		}
		code = findCodeByBinary(hnd.getVarCodeEnums(), cmdBinary);
		if (code != null) {
			int oac = code.getOverrideArgCount();
			if (oac != -1) {
				argCount = oac;
			}
		}

		for (int i = 0; i < argCount; i++) {
			args.add((int) strm.readChar());
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		if (code != null) {
			if (!code.getIsImperative()) {
				sb.append("VAR ");
			}
			sb.append(code.toString());
		} else {
			sb.append("0x").append(FormattingUtils.getStrWithLeadingZeros(4, Integer.toHexString(cmdBinary)));
		}
		if (!args.isEmpty()) {
			sb.append('(');
			for (int i = 0; i < args.size(); i++) {
				if (i != 0) {
					sb.append(", ");
				}
				sb.append(String.valueOf(args.get(i)));
			}
			sb.append(')');
		}
		sb.append(']');
		return sb.toString();
	}

	public void write(GFMessageStream strm) throws IOException {
		strm.writeChar(hnd.getVarIdentChar());
		if (hnd.getHasVarLengthFirst()) {
			strm.writeChar(args.size() + 1);
			strm.writeChar(cmdBinary);
		} else {
			strm.writeChar(cmdBinary);
			strm.writeChar(args.size());
		}

		for (Integer argument : args) {
			strm.writeChar(argument);
		}
	}
}
