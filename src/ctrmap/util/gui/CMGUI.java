package ctrmap.util.gui;

import ctrmap.CTRMapResources;
import ctrmap.formats.pokemon.WorldObject;
import ctrmap.editor.gui.editors.common.AbstractPerspective;
import xstandard.fs.FSUtil;
import xstandard.gui.DialogUtils;
import xstandard.util.ReflectionHash;
import java.awt.Component;
import java.io.InputStream;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import xstandard.res.ResourceAccess;

public class CMGUI {

	public static JRadioButton createGraphicalToolButton(String prefix) {
		String baseRes = "toolbar/_tool_" + prefix;

		InputStream testRes = CTRMapResources.ACCESSOR.getStream(baseRes + "_stale.png");
		if (testRes != null) {
			JRadioButton ret = new JRadioButton(new ImageIcon(FSUtil.readStreamToBytes(testRes)));
			ret.setRolloverIcon(getImageIconFromResource(baseRes + "_rollover.png"));
			ret.setPressedIcon(getImageIconFromResource(baseRes + "_active.png"));
			ret.setSelectedIcon(getImageIconFromResource(baseRes + "_active.png"));
			ret.setRolloverEnabled(true);
			return ret;
		} else {
			return new JRadioButton(prefix);
		}
	}

	public static ImageIcon getImageIconFromResource(String respath) {
		return new ImageIcon(CTRMapResources.ACCESSOR.getByteArray(respath));
	}

	public static <T extends WorldObject> T addToComboBoxAndListWorldObjSimple(T o, List<T> list, JComboBox box, AbstractPerspective editors) {
		return addToComboBoxAndListWorldObjSimple(o, list, box, editors, null);
	}

	public static <T extends WorldObject> T addToComboBoxAndListWorldObjSimple(T o, List<T> list, JComboBox box, AbstractPerspective editors, String namePrefix) {
		return addToComboBoxAndListWorldObjSimple(o, list, box, editors, namePrefix, false);
	}
	
	public static <T extends WorldObject> T addToComboBoxAndListWorldObjSimple(T o, List<T> list, JComboBox box, AbstractPerspective editors, String namePrefix, boolean isSuffix) {
		o.setWPos(editors.getIdealCenterCameraPosByZeroPlane());
		int size = list.size();
		list.add(o);
		String name;
		if (namePrefix == null) {
			name = String.valueOf(size);
		}
		else if (isSuffix) {
			name = size + " - " + namePrefix;
		}
		else {
			name = namePrefix + size;
		}
		box.addItem(name);
		box.setSelectedIndex(size);
		return o;
	}

	public static <T> int removeFromComboBoxAndList(T o, List<T> list, JComboBox box) {
		if (o != null) {
			int index = list.indexOf(o);
			if (index != -1) {
				list.remove(index);
				box.removeItemAt(index);
				box.setSelectedIndex(Math.min(index, box.getItemCount() - 1));
				return index;
			}
		}
		return -1;
	}

	public static boolean commonSaveDataSequence(Component parent, ReflectionHash hash, boolean dialog, String subject, boolean plural, Runnable callbackOnSave) {
		if (hash.getChangeFlagRecalcIfNeeded()) {
			int rsl = JOptionPane.YES_OPTION;
			if (dialog) {
				rsl = DialogUtils.showSaveConfirmationDialog(parent, subject, plural);
			}
			switch (rsl) {
				case JOptionPane.YES_OPTION:
					callbackOnSave.run();
					hash.resetChangedFlag();
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
					return false;
			}
		}
		return true;
	}
}
