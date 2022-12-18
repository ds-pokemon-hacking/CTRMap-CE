package ctrmap.renderer.util.camcvt;

import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimation;
import ctrmap.renderer.scene.animation.skeletal.SkeletalAnimationTransformRequest;
import ctrmap.renderer.scene.animation.skeletal.SkeletalBoneTransform;
import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Skeleton;
import xstandard.math.vec.Matrix4;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SkeletalMatrixBakery implements Iterable<Matrix4> {

	private final SkeletalAnimation anm;

	private final Matrix4 mat_tmp = new Matrix4();

	private final SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(0f);

	private final List<SkeletalBoneTransform> transformList = new ArrayList<>();

	private final List<Joint> jointList = new ArrayList<>();

	public SkeletalMatrixBakery(SkeletalAnimation anm, Skeleton skl, Joint jnt) {
		this.anm = anm;

		while (jnt != null) {
			SkeletalBoneTransform jbt = (SkeletalBoneTransform) anm.getBoneTransform(jnt.name);
			if (jbt == null) {
				jbt = new SkeletalBoneTransform();
			}
			transformList.add(jbt);
			jointList.add(jnt);

			jnt = skl.getJoint(jnt.parentName);
		}

		Collections.reverse(transformList);
		Collections.reverse(jointList);
	}

	public float getCurrentFrame() {
		return req.frame;
	}

	public Matrix4 manualBake(float frame) {
		req.frame = frame;
		bake();
		return mat_tmp;
	}

	public Matrix4 manualBakeLocal(float frame) {
		req.frame = frame;
		req.bindJoint = jointList.get(jointList.size() - 1);
		return transformList.get(transformList.size() - 1).getTransformMatrix(req, new Matrix4());
	}

	private void bakeNext() {
		req.frame++;
		bake();
	}

	private void bake() {
		mat_tmp.identity();
		Matrix4 lastMtx = null;
		for (int jidx = 0; jidx < jointList.size(); jidx++) {
			req.bindJoint = jointList.get(jidx);
			Matrix4 mtx = transformList.get(jidx).getTransformMatrix(req, new Matrix4());
			mat_tmp.mul(mtx);
			if (req.bindJoint.isScaleCompensate() && lastMtx != null) {
				mat_tmp.scale(lastMtx.getScale().recip());
			}
			lastMtx = mtx;
		}
	}

	public void reset() {
		req.frame = 0f;
	}

	@Override
	public Iterator<Matrix4> iterator() {
		reset();

		return new Iterator<Matrix4>() {
			@Override
			public boolean hasNext() {
				return req.frame < anm.frameCount;
			}

			@Override
			public Matrix4 next() {
				bakeNext();
				return mat_tmp;
			}
		};
	}
}
