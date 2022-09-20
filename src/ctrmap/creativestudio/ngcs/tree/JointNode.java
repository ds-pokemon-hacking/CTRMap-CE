package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;
import java.util.Objects;

public class JointNode extends CSNode {

	public static final int RESID = 0x420103;

	private Joint jnt;

	public JointNode(Joint jnt, CSJTree tree) {
		super(tree);
		this.jnt = jnt;

		for (Joint maybeChild : jnt.parentSkeleton.getJoints()) {
			if (Objects.equals(maybeChild.parentName, jnt.name)) {
				if (jnt == maybeChild) {
					throw new RuntimeException("Broken joint - parented to itself at " + jnt.name);
				}
				addChild(new JointNode(maybeChild, tree));
			}
		}
		jnt.parentSkeleton.getJoints().addListener(new CSNodeListener<Joint>(this) {
			@Override
			protected boolean isAllowEntityChange(Joint elem) {
				return Objects.equals(elem.parentName, jnt.name);
			}

			@Override
			protected CSNode createNode(Joint elem) {
				return new JointNode(elem, tree);
			}
		});

		registerActionPrepend("Add child", this::callAddChildJoint);
	}

	public void callAddChildJoint() {
		Joint child = new Joint();
		child.name = "Joint";
		child.parentName = jnt.name;
		int index = 1;
		while (jnt.parentSkeleton.getJoint(child.name) != null) {
			child.name = "Joint" + index++;
		}
		jnt.parentSkeleton.addJoint(child);
		setExpansionState(true);
	}

	public void changeParentToJoint(Joint newJointParent) {
		ContainerNode skel = descend(ContainerNode.class);
		if (skel != null) {
			CSNode newParent;
			if (newJointParent != null) {
				newParent = skel.ascendByContent(JointNode.class, newJointParent);
			} else {
				newParent = skel;
			}
			if (newParent != null) {
				removeFromParent();
				if (newParent.isNodeAncestor(this)) {
					System.err.println("ERROR: Node " + getNodeName() + " is an ancestor of " + newParent.getNodeName());
				}
				newParent.addChild(this);
			}
			jnt.parentSkeleton.getJoints().fireModifyEvent(jnt);
		}
	}

	@Override
	public IEditor getEditor(NGEditorController editors) {
		return editors.jointEditor;
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return jnt.name;
	}

	@Override
	public NamedResource getContent() {
		return jnt;
	}

	@Override
	public ListenableList getParentList() {
		return jnt.parentSkeleton.getJoints();
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.JOINT;
	}

	@Override
	public void setContent(NamedResource cnt) {
		jnt = (Joint) cnt;
	}

	@Override
	public void putForExport(G3DResource rsc) {
		getDmyModel(rsc).skeleton.addJoint(jnt);
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		return getFirst(getDmyModel(rsc).skeleton.getJoints());
	}
}
