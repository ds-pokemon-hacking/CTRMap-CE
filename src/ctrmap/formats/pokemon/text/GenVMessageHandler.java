package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.varcodes.GenVTextVariableCode;
import ctrmap.formats.pokemon.text.varcodes.TextVariableCode;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class GenVMessageHandler implements MessageHandler {

	public static final Map<Character, String> SPEC_CHARS_V = new HashMap<>();

	static {
		SPEC_CHARS_V.put((char) 0xFFFE, "\n");
		SPEC_CHARS_V.put((char) 0x246D, "♂");
		SPEC_CHARS_V.put((char) 0x246E, "♀");
		SPEC_CHARS_V.put((char) 0x2486, "Pk");
		SPEC_CHARS_V.put((char) 0x2487, "Mn");
	}

	@Override
	public Map<Character, String> getCharLUT() {
		return SPEC_CHARS_V;
	}

	@Override
	public char getVarIdentChar() {
		return 0xF000;
	}

	@Override
	public boolean getHasVarLengthFirst() {
		return false;
	}

	@Override
	public char getTerminator(){
		return 0xFFFF;
	}
	
	@Override
	public String handleUnrecognizedCharacter(char c, GFMessageStream strm) {
		if (c == 0xFF0D) {
			return String.valueOf(c);
		}
		return "\\u" + Integer.toHexString(c);
	}

	@Override
	public String getBlankLineText(int lineNo) {
		return "";
	}

	@Override
	public boolean isMsgDataPadded() {
		return false;
	}

	@Override
	public boolean isMsgDataSupports9Bit() {
		return true;
	}

	@Override
	public boolean isMsgDataSupportsNonEncrypted() {
		return false;
	}

	@Override
	public GenVTextVariableCode[] getVarCodeEnums() {
		return GenVTextVariableCode.CODES;
	}
}
