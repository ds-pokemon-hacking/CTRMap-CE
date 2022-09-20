package ctrmap.creativestudio.editors;

import ctrmap.creativestudio.ngcs.tree.CSNode;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.gui.components.ComponentUtils;
import xstandard.gui.components.listeners.DocumentAdapterEx;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;

public class NameTextField extends JTextField {

	private NamedResource res;
	private CSNode node;

	private boolean allowEv = false;
	
	private List<NameChangeListener> listeners = new ArrayList<>();

	public NameTextField() {
		super();

		getDocument().addDocumentListener(new DocumentAdapterEx() {
			@Override
			public void textChangedUpdate(DocumentEvent e) {
				if (allowEv && node != null && res != null && isEnabled()) {
					String text = ComponentUtils.getDocTextFromField(NameTextField.this);
					if (!text.isEmpty()) {
						String oldName = res.getName();
						res.setName(text);
						for (NameChangeListener l : listeners) {
							l.onNameChanged(oldName, text);
						}
						node.updateThis();
					}
				}
			}
		});
		setText("<N/A>");
	}
	
	public void addListener(NameChangeListener l) {
		ArraysEx.addIfNotNullOrContains(listeners, l);
	}

	public void loadNode(CSNode node) {
		this.node = node;
		if (node != null) {
			res = node.getContent();
		} else {
			res = null;
		}
		allowEv = false;
		setText(res != null ? res.getName() : "<N/A>");
		boolean isRenameable = false;
		if (res != null) {
			try {
				res.setName(res.getName());
				isRenameable = true;
			} catch (UnsupportedOperationException ex) {

			}
		}
		setEditable(isRenameable);
		allowEv = true;
	}
	
	public static interface NameChangeListener {
		public void onNameChanged(String oldName, String newName);
	}
}
