package ctrmap.creativestudio.ngcs.tree;

import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.camera.CameraAnimation;
import ctrmap.renderer.scene.animation.camera.CameraBoneTransform;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.NamedResource;
import xstandard.util.ListenableList;

public class AnimationTransformNode extends CSNode {

	public static final int RESID = 0x420201;

	private AbstractAnimation anm;
	private AbstractBoneTransform bt;

	public AnimationTransformNode(AbstractAnimation anm, AbstractBoneTransform bt, CSJTree tree) {
		super(tree, getContentType(bt));
		this.anm = anm;
		this.bt = bt;
	}

	@Override
	public int getIconResourceID() {
		return RESID;
	}

	@Override
	public String getNodeName() {
		return bt.name;
	}

	@Override
	public NamedResource getContent() {
		return bt;
	}

	@Override
	public void putForExport(G3DResource dest) {
		switch (getContentType()) {
			case ANIMATION_TARGET_C:
				getDmyCamAnm(dest, anm.frameCount).transforms.add((CameraBoneTransform) bt);
				break;
			case ANIMATION_TARGET_S:
				getDmySkelAnm(dest, anm.frameCount).bones.add((SkeletalBoneTransform) bt);
		}
	}

	@Override
	public NamedResource getReplacement(G3DResource source) {
		switch (getContentType()) {
			case ANIMATION_TARGET:
				return null;
			case ANIMATION_TARGET_C:
				for (CameraAnimation a : source.cameraAnimations) {
					for (CameraBoneTransform bt : a.transforms) {
						if (bt.getClass() == this.bt.getClass()) {
							return bt;
						}
					}
				}
				return null;
			case ANIMATION_TARGET_S:
				SkeletalAnimation skelAnm = getFirst(source.skeletalAnimations);
				if (skelAnm != null) {
					return getFirst(skelAnm.bones);
				}
				return null;
		}
		return null;
	}

	@Override
	public ListenableList getParentList() {
		return anm.getBones();
	}

	private static CSNodeContentType getContentType(AbstractBoneTransform bt) {
		if (bt instanceof CameraBoneTransform) {
			return CSNodeContentType.ANIMATION_TARGET_C;
		}
		else if (bt instanceof SkeletalBoneTransform) {
			return CSNodeContentType.ANIMATION_TARGET_S;
		}
		return CSNodeContentType.ANIMATION_TARGET;
	}

	@Override
	public CSNodeContentType getContentType() {
		return getContentType(bt);
	}

	@Override
	public void setContent(NamedResource cnt) {
		bt = (AbstractBoneTransform) cnt;
	}
}
