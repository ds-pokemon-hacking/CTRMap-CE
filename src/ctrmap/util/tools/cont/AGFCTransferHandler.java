
package ctrmap.util.tools.cont;

import java.awt.datatransfer.Transferable;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceMotionListener;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 */
public class AGFCTransferHandler extends TransferHandler implements DragSourceMotionListener{

	@Override
	public Transferable createTransferable(JComponent c){
		if (c instanceof ContFileView){
			return new AGFCTransferable((ContFileView)c);
		}
		
		return null;
	}
	
	@Override
	public void dragMouseMoved(DragSourceDragEvent dsde) {
	}
	
	@Override
	public int getSourceActions(JComponent c){
		if (c instanceof ContFileView){
			return TransferHandler.MOVE;
		}
		
		return TransferHandler.NONE;
	}
}
