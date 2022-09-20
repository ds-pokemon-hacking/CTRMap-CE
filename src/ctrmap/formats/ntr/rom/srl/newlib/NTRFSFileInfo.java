package ctrmap.formats.ntr.rom.srl.newlib;

public class NTRFSFileInfo {

	public String name;
	public boolean isDirectory;

	public int offset;
	public int endOffset;

	public static NTRFSFileInfo makeDirInfo(String dirName) {
		NTRFSFileInfo fi = new NTRFSFileInfo();
		fi.isDirectory = true;
		fi.name = dirName;
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
