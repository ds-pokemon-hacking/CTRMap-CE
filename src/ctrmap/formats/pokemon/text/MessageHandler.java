package ctrmap.formats.pokemon.text;

import java.util.Map;

public interface MessageHandler {

	public Map<Character, String> getCharLUT();
	
	public boolean isMsgDataPadded();
	
	public boolean isMsgDataSupportsNonEncrypted();
	
	public boolean isMsgDataSupports9Bit();
	
	public char getTerminator();

	public char getVarIdentChar();

	public TextVariableCode[] getVarCodeEnums();

	public abstract String getBlankLineText(int lineNo);

	public boolean getHasVarLengthFirst();

	public String handleUnrecognizedCharacter(char c, GFMessageStream strm);
}
