package ctrmap.editor.gui.editors.text.loaders;

import ctrmap.editor.CTRMap;
import ctrmap.formats.pokemon.text.ITextFile;
import ctrmap.formats.pokemon.text.MessageHandler;
import ctrmap.formats.pokemon.text.MsgStr;
import java.util.List;
import xstandard.fs.FSFile;

public abstract class AbstractTextLoader {

	protected ITextArcType arcType = null;
	protected int textFileId = -1;
	protected CTRMap cm;

	private ITextFile file;
	private int subFileIndex = -1;
	
	private boolean syncSections = true;

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
	
	public boolean isSyncSections() {
		return syncSections;
	}
	
	public void setSyncSections(boolean syncSections) {
		this.syncSections = syncSections;
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
		selectSection(subFileIndex);
	}
	
	public void setTextFile(ITextArcType fileType, FSFile file) {
		this.file = loadFromFile(fileType, file);
		selectSection(subFileIndex);
	}
	
	public void selectSection(int sectionIndex) {
		List<? extends ITextFile> subFiles = file.getSubFiles();
		if (subFiles.isEmpty()) {
			subFileIndex = -1;
		} else {
			if (sectionIndex < 0 || sectionIndex >= subFiles.size()) {
				sectionIndex = 0;
			}
			subFileIndex = sectionIndex;
		}
	}
	
	public int getSelectedSection() {
		return subFileIndex;
	}

	@Deprecated
	public int getTextFileId() {
		return textFileId;
	}

	public int getTextArcMax() {
		return getTextArcMax(arcType);
	}

	public List<? extends ITextFile> getSections() {
		return file.getSubFiles();
	}

	private ITextFile getSectionOrFile() {
		if (subFileIndex != -1) {
			return getSections().get(subFileIndex);
		}
		return file;
	}
	
	private ITextFile getSectionOrSyncedFile() {
		if (syncSections) {
			return file;
		}
		return getSectionOrFile();
	}

	public ITextFile getCurrentFile() {
		return getSectionOrFile();
	}

	public List<MsgStr> getMsgStrs() {
		return getCurrentFile().getLines();
	}

	public void insertTextLineContent(int line, String data) {
		getSectionOrSyncedFile().insertFriendlyLine(line, data);
	}

	public boolean setTextLineContent(int line, String data) {
		return getCurrentFile().setFriendlyLine(line, data);
	}

	public void removeTextLine(int line) {
		getSectionOrSyncedFile().removeLine(line);
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
