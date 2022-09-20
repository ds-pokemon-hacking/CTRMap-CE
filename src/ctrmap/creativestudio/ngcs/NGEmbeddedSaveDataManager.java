package ctrmap.creativestudio.ngcs;

import xstandard.gui.DialogUtils;
import javax.swing.JOptionPane;

public class NGEmbeddedSaveDataManager extends NGCSSaveDataManager {

	private Callback callback;

	public NGEmbeddedSaveDataManager(NGCS cs, Callback cb) {
		super(cs);
		callback = cb;
	}

	@Override
	protected boolean callSave(boolean dialog) {
		parent.save();
		int rsl = JOptionPane.YES_OPTION;
		if (dialog) {
			rsl = DialogUtils.showSaveConfirmationDialog("Scene");
		}
		switch (rsl) {
			case JOptionPane.YES_OPTION:
				if (callback != null){
					if (!callback.onSave(parent)){
						if (!callSaveToFile()){
							return false;
						}
					}
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

	public static interface Callback {

		public boolean onSave(NGCS cs);
	}
}
