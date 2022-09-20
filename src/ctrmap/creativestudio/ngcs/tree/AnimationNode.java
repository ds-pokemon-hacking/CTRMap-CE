package ctrmap.creativestudio.ngcs.tree;

import ctrmap.creativestudio.editors.IEditor;
import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.creativestudio.ngcs.NGEditorController;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraViewpointBoneTransform;
import ctrmap.renderer.scene.animation.material.MatAnimBoneTransform;
import ctrmap.renderer.scene.animation.material.MaterialAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.animation.visibility.VisibilityAnimation;
import ctrmap.renderer.scene.animation.visibility.VisibilityBoneTransform;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.gui.components.tree.CheckboxTreeCell;
import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.List;

public class AnimationNode extends CSNode {

	public static final int RESID = 0x420200;

	private final CheckboxTreeCell checkbox = new CheckboxTreeCell();

	private ListenableList<? extends AbstractAnimation> list;
	private AbstractAnimation anm;

	private CSNodeListener<AbstractBoneTransform> btListener;

	public AnimationNode(ListenableList<? extends AbstractAnimation> list, AbstractAnimation anm, CSJTree tree) {
		super(tree, getContentType(anm));
		setTreeCellComponent(checkbox);
		this.list = list;
		this.anm = anm;

		btListener = new CSNodeListener<AbstractBoneTransform>(this) {
			@Override
			protected CSNode createNode(AbstractBoneTransform elem) {
				return new AnimationTransformNode(anm, elem, tree);
			}
		};

		checkbox.addActionListener(((e) -> {
			if (checkbox.isChecked()) {
				getCS().playAnimation(anm);
			} else {
				getCS().stopAnimation(anm);
			}
		}));

		registerAction("Add target", this::callAddAnmTarget);
		
		rebuildSubNodes();
	}

	protected final void rebuildSubNodes() {
		removeAllChildren();
		for (AbstractBoneTransform bt : anm.getBones()) {
			addChild(new AnimationTransformNode(anm, bt, tree));
		}

		anm.getBones().addListener(btListener);
	}

	@Override
	public void onReplaceFinish(Object oldObj) {
		rebuildSubNodes();
		NGCS cs = getCS();
		if (cs.stopAnimation((AbstractAnimation) oldObj)) {
			cs.playAnimation(anm);
		}
	}
	
	@Override
	public IEditor getEditor(NGEditorController editors) {
		switch (getContentType()) {
			case ANIMATION_C:
				return editors.cameraAnimeEditor;
			case ANIMATION_M:
				return editors.matAnimEditor;
			case ANIMATION_S:
				return editors.sklAnmEditor;
		}
		return null;
	}

	public void callAddAnmTarget() {
		ListenableList l = anm.getBones();

		switch (getContentType()) {
			case ANIMATION_M:
				l.add(new MatAnimBoneTransform());
				break;
			case ANIMATION_S:
				l.add(new SkeletalBoneTransform());
				break;
			case ANIMATION_V:
				l.add(new VisibilityBoneTransform());
				break;
			case ANIMATION_C:
				//TODO other camera types
				l.add(new CameraViewpointBoneTransform());
				break;
		}
		G3DResource.renameDuplicates(l, "AnimationTarget");
	}

	@Override
	public void onNodeRemoved() {
		anm.getBones().removeListener(btListener);
		NGCS cs = getCS();
		cs.stopAnimation(anm);
	}

	@Override
	public void putForExport(G3DResource rsc) {
		switch (getContentType()) {
			case ANIMATION_C:
				rsc.cameraAnimations.add((CameraAnimation) anm);
				break;
			case ANIMATION_S:
				rsc.skeletalAnimations.add((SkeletalAnimation) anm);
				break;
			case ANIMATION_M:
				rsc.materialAnimations.add((MaterialAnimation) anm);
				break;
			case ANIMATION_V:
				rsc.visibilityAnimations.add((VisibilityAnimation) anm);
				break;
		}
	}

	@Override
	public NamedResource getReplacement(G3DResource rsc) {
		List<? extends NamedResource> l = new ArrayList<>();
		switch (getContentType()) {
			case ANIMATION_C:
				l = rsc.cameraAnimations;
				break;
			case ANIMATION_M:
				l = rsc.materialAnimations;
				break;
			case ANIMATION_S:
				l = rsc.skeletalAnimations;
				break;
			case ANIMATION_V:
				l = rsc.visibilityAnimations;
				break;
		}
		return getFirst(l);
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return anm.name;
	}

	@Override
	public NamedResource getContent() {
		return anm;
	}

	@Override
	public void setContent(NamedResource cnt) {
		anm = (AbstractAnimation) cnt;
	}

	@Override
	public ListenableList getParentList() {
		return list;
	}

	private static CSNodeContentType getContentType(AbstractAnimation anm) {
		if (anm instanceof SkeletalAnimation) {
			return CSNodeContentType.ANIMATION_S;
		} else if (anm instanceof VisibilityAnimation) {
			return CSNodeContentType.ANIMATION_V;
		} else if (anm instanceof MaterialAnimation) {
			return CSNodeContentType.ANIMATION_M;
		} else if (anm instanceof CameraAnimation) {
			return CSNodeContentType.ANIMATION_C;
		}
		return CSNodeContentType.OTHER;
	}

	@Override
	public CSNodeContentType getContentType() {
		return getContentType(anm);
	}
}
