package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.varcodes.GenVITextVariableCode;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class GenVIMessageHandler implements MessageHandler {

	public static final Map<Character, String> SPEC_CHARS_VI = new HashMap<>();

	static {
		SPEC_CHARS_VI.put((char) 0xE07F, "\u202f");
		SPEC_CHARS_VI.put((char) 0xE08D, "…");
		SPEC_CHARS_VI.put((char) 0xE08E, "♂");
		SPEC_CHARS_VI.put((char) 0xE08F, "♀");
	}

	@Override
	public Map<Character, String> getCharLUT() {
		return SPEC_CHARS_VI;
	}

	@Override
	public char getVarIdentChar() {
		return 0x10;
	}

	@Override
	public boolean getHasVarLengthFirst() {
		return true;
	}

	@Override
	public String getBlankLineText(int i) {
		return "[BLANK(" + i + ")]";
	}

	@Override
	public String handleUnrecognizedCharacter(char c, GFMessageStream strm) {
		if (c == 0xFF0D) {
			return String.valueOf(c);
		}
		return "\\u" + Integer.toHexString(c);
	}

	@Override
	public char getTerminator() {
		return '\0';
	}

	@Override
	public boolean isMsgDataPadded() {
		return true;
	}

	@Override
	public boolean isMsgDataSupports9Bit() {
		return false;
	}

	@Override
	public boolean isMsgDataSupportsNonEncrypted() {
		return true;
	}

	@Override
	public GenVITextVariableCode[] getVarCodeEnums() {
		return GenVITextVariableCode.CODES;
	}

}
