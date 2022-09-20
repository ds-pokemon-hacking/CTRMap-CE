package ctrmap.formats.pokemon.text.crypto;

public class DummyTextCrypto implements ITextCrypto {

	private static DummyTextCrypto INSTANCE;

	public static DummyTextCrypto getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DummyTextCrypto();
		}
		return INSTANCE;
	}

	@Override
	public void begin(int baseKey) {
	}

	@Override
	public char decode(char c) {
		return c;
	}

	@Override
	public char encode(char c) {
		return c;
	}

}
