package ctrmap.formats.pokemon.text;

import java.util.ArrayList;
import xstandard.fs.FSFile;
import xstandard.util.ListenableList;
import java.util.List;

/**
 * X/Y text file reading and decryption, documented in Kaphotics' xytext
 */
public class TextFile implements ITextFile {

	public final List<TextFileSection> sections;
	public TextFileSection mainSection;
	public ListenableList<MsgStr> lines;

	public boolean enableEncryption;

	private FSFile source;
	private MessageHandler hnd;

	public TextFile(FSFile f, MessageHandler handler) {
		sections = new ArrayList<>();
		if (f.exists()) {
			TextFileRW.readLinesForFile(this, f, handler);
		}
		if (sections.isEmpty()) {
			sections.add(new TextFileSection(this));
		}
		setMainSection(0);
		source = f;
		hnd = handler;
	}

	public void setMainSection(int index) {
		mainSection = sections.get(index);
		lines = mainSection.lines;
	}

	public String[] getLinesArray() {
		return mainSection.getLinesArray();
	}

	public String[] getFriendlyLinesArray() {
		return mainSection.getFriendlyLinesArray();
	}

	@Override
	public void store() {
		if (source != null) {
			source.setBytes(TextFileRW.getBytesForFile(this, hnd));
		}
	}

	public MessageHandler getHandler() {
		return hnd;
	}

	public int indexOfOrDefault(String v, int defValue) {
		return mainSection.indexOfOrDefault(v, defValue);
	}

	public int indexOf(String v) {
		return mainSection.indexOf(v);
	}

	public String getOrDefault(int num, String defaultV) {
		return mainSection.getOrDefault(num, defaultV);
	}

	public String getLine(int num) {
		return mainSection.getLine(num);
	}

	@Override
	public void removeLine(int num) {
		for (TextFileSection section : sections) {
			section.removeLine(num);
		}
	}

	public int getLineCount() {
		return mainSection.getLineCount();
	}

	public int appendLine(String value) {
		return mainSection.appendLine(value);
	}

	@Override
	public boolean setFriendlyLine(int num, String value) {
		return mainSection.setFriendlyLine(num, value);
	}

	@Override
	public void insertFriendlyLine(int num, String value) {
		for (TextFileSection section : sections) {
			section.insertFriendlyLine(num, value);
		}
	}

	public void insertLine(int num, String value) {
		for (TextFileSection section : sections) {
			section.insertLine(num, value);
		}
	}

	public boolean setLine(int num, String value) {
		return mainSection.setLine(num, value);
	}

	@Override
	public List<MsgStr> getLines() {
		return mainSection.getLines();
	}

	@Override
	public List<TextFileSection> getSubFiles() {
		return sections;
	}
}
