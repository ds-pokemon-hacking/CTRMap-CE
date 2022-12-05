package ctrmap.util.tools.cont;

import xstandard.fs.FSUtil;
import xstandard.util.ArraysEx;
import xstandard.fs.TempFileAccessor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

public class AGFCTransferable implements Transferable {

	private DataFlavor[] flavours = new DataFlavor[]{DataFlavor.javaFileListFlavor};

	public final ContFileView cfv;
	private File tempFile = null;
	
	public AGFCTransferable(ContFileView cfv){
		this.cfv = cfv;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavours;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavour) {
		return DataFlavor.javaFileListFlavor.equals(flavour) || flavour == cfv.uniqueDataFlavour;
	}

	private void ensureTempFile(){
		if (tempFile == null){
			tempFile = TempFileAccessor.createTempFile(cfv.getFileName());
			FSUtil.writeBytesToFile(tempFile, cfv.getData());
		}
	}
	
	@Override
	public Object getTransferData(DataFlavor flavour) throws UnsupportedFlavorException, IOException {
		if (isDataFlavorSupported(flavour)){
            ensureTempFile();
			return ArraysEx.asList(tempFile);
		}
		else{
            return null;
		}
	}

}
