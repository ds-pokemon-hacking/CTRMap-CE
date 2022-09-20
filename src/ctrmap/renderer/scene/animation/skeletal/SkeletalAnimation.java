package ctrmap.renderer.scene.animation.skeletal;

import ctrmap.renderer.scene.model.Skeleton;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.animation.AbstractAnimation;
import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.AnimatedValue;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.util.AnimeProcessor;
import xstandard.fs.FSFile;
import xstandard.util.ListenableList;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SkeletalAnimation extends AbstractAnimation {

	public Skeleton skeleton = null;
	public ListenableList<SkeletalBoneTransform> bones = new ListenableList<>();

	public boolean hasSkeleton() {
		return skeleton != null;
	}

	@Override
	public ListenableList<? extends AbstractBoneTransform> getBones() {
		return bones;
	}

	private static void optRotKflWithPrependedValue(List<KeyFrame> kfl, AnimatedValue value) {
		if (value.exists && !kfl.isEmpty()) {
			kfl.add(0, new KeyFrame(kfl.get(0).frame - 1, value.value));
			AnimeProcessor.optimizeRotKFL(kfl, 30f);
			kfl.remove(0);
		}
	}

	private static List<KeyFrame> getKeyFramesSince(List<KeyFrame> kfl, float since) {
		List<KeyFrame> l = new ArrayList<>();
		for (KeyFrame kf : kfl) {
			if (kf.frame >= since) {
				l.add(new KeyFrame(kf));
			}
		}
		return l;
	}

	@Override
	public void callOptimize() {
		AnimeProcessor.optimizeSkeletalAnimation(this);
	}

	public static void keyframesFromVectors(List<Vec3f> vectors, List<KeyFrame> out0, List<KeyFrame> out1, List<KeyFrame> out2) {
		out0.clear();
		out1.clear();
		out2.clear();

		int frm = 0;

		for (Vec3f v : vectors) {
			out0.add(new KeyFrame(frm, v.x, 0));
			out1.add(new KeyFrame(frm, v.y, 0));
			out2.add(new KeyFrame(frm, v.z, 0));
			frm++;
		}
	}

	public void dumpToFile(FSFile f) {
		try {
			BufferedWriter w = new BufferedWriter(new OutputStreamWriter(f.getNativeOutputStream()));
			/*for (int i = 0; i < frameCount; i++) {
				w.append("FRAME " + i);
				w.newLine();
				for (SkeletalBoneTransform bt : bones) {
					w.append("----" + bt.name + "----");
					w.newLine();
					w.append(bt.getFrame(i).toString());
					w.newLine();
				}
			}*/
			for (SkeletalBoneTransform bt : bones) {
				w.append("---- " + bt.name + " ----");
				dumpKFL(w, "TX", bt.tx);
				dumpKFL(w, "TY", bt.ty);
				dumpKFL(w, "TZ", bt.tz);
				dumpKFL(w, "RX", bt.rx);
				dumpKFL(w, "RY", bt.ry);
				dumpKFL(w, "RZ", bt.rz);
				dumpKFL(w, "SX", bt.sx);
				dumpKFL(w, "SY", bt.sy);
				dumpKFL(w, "SZ", bt.sz);
			}
			w.close();
		} catch (IOException ex) {
			Logger.getLogger(SkeletalAnimation.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void dumpKFL(BufferedWriter w, String label, KeyFrameList kfl) throws IOException {
		if (kfl.isEmpty()) {
			return;
		}
		w.newLine();
		w.append(label);
		w.newLine();
		for (KeyFrame kf : kfl) {
			w.append(kf.frame + ": " + kf.value);
			if (kf.interpolation == KeyFrame.InterpolationMethod.HERMITE) {
				w.append(" | " + kf.inSlope + " / " + kf.outSlope);
			}
			w.newLine();
		}
	}
}
