package ctrmap.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class CTRMapUIManager {

	private final CTRMap cm;

	private final Map<String, JMenu> menus = new HashMap<>();

	public CTRMapUIManager(CTRMap cm) {
		this.cm = cm;
	}

	public JMenu getMenu(String name) {
		JMenu menu = menus.get(name);
		if (menu == null) {
			menu = new JMenu(name);
			menus.put(name, menu);
			cm.menubar.add(menu);
		}
		return menu;
	}

	public JMenuItem addMenuItem(String menuName, String itemName, ActionCallback callback) {
		JMenuItem item = new JMenuItem(itemName);
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				callback.actionPerformed(cm);
			}
		});
		getMenu(menuName).add(item);
		return item;
	}

	public void removeMenuItem(JMenuItem t) {
		for (JMenu menu : menus.values()) {
			menu.remove(t);
		}
	}
	
	public static interface ActionCallback {
		public void actionPerformed(CTRMap ctrmap);
	}
}
