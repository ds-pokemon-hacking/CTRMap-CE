package ctrmap.editor.gui.editors.scenegraph.tree;

import xstandard.util.ListenableList;

public abstract class ScenegraphListener<T> implements ListenableList.ElementChangeListener {

	private ScenegraphExplorerNode node;
	
	public ScenegraphListener(ScenegraphExplorerNode node){
		this.node = node;
	}
	
	public void free() {
		node = null;
	}
	
	protected abstract ScenegraphExplorerNode createNode(T elem);

	@Override
	public void onEntityChange(ListenableList.ElementChangeEvent evt) {
		T obj = (T) evt.element;
		switch (evt.type) {
			case ADD:
				node.addChild(evt.index, createNode(obj));
				break;
			case REMOVE:
				for (int i = 0; i < node.getChildCount(); i++){
					ScenegraphExplorerNode ch = (ScenegraphExplorerNode)node.getChildAt(i);
					if (ch.getContent() == obj){
						node.removeChild(ch);
						break;
					}
				}
				break;
		}
	}
}
