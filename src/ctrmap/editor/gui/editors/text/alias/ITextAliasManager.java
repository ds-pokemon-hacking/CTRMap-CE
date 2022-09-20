package ctrmap.editor.gui.editors.text.alias;

import ctrmap.editor.gui.editors.text.loaders.ITextArcType;

public interface ITextAliasManager {
	public String getMsgidAlias(MessageTag tag);
	
	public void setMsgidAlias(MessageTag tag, String alias);
	public void setMsgidAliasContent(MessageTag tag, String content);
	
	public void setSavedataEnable(boolean value);
	
	public void saveDataManual(ITextArcType arcType, int fileNo);
	
	public static class MessageTag {
		public ITextArcType arc;
		public int fileNo;
		public int msgId;
		
		public MessageTag(ITextArcType arc, int fileNo, int msgId) {
			this.arc = arc;
			this.fileNo = fileNo;
			this.msgId = msgId;
		}
	}
}
