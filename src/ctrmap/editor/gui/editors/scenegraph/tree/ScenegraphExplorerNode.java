package ctrmap.editor.gui.editors.scenegraph.tree;

import ctrmap.editor.gui.editors.scenegraph.ScenegraphExplorer;
import xstandard.gui.components.tree.CustomJTreeNode;
import javax.swing.tree.TreeNode;

public abstract class ScenegraphExplorerNode extends CustomJTreeNode {

	protected final ScenegraphJTree tree;

	public ScenegraphExplorerNode(ScenegraphJTree tree) {
		this.tree = tree;
	}

	public void onSelected(ScenegraphExplorer gui) {
		gui.loadObjToEditor(null, null);
	}

	public void addChild(ScenegraphExplorerNode ch) {
		addChild(getChildCount(), ch);
	}

	public void addChild(int index, ScenegraphExplorerNode ch) {
		if (index < 0 || index > getChildCount()) {
			throw new ArrayIndexOutOfBoundsException("Can not add child " + ch + " to " + this + " - index " + index + " out of bounds for length " + getChildCount() + "!");
		}
		tree.getModel().insertNodeInto(ch, this, index);
	}

	public void removeChild(ScenegraphExplorerNode ch) {
		if (ch != null) {
			tree.getModel().removeNodeFromParent(ch);
		}
	}

	public ScenegraphExplorerNode getChildByContent(Object cnt) {
		int count = getChildCount();
		for (int i = 0; i < count; i++) {
			TreeNode n = getChildAt(i);
			if (n instanceof ScenegraphExplorerNode) {
				if (((ScenegraphExplorerNode) n).getContent() == cnt) {
					return (ScenegraphExplorerNode) n;
				}
			}
		}
		return null;
	}

	public abstract Object getContent();
}
