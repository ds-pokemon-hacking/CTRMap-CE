package ctrmap.creativestudio.ngcs;

import ctrmap.creativestudio.ngcs.tree.CSNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

public class NGCSNodePopupMenu extends JPopupMenu{
	
	private CSNode node;
	
	public NGCSNodePopupMenu(CSNode n){
		node = n;
		
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				node.callAction(e.getActionCommand());
				setVisible(false);
			}
		};
		
		for (CSNode.CSNodeAction action : node.getActions()){
			JMenuItem itm = new JMenuItem(action.name);
			itm.setActionCommand(action.name);
			itm.setRolloverEnabled(true);
			itm.setEnabled(true);
			itm.addActionListener(al);
			add(itm);
		}
	}
	
	public boolean makesSense(){
		return node != null && getComponentCount() > 0;
	}
}
