package ctrmap.formats.generic.interchange;

import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.StringIO;
import java.io.IOException;

public class OtherFileUtil {
	
	public static CMIFFile.OtherFile readOtherFile(DataIOStream dis) throws IOException{
		CMIFFile.OtherFile o = new CMIFFile.OtherFile();
		o.name = StringIO.readStringWithAddress(dis);
		o.data = new byte[dis.readInt()];
		dis.readFully(o.data);
		return o;
	}
	
	public static void writeOtherFile(CMIFFile.OtherFile o, CMIFWriter dos) throws IOException{
		dos.writeString(o.getName());
		dos.writeInt(o.data.length);
		dos.write(o.data);
		dos.pad(CMIFFile.IF_PADDING);
	}
}
