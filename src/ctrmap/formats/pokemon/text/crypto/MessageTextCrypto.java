package ctrmap.formats.pokemon.text.crypto;

public class MessageTextCrypto implements ITextCrypto {

	public static final int STEP_KEY = 0x2983;

	private static MessageTextCrypto INSTANCE;

	public static MessageTextCrypto getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new MessageTextCrypto();
		}
		return INSTANCE;
	}

	private char key;

	@Override
	public void begin(int base) {
		key = (char) (base * STEP_KEY);
	}

	@Override
	public char decode(char c) {
		c = (char) (c ^ key);
		advanceCharKey();
		return c;
	}

	@Override
	public char encode(char c) {
		c = (char) (key ^ c);
		advanceCharKey();
		return c;
	}

	private void advanceCharKey() {
		//rotate the key right by 13
		key = (char) (((key << 3) | (key >> 13)) & 0xffff);
	}
}
