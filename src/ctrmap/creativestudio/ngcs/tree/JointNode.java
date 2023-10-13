package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.util.ModelProcessor;
import java.util.ArrayList;
import java.util.List;
import xstandard.util.ListenableList;
import java.util.Objects;
import javax.swing.tree.TreeNode;

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
		jnt.parentName = newJointParent == null ? null : newJointParent.name;
		ContainerNode skel = descend(ContainerNode.class);
		if (skel != null) {
			CSNode newParent;
			if (newJointParent != null) {
				newParent = skel.ascendByContent(JointNode.class, newJointParent);
			} else {
				newParent = skel;
			}
			if (newParent != null) {
				//removeFromParent();
				if (newParent.isNodeAncestor(this)) {
					System.err.println("ERROR: Node " + getNodeName() + " is an ancestor of " + newParent.getNodeName());
				}
				newParent.addChild(this);
			}
			jnt.parentSkeleton.getJoints().fireModifyEvent(jnt);
		} else {
			System.err.println("Could not descend to skeleton container!!");
		}
	}

	@Override
	public void callRemove() {
		//Do not override onNodeRemoved as it would apply recursively
		//CALL ORDER HERE IS IMPORTANT
		//First we change the parents while the node is still linked
		Joint newParent = jnt.getParent();
		List<TreeNode> childrenStatic = new ArrayList<>();
		for (int i = 0; i < getChildCount(); i++) {
			childrenStatic.add(getChildAt(i));
		}
		for (TreeNode child : childrenStatic) {
			if (child instanceof JointNode) {
				JointNode cj = (JointNode) child;
				cj.changeParentToJoint(newParent);
			}
		}
		int index = jnt.getIndex();
		int replacementIndex = 0;
		if (jnt.getParent() != null) {
			replacementIndex = jnt.getParent().getIndex();
		}
		//Now we retrieve the parent model (still linked)
		ModelNode mn = descend(ModelNode.class);
		//Node can now be safely unlinked, actually remove it
		super.callRemove();
		if (mn != null) {
			//Only update here after the joint was actually removed
			ModelProcessor.updateIndicesOnJointRemoved(mn.getContent(), index, replacementIndex);
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
