package ctrmap.formats.pokemon.text;

import xstandard.fs.FSFile;
import xstandard.util.ListenableList;
import java.util.List;

/**
 * X/Y text file reading and decryption, documented in Kaphotics' xytext
 */
public class TextFile implements ITextFile {

	public final ListenableList<MsgStr> lines;
	
	public boolean enableEncryption;

	private FSFile source;
	private MessageHandler hnd;

	public TextFile(FSFile f, MessageHandler handler) {
		lines = new ListenableList<>();
		if (f.exists()) {
			TextFileRW.readLinesForFile(this, f, handler);
		}
		source = f;
		hnd = handler;
	}

	public void addListener(ListenableList.ElementChangeListener l) {
		lines.addListener(l);
	}

	public String[] getLinesArray() {
		String[] l = new String[lines.size()];
		for (int i = 0; i < l.length; i++){
			l[i] = getLine(i);
		}
		return l;
	}
	
	public String[] getFriendlyLinesArray() {
		String[] l = new String[lines.size()];
		for (int i = 0; i < l.length; i++){
			l[i] = TextFileFriendlizer.getFriendlized(getLine(i));
		}
		return l;
	}

	@Override
	public void store() {
		if (source != null) {
			source.setBytes(TextFileRW.getBytesForFile(this, hnd));
		}
	}
	
	public MessageHandler getHandler(){
		return hnd;
	}
	
	public int indexOfOrDefault(String v, int defValue){
		int idx = indexOf(v);
		if (idx != -1){
			return idx;
		}
		return defValue;
	}
	
	public int indexOf(String v){
		for (int i = 0; i < lines.size(); i++){
			if (lines.get(i).value.equals(v)){
				return i;
			}
		}
		return -1;
	}

	public String getOrDefault(int num, String defaultV) {
		if (num < 0 || num >= lines.size()) {
			return defaultV;
		}
		return lines.get(num).value;
	}

	public String getLine(int num) {
		return getOrDefault(num, null);
	}

	@Override
	public void removeLine(int num) {
		if (num >= 0 && num < lines.size()) {
			lines.remove(num);
		}
	}
	
	public int getLineCount() {
		return lines.size();
	}
	
	private boolean getShouldEncode9BitLine(int line){
		int lidx = line - 1;
		if (lidx < 0){
			lidx = line + 1;
		}
		if (lidx < lines.size()){
			return lines.get(lidx).encode9Bit;
		}
		return false;
	}

	public int appendLine(String value) {
		int idx = lines.size();
		lines.add(new MsgStr(value, getShouldEncode9BitLine(idx)));
		return idx;
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
		if (num < lines.size()) {
			lines.add(num, new MsgStr(value, getShouldEncode9BitLine(num)));
		}
		else {
			setLine(num, value);
		}
	}

	public boolean setLine(int num, String value) {
		boolean changed = false;
		while (num >= lines.size()) {
			appendLine(hnd.getBlankLineText(lines.size()));
			changed = true;
		}
		if (!value.equals(lines.get(num).value)){
			changed = true;
		}
		lines.get(num).value = value;
		lines.fireModifyEvent(lines.get(num));
		return changed;
	}

	@Override
	public List<MsgStr> getLines() {
		return lines;
	}
}
