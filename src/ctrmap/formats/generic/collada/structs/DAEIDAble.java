package ctrmap.formats.generic.collada.structs;

public interface DAEIDAble {
	public String getID();
	public void setID(String id);
	
	public default String getURL() {
		return "#" + getID();
	}
}
