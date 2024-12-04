package ctrmap.creativestudio.ngcs.tree;

import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.util.ModelProcessor;
import xstandard.gui.DialogUtils;
import xstandard.util.ListenableList;

public class SkeletonNode extends ContainerNode {

	public static final int RESID = 0x421101;

	public SkeletonNode(String name, ListenableList<Joint> list, CSJTree tree) {
		super(name, CSNodeContentType.JOINT, list, RESID, tree);

		registerAction("Replace", this::callReplace);
	}

	@Override
	public CSNodeContentType getContentType() {
		return CSNodeContentType.MODEL; //import as model
	}

	@Override
	public NamedResource getReplacement(G3DResource source) {
		if (!source.models.isEmpty()) {
			return source.models.get(0).skeleton;
		}
		return null;
	}

	@Override
	public void replaceContent(NamedResource res) {
		Skeleton skeleton = (Skeleton) res;
		try {
			ModelNode mdlNode = descend(ModelNode.class);
			Model mdl = mdlNode.getContent();
			ModelProcessor.transplantSkeleton(mdl, skeleton);
			rebind(skeleton.getJoints());
			mdlNode.rebuildSkeletonNode();
		} catch (IllegalArgumentException ex) {
			DialogUtils.showErrorMessage(getCS(), "Incompatible skeleton", "This model contains references to joints that are not present on the replacement skeleton.\n(" + ex.getMessage() + ")");
		}
	}
}
