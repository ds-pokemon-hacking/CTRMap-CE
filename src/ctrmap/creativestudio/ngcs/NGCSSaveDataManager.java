package ctrmap.creativestudio.ngcs;

import ctrmap.formats.generic.interchange.CMIFFile;
import xstandard.fs.FSFile;
import xstandard.gui.DialogUtils;
import xstandard.gui.file.XFileDialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class NGCSSaveDataManager {

	protected NGCS parent;

	public FSFile save = null;

	protected boolean saved = true;

	public NGCSSaveDataManager(NGCS cs) {
		parent = cs;
	}

	public void callOpen() {
		boolean allow = true;
		if (!saved) {
			allow = callSave(true);
		}
		if (allow) {
			FSFile f = XFileDialog.openFileDialog("Open a CMIF Scene file", CMIFFile.EXTENSION_FILTER);
			if (f != null) {
				save = f;

				parent.importCMIFSeq(save, false);
			}
		}
	}

	public void callClear() {
		boolean b = true;
		if (!saved) {
			b = callSave(true);
		}
		if (b) {
			save = null;
			parent.clear();
		}
	}

	public boolean callSave() {
		return callSave(false);
	}

	protected final boolean callSaveToFile() {
		FSFile dest = save;
		if (dest == null) {
			dest = XFileDialog.openSaveFileDialog(CMIFFile.EXTENSION_FILTER);
		}

		if (dest != null) {
			parent.getCMIF().write(dest);
			save = dest;
		} else {
			return false;
		}
		return true;
	}

	public final void callSaveAs() {
		FSFile dest = XFileDialog.openSaveFileDialog(CMIFFile.EXTENSION_FILTER);
		if (dest != null) {
			save = dest;
			callSaveToFile();
		}
	}

	protected boolean callSave(boolean dialog) {
		parent.save();
		int rsl = JOptionPane.YES_OPTION;
		if (dialog) {
			rsl = DialogUtils.showSaveConfirmationDialog("Scene");
		}
		switch (rsl) {
			case JOptionPane.YES_OPTION:
				if (!callSaveToFile()) {
					return false;
				}

			//fall through
			case JOptionPane.NO_OPTION:
				saved = true;
				return true;	//continue sequence
			case JOptionPane.CANCEL_OPTION:
				return false;
		}
		return false;
	}

	public void raiseSaveFlag() {
		saved = false;
	}
}
