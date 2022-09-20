package ctrmap.formats.pokemon.text.crypto;

public interface ITextCrypto {
	public void begin(int base);
	
	public char decode(char c);
	public char encode(char c);
}
