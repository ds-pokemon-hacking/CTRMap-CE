package ctrmap.creativestudio.ngcs.tree;

import xstandard.util.ListenableList;
import javax.swing.tree.TreeNode;

public abstract class CSNodeListener<T> implements ListenableList.ElementChangeListener {

	private CSNode node;

	public CSNodeListener(CSNode node) {
		this.node = node;
	}

	protected abstract CSNode createNode(T elem);

	protected boolean isAllowEntityChange(T elem) {
		return true;
	}

	@Override
	public void onEntityChange(ListenableList.ElementChangeEvent evt) {
		T obj = (T) evt.element;
		if (!isAllowEntityChange(obj)) {
			return;
		}
		int index = -1;
		for (int i = 0; i < node.getChildCount(); i++) {
			CSNode ch = (CSNode) node.getChildAt(i);
			if (ch.getContent() == obj) {
				index = i;
				break;
			}
		}
		if (index == -1 && evt.type == ListenableList.ElementChangeType.ADD) {
			index = node.getChildCount();
		}
		if (index != -1) {
			switch (evt.type) {
				case ADD:
					node.addChild(index, createNode(obj));
					break;
				case REMOVE:
					node.removeChild((CSNode)node.getChildAt(index));
					break;
				case MODIFY:
					TreeNode child = node.getChildAt(index);
					if (child instanceof CSNode) {
						((CSNode) child).updateThis();
					}
					break;
			}
		}
	}
}
