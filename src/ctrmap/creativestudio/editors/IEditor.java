package ctrmap.creativestudio.editors;

import ctrmap.creativestudio.ngcs.tree.CSNode;

public interface IEditor {

	public void handleObject(Object o);

	public void save();

	public static boolean checkIsCompatibleNG(Object o, Class tgt) {
		if (o != null && o instanceof CSNode) {
			Object content = ((CSNode) o).getContent();
			return content != null && tgt.isAssignableFrom(content.getClass());
		}
		return false;
	}
}
