package ctrmap.renderer.scene.animation;

import ctrmap.renderer.util.AnimeProcessor;
import xstandard.math.MathEx;
import java.util.ArrayList;
import java.util.Collection;

public class KeyFrameList extends ArrayList<KeyFrame> {

	public LoopMode loopMode = LoopMode.NONE;

	public float startFrame = 0f;
	public float endFrame = 0f;

	public KeyFrameList() {
		super();
	}

	public KeyFrameList(KeyFrameList kfl) {
		this(kfl, false);
	}

	public KeyFrameList(KeyFrameList kfl, boolean copy) {
		super();
		if (copy) {
			copy(kfl);
		} else {
			set(kfl);
		}
	}

	public KeyFrameList(Collection<? extends KeyFrame> kfl) {
		super(kfl);
	}

	public float normalizeFrame(float frame) {
		float firstFrame = startFrame;
		float lastFrameAbs = (endFrame == 0 ? lastFrameTime() : endFrame) - firstFrame;

		frame -= firstFrame;

		if (frame > lastFrameAbs) {
			switch (loopMode) {
				case REPEAT:
					frame = (frame % lastFrameAbs);
					break;
				case MIRRORED_REPEAT:
					frame %= lastFrameAbs;
					frame = lastFrameAbs - frame;
					break;
			}
		}

		return frame;
	}

	public KeyFrame.InterpolationMethod getInterpolationHighest() {
		KeyFrame.InterpolationMethod highest = KeyFrame.InterpolationMethod.STEP;
		for (KeyFrame kf : this) {
			if (kf.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
				return KeyFrame.InterpolationMethod.HERMITE;
			}
			if (kf.interpolation == KeyFrame.InterpolationMethod.LINEAR) {
				highest = KeyFrame.InterpolationMethod.LINEAR;
			}
		}
		return highest;
	}

	public void copy(KeyFrameList kfl) {
		clear();
		for (KeyFrame kf : kfl) {
			add(new KeyFrame(kf));
		}
		this.loopMode = kfl.loopMode;
		this.startFrame = kfl.startFrame;
		this.endFrame = kfl.endFrame;
	}

	public void set(KeyFrameList kfl) {
		clear();
		addAll(kfl);
		this.loopMode = kfl.loopMode;
		this.startFrame = kfl.startFrame;
		this.endFrame = kfl.endFrame;
	}

	public KeyFrame getByFrameRounddown(float frame) {
		double round = Math.floor(frame);
		for (KeyFrame kf : this) {
			if (Math.floor(kf.frame) == round) {
				return kf;
			}
		}
		return null;
	}
	
	public float firstFrameTime(float defaultValue) {
		if (!isEmpty()) {
			return get(0).frame;
		}
		return defaultValue;
	}

	public float firstFrameTime() {
		return KeyFrameList.this.firstFrameTime(0f);
	}

	public float lastFrameTime() {
		if (!isEmpty()) {
			return get(size() - 1).frame;
		}
		return 0f;
	}
	
	public float firstFrameValue(float defaultValue) {
		if (!isEmpty()) {
			return get(0).value;
		}
		return defaultValue;
	}
	
	public float lastFrameValue(float defaultValue) {
		if (!isEmpty()) {
			return get(size() - 1).value;
		}
		return defaultValue;
	}
	
	public float frameValue(float frame, float defaultValue) {
		AnimatedValue val = AbstractBoneTransform.getValueAt(this, frame);
		if (val.exists) {
			return val.value;
		}
		return defaultValue;
	}

	public static final void radToDeg(KeyFrameList... lists) {
		for (KeyFrameList l : lists) {
			for (KeyFrame kf : l) {
				kf.value *= MathEx.RADIANS_TO_DEGREES;
			}
		}
	}

	public static final boolean existAll(KeyFrameList... lists) {
		for (KeyFrameList l : lists) {
			if (l.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static final boolean existAny(KeyFrameList... lists) {
		for (KeyFrameList l : lists) {
			if (!l.isEmpty()) {
				return true;
			}
		}
		return false;
	}
	
	public static final float nextFrame(float frame, KeyFrameList... list) {
		float min = Float.MAX_VALUE;
		for (KeyFrameList kfl : list) {
			for (KeyFrame kf : kfl) {
				if (kf.frame > frame && kf.frame < min) {
					min = kf.frame;
				}
			}
		}
		return min;
	}

	public static final boolean existAnyRounddownFrame(float frame, KeyFrameList... lists) {
		return existAnyRounddownFrame(frame, 30f, lists);
	}
	
	public static final boolean existAnyRounddownFrame(float frame, float fps, KeyFrameList... lists) {
		float maxDiff = 1f / AnimeProcessor.convFpsFrom30(1f, fps);
		
		for (KeyFrameList l : lists) {
			for (KeyFrame kf : l) {
				if (kf.frame >= frame && (kf.frame - frame) < maxDiff) {
					return true;
				}
			}
		}
		return false;
	}
}
