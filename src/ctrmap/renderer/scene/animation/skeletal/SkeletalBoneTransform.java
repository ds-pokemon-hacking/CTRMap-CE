package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.backends.RenderAllocator;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.AnimatedValue;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.model.Joint;
import xstandard.math.MathEx;
import xstandard.math.vec.Matrix4;
import xstandard.util.ArraysEx;
import java.util.List;

public class SkeletalBoneTransform extends AbstractBoneTransform {

	public KeyFrameList tx = new KeyFrameList();
	public KeyFrameList ty = new KeyFrameList();
	public KeyFrameList tz = new KeyFrameList();

	public KeyFrameList rx = new KeyFrameList();
	public KeyFrameList ry = new KeyFrameList();
	public KeyFrameList rz = new KeyFrameList();

	public KeyFrameList sx = new KeyFrameList();
	public KeyFrameList sy = new KeyFrameList();
	public KeyFrameList sz = new KeyFrameList();

	public SkeletalBoneTransform() {

	}

	public SkeletalBoneTransform(SkeletalBoneTransform src) {
		name = src.name;
		rx.addAll(src.rx);
		ry.addAll(src.ry);
		rz.addAll(src.rz);
		tx.addAll(src.tx);
		ty.addAll(src.ty);
		tz.addAll(src.tz);
		sx.addAll(src.sx);
		sy.addAll(src.sy);
		sz.addAll(src.sz);
	}
	
	private boolean getIsRotationNonHermite() {
		KeyFrameList[] kfls = new KeyFrameList[]{rx, ry, rz};
		for (KeyFrameList l : kfls) {
			for (KeyFrame kf : l) {
				if (kf.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
					return false;
				}
			}
		}
		return true;
	}

	private float searchExistingRotFrame(float frame, boolean backwards) {
		KeyFrame x = getNearKeyFrame(rx, frame, backwards);
		KeyFrame y = getNearKeyFrame(ry, frame, backwards);
		KeyFrame z = getNearKeyFrame(rz, frame, backwards);
		if (backwards) {
			if (x != null && (x.frame <= frame) && (y == null || x.frame > y.frame) && (z == null || x.frame > z.frame)) {
				return x.frame;
			} else if (y != null && (y.frame <= frame) && (z == null || y.frame > z.frame)) {
				return y.frame;
			}
			return (z != null && z.frame <= frame) ? z.frame : -1f;
		} else {
			if (x != null && (x.frame >= frame) && (y == null || x.frame < y.frame) && (z == null || x.frame < z.frame)) {
				return x.frame;
			} else if (y != null && (y.frame >= frame) && (z == null || y.frame < z.frame)) {
				return y.frame;
			}
			return (z != null && z.frame >= frame) ? z.frame : -1f;
		}
	}

	public Matrix4 getTransformMatrix(SkeletalAnimationTransformRequest req, Matrix4 dest) {
		boolean nonHermite = getIsRotationNonHermite();
		
		SkeletalAnimationTransformRequest reqNoRot = new SkeletalAnimationTransformRequest(req);
		reqNoRot.rotation = false;
		SkeletalAnimationTransformRequest reqRotOnly = new SkeletalAnimationTransformRequest(req);
		reqRotOnly.translation = false;
		reqRotOnly.scale = false;

		float floorFrame = nonHermite ? searchExistingRotFrame(req.frame, true) : -1f;
		float ceilFrame = nonHermite ? searchExistingRotFrame(req.frame, false) : -1f;
		if (ceilFrame == -1f) {
			ceilFrame = (float) Math.ceil(req.frame);
		}
		if (floorFrame == -1f) {
			floorFrame = (float) Math.floor(req.frame);
		}

		SkeletalAnimationFrame fMain = getFrame(reqNoRot);
		reqRotOnly.frame = floorFrame;
		SkeletalAnimationFrame fRotLeft = getFrame(reqRotOnly);
		reqRotOnly.frame = ceilFrame;
		SkeletalAnimationFrame fRotRight = getFrame(reqRotOnly);

		if (req.translation) {
			dest.translate(fMain.tx.value, fMain.ty.value, fMain.tz.value);
		}
		float frameDiff = req.frame - floorFrame;
		if (req.disableInterpolation || frameDiff == 0f || (floorFrame == ceilFrame)) {
			dest.rotate(fRotLeft.getRotation());
		} else {
			dest.rotate(
				fRotLeft.getRotation().slerp(
					fRotRight.getRotation(),
					MathEx.clamp(0f, 1f, frameDiff / (ceilFrame - floorFrame))
				));
		}
		if (req.scale) {
			dest.scale(fMain.sx.value, fMain.sy.value, fMain.sz.value);
		}
		
		fMain.free();
		fRotLeft.free();
		fRotRight.free();

		return dest;
	}

	public SkeletalAnimationFrame getFrame(float frame) {
		return getFrame(new SkeletalAnimationTransformRequest(frame));
	}

	public SkeletalAnimationFrame getFrame(float frame, Joint bindJoint, boolean manualAlloc) {
		SkeletalAnimationTransformRequest req = new SkeletalAnimationTransformRequest(frame, manualAlloc);
		req.bindJoint = bindJoint;
		return getFrame(req);
	}

	protected static void setAnmFrameJoint(SkeletalAnimationFrame frm, SkeletalAnimationTransformRequest request) {
		Joint bindJoint = request.bindJoint;
		if (bindJoint != null) {
			if (request.translation) {
				frm.tx.setIfNotExists(bindJoint.position.x);
				frm.ty.setIfNotExists(bindJoint.position.y);
				frm.tz.setIfNotExists(bindJoint.position.z);
			}

			if (request.rotation) {
				frm.rx.setIfNotExists(bindJoint.rotation.x);
				frm.ry.setIfNotExists(bindJoint.rotation.y);
				frm.rz.setIfNotExists(bindJoint.rotation.z);
			}

			if (request.scale) {
				frm.sx.setIfNotExists(bindJoint.scale.x);
				frm.sy.setIfNotExists(bindJoint.scale.y);
				frm.sz.setIfNotExists(bindJoint.scale.z);
			}
		}
	}

	public SkeletalAnimationFrame getFrame(SkeletalAnimationTransformRequest request) {
		SkeletalAnimationFrame frm = new SkeletalAnimationFrame(request.useManualAllocation);

		float frame = request.frame;
		boolean disableInterpolation = request.disableInterpolation;

		if (request.translation) {
			getValueAt(tx, frame, disableInterpolation, frm.tx);
			getValueAt(ty, frame, disableInterpolation, frm.ty);
			getValueAt(tz, frame, disableInterpolation, frm.tz);
		}

		if (request.rotation) {
			getValueAt(rx, frame, disableInterpolation, frm.rx);
			getValueAt(ry, frame, disableInterpolation, frm.ry);
			getValueAt(rz, frame, disableInterpolation, frm.rz);
		}

		if (request.scale) {
			getValueAt(sx, frame, disableInterpolation, frm.sx);
			getValueAt(sy, frame, disableInterpolation, frm.sy);
			getValueAt(sz, frame, disableInterpolation, frm.sz);
		}

		setAnmFrameJoint(frm, request);

		return frm;
	}

	public void pushFullBakedFrame(float frame, Vec3f t, Vec3f r, Vec3f s) {
		addValueToKfList(frame, t.x, tx);
		addValueToKfList(frame, t.y, ty);
		addValueToKfList(frame, t.z, tz);

		addValueToKfList(frame, r.x, rx);
		addValueToKfList(frame, r.y, ry);
		addValueToKfList(frame, r.z, rz);

		addValueToKfList(frame, s.x, sx);
		addValueToKfList(frame, s.y, sy);
		addValueToKfList(frame, s.z, sz);
	}

	public void pushFullBakedFrame(float frame, SkeletalAnimationFrame frm) {
		addValueToKfList(frame, frm.tx, tx);
		addValueToKfList(frame, frm.ty, ty);
		addValueToKfList(frame, frm.tz, tz);

		addValueToKfList(frame, frm.rx, rx);
		addValueToKfList(frame, frm.ry, ry);
		addValueToKfList(frame, frm.rz, rz);

		addValueToKfList(frame, frm.sx, sx);
		addValueToKfList(frame, frm.sy, sy);
		addValueToKfList(frame, frm.sz, sz);
	}

	public void removeKeyFramesAt(float frame) {
		removeKfsByFrame(tx, frame);
		removeKfsByFrame(ty, frame);
		removeKfsByFrame(tz, frame);

		removeKfsByFrame(rx, frame);
		removeKfsByFrame(ry, frame);
		removeKfsByFrame(rz, frame);

		removeKfsByFrame(sx, frame);
		removeKfsByFrame(sy, frame);
		removeKfsByFrame(sz, frame);
	}

	private static void removeKfsByFrame(KeyFrameList kfl, float frame) {
		for (int i = 0; i < kfl.size(); i++) {
			if (kfl.get(i).frame == frame) {
				kfl.remove(i);
				i--;
			}
		}
	}

	private static void addValueToKfList(float frame, float value, KeyFrameList kfl) {
		AnimatedValue val = RenderAllocator.allocAnimatedValue();
		addValueToKfList(frame, val, kfl);
		RenderAllocator.freeAnimatedValue(val);
	}

	private static void addValueToKfList(float frame, AnimatedValue value, KeyFrameList kfl) {
		if (value.exists) {
			int i = 0;
			for (; i < kfl.size(); i++) {
				if (kfl.get(i).frame == frame) {
					kfl.remove(i);
					break;
				}
				if (kfl.get(i).frame > frame) {
					break;
				}
			}
			kfl.add(i, new KeyFrame(frame, value.value));
		}
	}

	@Override
	public List<KeyFrameList> getAllKfLists() {
		return ArraysEx.asList(tx, ty, tz, rx, ry, rz, sx, sy, sz);
	}
	
	public boolean hasTranslation() {
		return KeyFrameList.existAny(tx, ty, tz);
	}
	
	public boolean hasRotation() {
		return KeyFrameList.existAny(rx, ry, rz);
	}
	
	public boolean hasScale() {
		return KeyFrameList.existAny(sx, sy, sz);
	}

	public boolean exists() {
		return KeyFrameList.existAny(tx, ty, tz, rx, ry, rz, sx, sy, sz);
	}
}
