package ctrmap.formats.generic.interchange;

import xstandard.io.base.iface.IOStream;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.structs.StringTable;
import xstandard.io.structs.TemporaryOffset;
import java.io.IOException;

public class CMIFWriter extends DataIOStream {

	private StringTable stringTable;

	private TemporaryOffset offsetToStringTable;

	public CMIFWriter() {
		super();
		stringTable = new StringTable(this);
	}
	
	public CMIFWriter(IOStream io) {
		super(io);
		stringTable = new StringTable(this);
	}

	public void setOffsetToStringTableHere() throws IOException {
		offsetToStringTable = new TemporaryOffset(this);
	}

	@Override
	public void writeString(String str) {
		stringTable.putStringOffset(str);
	}

	@Override
	public void close() throws IOException {
		pad(CMIFFile.IF_PADDING);
		if (offsetToStringTable != null) {
			offsetToStringTable.setHere();
		}
		writeInt(stringTable.getStringCount());
		stringTable.writeTable();
		super.close();
	}
}
