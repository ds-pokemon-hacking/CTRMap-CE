package ctrmap.formats.ntr.rom.srl.newlib;

public class NTRFSFileInfo {

	public boolean isDirectory;

	public int offset;
	public int endOffset;

	public static NTRFSFileInfo makeDirInfo() {
		NTRFSFileInfo fi = new NTRFSFileInfo();
		fi.isDirectory = true;
		return fi;
	}
	
	public static NTRFSFileInfo makeFileInfo(int startOff, int endOff) {
		NTRFSFileInfo fi = new NTRFSFileInfo();
		fi.isDirectory = false;
		fi.offset = startOff;
		fi.endOffset = endOff;
		return fi;
	}
}
