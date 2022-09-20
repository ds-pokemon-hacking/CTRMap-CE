package ctrmap.creativestudio.ngcs.rtldr;

import xstandard.fs.FSFile;
import xstandard.gui.file.XFileDialog;
import xstandard.gui.file.ExtensionFilter;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class NGCSUIManager {

	private final JMenuBar menuBar;

	public NGCSUIManager(JMenuBar menuBar) {
		this.menuBar = menuBar;
	}

	public void addMenuItem(String menuName, JMenuItem item) {
		for (Component comp : menuBar.getComponents()) {
			if (comp instanceof JMenu) {
				JMenu menu = (JMenu) comp;
				if (Objects.equals(menu.getText(), menuName)) {
					menu.add(item);
					break;
				}
			}
		}
	}

	public void removeMenuItem(JMenuItem item) {
		for (Component comp : menuBar.getComponents()) {
			if (comp instanceof JMenu) {
				JMenu menu = (JMenu) comp;
				menu.remove(item);
			}
		}
	}

	public static JMenuItem createSimpleImportMenuItem(String name, ExtensionFilter filter, NGCSContentAccessor contentAccessor) {
		JMenuItem i = new JMenuItem(name);
		i.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FSFile file = XFileDialog.openFileDialog(filter);

				contentAccessor.importFile(file);
			}
		});
		return i;
	}

	public static JMenuItem createExportMenuItem(String name, ExtensionFilter filter, Frame uiParent, NGCSContentAccessor contentAccessor, ExportCallback callback) {
		JMenuItem i = new JMenuItem(name);
		i.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FSFile dest = XFileDialog.openSaveFileDialog(filter);

				if (dest != null) {
					callback.export(dest, uiParent, contentAccessor);
				}
			}
		});
		return i;
	}

	public static interface ExportCallback {

		public void export(FSFile dest, Frame uiParent, NGCSContentAccessor contentAccessor);
	}
}
