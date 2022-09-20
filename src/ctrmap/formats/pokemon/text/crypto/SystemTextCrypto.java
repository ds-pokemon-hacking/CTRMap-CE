package ctrmap.formats.pokemon.text.crypto;

public class SystemTextCrypto implements ITextCrypto {

	private int key;

	public SystemTextCrypto(int baseKey) {
		key = baseKey;
	}

	@Override
	public void begin(int base) {
	}

	@Override
	public char decode(char c) {
		return (char) (nextCharKey() ^ c);
	}

	@Override
	public char encode(char c) {
		return (char) (c ^ nextCharKey());
	}

	private char nextCharKey() {
		key = 0x41C64E6D * key + 0x6073;
		return (char) (key >>> 16);
	}
}
