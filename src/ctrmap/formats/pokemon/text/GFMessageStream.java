package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.crypto.ITextCrypto;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GFMessageStream extends DataIOStream {

	private static final char[] COMPRESSED_CHAR_TABLE = new char[]{
		'Œ', 'œ', 'Ş', 'ş', '‘', '“', '”', '„', '…',
		'①', '②', '③', '④', '⑤', '⑥', '⑦', '⑧', '⑨', '⑩', '⑪', '⑫', '⑬', '⑭', '⑮', '⑯', '⑰', '⑱', '⑲', '⑳',
		'⑴', '⑵', '⑶', '⑷', '⑸', '⑹', '⑺', '⑻', '⑼', '⑽', '⑾', '⑿', '⒀', '⒁', '⒂', '⒃', '⒄', '⒅', '⒆', '⒇',
		'･'
	};
	private static final int COMPRESSED_CHAR_SPECIAL_BASE_INDEX = 256;

	public static final int CMD_BEGIN_9BIT = 0xF100;

	//Message handler, charset and stream info
	private MessageHandler hnd;
	private char terminator;
	private ITextCrypto crypto;
	private Map<Character, String> specialCharacters;
	private Map<String, Character> specialCharactersInv;

	//Decryption/encryption
	private int keyIter = 0;

	//Character size control
	private int charBuf = 0;
	private int bufIdx = 0;
	private int currentCharSize = Character.SIZE;
	private char currentCharMask = 0xFFFF;

	public GFMessageStream(IOStream io, MessageHandler handler) {
		super(io);
		hnd = handler;

		cacheCharacterSet();
		resetCryptoCounter();
	}

	private void cacheCharacterSet() {
		terminator = hnd.getTerminator();
		specialCharacters = hnd.getCharLUT();

		specialCharactersInv = new HashMap<>();
		for (Map.Entry<Character, String> e : specialCharacters.entrySet()) {
			specialCharactersInv.put(e.getValue(), e.getKey());
		}
	}

	public void setCrypto(ITextCrypto crypto) {
		this.crypto = crypto;
		setUpKeyByIter();
	}
	
	public final void resetCryptoCounter() {
		keyIter = 0;
		if (crypto != null) {
			setUpKeyByIter();
		}
	}

	public MsgStr readString(int maxChars) throws IOException {
		char c;
		StringBuilder sb = new StringBuilder();
		int offs = getPosition();
		int maxBytes = maxChars * 2;

		boolean is9Bit = false;
		while ((getPosition() - offs < maxBytes || bufIdx >= currentCharSize) && (c = readChar()) != terminator) {
			handleChar(sb, c);
			is9Bit |= c == CMD_BEGIN_9BIT;
		}
		int garbageSize = maxChars - ((getPosition() - offs) >> 1);
		while (garbageSize-- != 0) {
			crypto.decode((char) readUnsignedShort()); //garbage data after terminator
		}

		notifyNewLine();
		return new MsgStr(sb.toString(), is9Bit);
	}

	private void handleChar(StringBuilder sb, char c) throws IOException {
		if (!isStandardCharSize()) {
			if (c >= COMPRESSED_CHAR_SPECIAL_BASE_INDEX && c < COMPRESSED_CHAR_SPECIAL_BASE_INDEX + COMPRESSED_CHAR_TABLE.length) {
				c = COMPRESSED_CHAR_TABLE[c - COMPRESSED_CHAR_SPECIAL_BASE_INDEX];
			}
		}
		if (c == CMD_BEGIN_9BIT) {
			setCurrentCharSize(9);
		} else if (c == currentCharMask) {
			setStandardCharSize();
		} else if (c == hnd.getVarIdentChar()) {
			TextVariable var = new TextVariable(this, hnd);
			sb.append(var.toString());
		} else if (specialCharacters.containsKey(c)) {
			sb.append(specialCharacters.get(c));
		} else if ((c & 0xFF00) == 0xFF00) {
			sb.append(hnd.handleUnrecognizedCharacter(c, this));
		} else {
			sb.append(c);
		}
	}

	public void writeString(MsgStr msgstr) throws IOException {
		writeString(msgstr, -1);
	}

	public void writeString(MsgStr msgstr, int paddingChars) throws IOException {
		int start = getPosition();
		String str = msgstr.value;
		if (msgstr.encode9Bit && hnd.isMsgDataSupports9Bit()) {
			writeChar(CMD_BEGIN_9BIT);
			setCurrentCharSize(9);
		}
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			for (Map.Entry<String, Character> e : specialCharactersInv.entrySet()) {
				if (str.startsWith(e.getKey(), i)) {
					c = e.getValue();
					i += e.getKey().length() - 1;
					break;
				}
			}

			if (c == '[') {
				int commandEnd = str.indexOf(']', i);
				if (commandEnd != -1) {
					String commandText = str.substring(i, commandEnd + 1);
					TextVariable var = new TextVariable(commandText, hnd);
					var.write(this);
					i = commandEnd;
					continue;
				}
			}

			if (!isStandardCharSize()) {
				for (int index = 0; index < COMPRESSED_CHAR_TABLE.length; index++) {
					if (COMPRESSED_CHAR_TABLE[index] == c) {
						c = (char) (index + COMPRESSED_CHAR_SPECIAL_BASE_INDEX);
						break;
					}
				}
			}

			writeChar(c);
		}

		if (!isStandardCharSize()) {
			if (bufIdx != 0) {
				charBuf |= (terminator << bufIdx);
				flushCharBuf();
			}
			setStandardCharSize();
		}
		writeChar(terminator);
		if (str.trim().isEmpty() && !str.isEmpty()) {
			writeChar(terminator);
		}

		if (paddingChars != -1) {
			int written = (getPosition() - start) >> 1;
			int paddingCount = paddingChars - written;
			while (paddingCount-- != 0) {
				writeChar(terminator);
			}
		}

		notifyNewLine();
	}

	private void setStandardCharSize() {
		setCurrentCharSize(Character.SIZE);
	}

	private boolean isStandardCharSize() {
		return currentCharSize == Character.SIZE;
	}

	@Override
	public char readChar() throws IOException {
		char r;
		if (!isStandardCharSize()) {
			if (bufIdx < currentCharSize) {
				char dc = getDecryptedChar();
				charBuf |= (dc << bufIdx);
				bufIdx += Character.SIZE;
			}
			r = (char) (charBuf & currentCharMask);
			bufIdx -= currentCharSize;
			charBuf >>= currentCharSize;
		} else {
			r = getDecryptedChar();
		}

		return r;
	}

	@Override
	public void writeChar(int v) throws IOException {
		char c = (char) v;

		if (isStandardCharSize()) {
			writeShort(getEncryptedChar(c));
		} else {
			charBuf |= ((c & currentCharMask) << bufIdx);
			bufIdx += currentCharSize;
			if (bufIdx >= Character.SIZE) {
				flushCharBuf();
			}
		}
	}

	private void flushCharBuf() throws IOException {
		writeShort(getEncryptedChar((char) charBuf));
		charBuf >>= Character.SIZE;
		bufIdx -= Character.SIZE;
	}

	private char getDecryptedChar() throws IOException {
		return crypto.decode((char) readUnsignedShort());
	}

	private char getEncryptedChar(char val) {
		return crypto.encode(val);
	}

	private void notifyNewLine() {
		keyIter++;
		setUpKeyByIter();
		setStandardCharSize();
	}

	private void setUpKeyByIter() {
		crypto.begin(keyIter);
	}

	private void setCurrentCharSize(int bitSize) {
		charBuf = 0;
		bufIdx = 0;
		currentCharSize = bitSize;
		int sh = (Character.SIZE - bitSize);
		currentCharMask = (char) ((0xFFFF << sh & 0xFFFF) >>> sh);
	}
}
