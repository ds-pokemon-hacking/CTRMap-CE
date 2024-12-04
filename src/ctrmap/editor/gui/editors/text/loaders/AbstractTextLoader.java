package ctrmap.editor.gui.editors.text.loaders;

import ctrmap.editor.CTRMap;
import ctrmap.formats.pokemon.text.ITextFile;
import ctrmap.formats.pokemon.text.MessageHandler;
import ctrmap.formats.pokemon.text.MsgStr;
import java.util.List;
import xstandard.fs.FSFile;

public abstract class AbstractTextLoader {

	protected ITextArcType arcType = null;
	protected int textFileId;
	protected CTRMap cm;
	private ITextFile file;

	public abstract MessageHandler getMsgHandler();
	
	public abstract ITextArcType[] getArcTypes();
	
	public abstract boolean checkCanExpandTextArc(ITextArcType type);
	
	public abstract boolean isArcTypeNSFW(ITextArcType type);

	public abstract int getTextArcMax(ITextArcType textArcType);
	
	public abstract FSFile getFileFromArc(ITextArcType type, int fileIndex);
	
	public abstract ITextFile loadFromFile(ITextArcType fileType, FSFile file);

	public ITextFile loadFromArc(ITextArcType type, int file) {
		return loadFromFile(type, getFileFromArc(type, file));
	}

	public AbstractTextLoader(CTRMap cm2) {
		cm = cm2;
	}

	public boolean setArcType(ITextArcType t) {
		if (arcType != t) {
			arcType = t;
			return true;
		}
		return false;
	}

	public ITextArcType getArcType() {
		return arcType;
	}

	@SuppressWarnings("deprecated")
	public final void setTextFile(int file) {
		this.textFileId = file;
		//backwards compatibility
		this.file = loadFromArc(arcType, file);
	}

	@Deprecated
	public int getTextFileId() {
		return textFileId;
	}

	public int getTextArcMax() {
		return getTextArcMax(arcType);
	}
	
	public ITextFile getCurrentFile() {
		return file;
	}

	public List<MsgStr> getMsgStrs() {
		return file.getLines();
	}

	public void insertTextLineContent(int line, String data) {
		file.insertFriendlyLine(line, data);
	}

	public boolean setTextLineContent(int line, String data) {
		return file.setFriendlyLine(line, data);
	}

	public void removeTextLine(int line) {
		file.removeLine(line);
	}

	public void writeCurrentTextFile() {
		if (file != null) {
			file.store();
		}
	}

	public String getBlankLineText(int lineNo) {
		return getMsgHandler().getBlankLineText(lineNo);
	}

}
