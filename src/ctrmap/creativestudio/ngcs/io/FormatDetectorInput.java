
package ctrmap.creativestudio.ngcs.io;

import xstandard.io.base.impl.ext.data.DataIOStream;

public class FormatDetectorInput {
	public String fileName;
	
	public String magic4str;
	public int magic4int;
	
	public byte[] first16Bytes;
	
	public DataIOStream stream;
	
	public boolean isMagic4Str(String check) {
		if (magic4str.length() < check.length()) {
			return false;
		}
		if (check.length() != 4) {
			return magic4str.substring(0, check.length()).equals(check);
		}
		return magic4str.equals(check);
	}
	
	public boolean isMagic2(int magic2) {
		return (magic4int & 0xFFFF) == magic2;
	}
}
