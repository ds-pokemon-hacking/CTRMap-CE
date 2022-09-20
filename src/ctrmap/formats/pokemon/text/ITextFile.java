package ctrmap.formats.pokemon.text;

import java.util.List;

public interface ITextFile {
	public void store();
	
	public List<MsgStr> getLines();
	
	public boolean setFriendlyLine(int num, String value);
	public void insertFriendlyLine(int num, String value);
	
	public void removeLine(int num);
}
