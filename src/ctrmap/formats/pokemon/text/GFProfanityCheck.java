package ctrmap.formats.pokemon.text;

import ctrmap.formats.pokemon.text.crypto.SystemTextCrypto;
import xstandard.fs.FSFile;
import xstandard.util.ListenableList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GFProfanityCheck implements ITextFile {

	private static final int ENTRY_SIZE_CHARS = 32;
	private static final int CRYPTO_BASE_KEY = 0x72012891;

	private final FSFile source;
	private final MessageHandler handler;

	public final ListenableList<MsgStr> entries = new ListenableList<>();

	public GFProfanityCheck(FSFile file, MessageHandler hnd) {
		source = file;
		this.handler = hnd;

		try (GFMessageStream in = new GFMessageStream(file.getIO(), hnd)) {
			in.setCrypto(new SystemTextCrypto(CRYPTO_BASE_KEY));

			int entryCount = in.getLength() / (ENTRY_SIZE_CHARS * Character.BYTES);

			for (int i = 0; i < entryCount; i++) {
				entries.add(in.readString(ENTRY_SIZE_CHARS));
			}
		} catch (IOException ex) {
			Logger.getLogger(GFProfanityCheck.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void store() {
		try (GFMessageStream out = new GFMessageStream(source.getIO(), handler)) {
			out.setCrypto(new SystemTextCrypto(CRYPTO_BASE_KEY));
			for (MsgStr entry : entries) {
				entry.encode9Bit = false;
				out.writeString(entry, ENTRY_SIZE_CHARS);
			}
		} catch (IOException ex) {
			Logger.getLogger(GFProfanityCheck.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public List<MsgStr> getLines() {
		return entries;
	}

	@Override
	public boolean setFriendlyLine(int num, String value) {
		return setLine(num, TextFileFriendlizer.getDefriendlized(value));
	}

	@Override
	public void insertFriendlyLine(int num, String value) {
		insertLine(num, TextFileFriendlizer.getDefriendlized(value));
	}

	public void insertLine(int num, String value) {
		if (num < entries.size()) {
			entries.add(num, new MsgStr(value));
		} else {
			setLine(num, value);
		}
	}

	public boolean setLine(int num, String value) {
		boolean changed = false;
		while (num >= entries.size()) {
			entries.add(new MsgStr(handler.getBlankLineText(entries.size())));
			changed = true;
		}
		if (!value.equals(entries.get(num).value)) {
			changed = true;
		}
		entries.get(num).value = value;
		entries.fireModifyEvent(entries.get(num));
		return changed;
	}

	@Override
	public void removeLine(int num) {
		if (num >= 0 && num < entries.size()) {
			entries.remove(num);
		}
	}
}
