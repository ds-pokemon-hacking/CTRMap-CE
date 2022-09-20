
package ctrmap.editor.gui.editors.scenegraph.tree;

public class ContainerNode extends ScenegraphExplorerNode {
	
	public static final int RESID_DEFAULT = 1;
	
	private String name;
	private int resId;

	public ContainerNode(String name, int resid, ScenegraphJTree tree) {
		super(tree);
		this.name = name;
		this.resId = resid;
	}
	
	@Override
	public int getIconResourceID() {
		return resId == -1 ? RESID_DEFAULT : resId;
	}

	@Override
	public String getNodeName() {
		return name;
	}

	@Override
	public Object getContent() {
		return name;
	}
}
