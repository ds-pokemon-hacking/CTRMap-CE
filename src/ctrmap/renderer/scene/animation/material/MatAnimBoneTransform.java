package ctrmap.renderer.scene.animation.material;

import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.AnimatedValue;
import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import java.util.ArrayList;
import java.util.List;

public class MatAnimBoneTransform extends AbstractBoneTransform {

	public List<String> textureNames = new ArrayList<>();

	public KeyFrameList[] mtx = new KeyFrameList[3];
	public KeyFrameList[] mty = new KeyFrameList[3];

	public KeyFrameList[] mrot = new KeyFrameList[3];

	public KeyFrameList[] msx = new KeyFrameList[3];
	public KeyFrameList[] msy = new KeyFrameList[3];

	public KeyFrameList[] textureIndices = new KeyFrameList[3];

	public final RGBAKeyFrameGroup[] materialColors = new RGBAKeyFrameGroup[MaterialColorType.values().length];

	public boolean hasCoordinator(int index) {
		return !(mtx[index].isEmpty() && mty[index].isEmpty() && mrot[index].isEmpty() && msx[index].isEmpty() && msy[index].isEmpty());
	}

	public boolean hasMapper(int index) {
		return !textureIndices[index].isEmpty();
	}

	public MatAnimBoneTransform() {
		createArrayLists(mtx);
		createArrayLists(mty);
		createArrayLists(mrot);
		createArrayLists(msx);
		createArrayLists(msy);
		createArrayLists(textureIndices);
		for (int i = 0; i < materialColors.length; i++) {
			materialColors[i] = new RGBAKeyFrameGroup();
		}
	}

	private static AnimatedValue getTranslation(KeyFrameList kflT, KeyFrameList kflS, float frame) {
		KeyFrame near = getNearKeyFrame(kflT, frame, true);
		if (near != null && near.interpolation == KeyFrame.InterpolationMethod.STEP) {
			return new AnimatedValue(near.value);
		}
		float floorFrame = (float) Math.floor(frame);

		AnimatedValue left = getValueAt(kflT, floorFrame);
		AnimatedValue right = getValueAt(kflT, (float) Math.ceil(frame));

		AnimatedValue scale = getValueAt(kflS, frame);

		if (left.exists && right.exists) {
			if (!scale.exists) {
				scale.value = 1f;
			}
			float invScale = 1f / scale.value;

			float limit = invScale * 0.5f;

			if (Math.abs(right.value - left.value) > limit) {
				//1.0f
				//0.1f
				float leftBase = left.value % invScale; //0.0
				float rightBase = right.value % invScale; //0.1

				float diffAbs = Math.abs(rightBase - leftBase);

				float frameDiff = frame - floorFrame;

				if (diffAbs > limit) {
					if (Math.abs(rightBase + invScale - leftBase) < diffAbs) {
						return new AnimatedValue(frameDiff * (rightBase + invScale - leftBase) + leftBase);
					} else if (diffAbs > invScale * 0.12f) {
						return left;
					}
				}
				float val = frameDiff * (rightBase - leftBase) + leftBase;
				return new AnimatedValue(val);
			}
		}
		return getValueAt(kflT, frame);
	}

	public MaterialAnimationFrame getFrame(float frame, int coordinator) {
		//System.out.println("getfrm coord " + coordinator);
		MaterialAnimationFrame frm = new MaterialAnimationFrame();

		frm.tx = getValueAt(mtx[coordinator], frame);
		frm.ty = getValueAt(mty[coordinator], frame);

		frm.r = getValueAt(mrot[coordinator], frame);

		frm.sx = getValueAt(msx[coordinator], frame);
		frm.sy = getValueAt(msy[coordinator], frame);

		AnimatedValue tex = getValueAt(textureIndices[coordinator], frame, true);

		if (tex.exists) {
			frm.textureName = textureNames.get((int) tex.value);
		}

		return frm;
	}

	public MaterialAnimationColorFrame getColorFrame(float frame) {
		MaterialAnimationColorFrame frm = new MaterialAnimationColorFrame();

		for (int i = 0; i < materialColors.length; i++) {
			RGBAKeyFrameGroup source = materialColors[i];
			AnimatedColor c = new AnimatedColor();
			c.r = getValueAt(source.r, frame);
			c.g = getValueAt(source.g, frame);
			c.b = getValueAt(source.b, frame);
			c.a = getValueAt(source.a, frame);
			frm.colors[i] = c;
			frm.constantIndexAbsolute[i] = source.isConstantIndexAbsolute;
		}

		return frm;
	}

	private void createArrayLists(KeyFrameList[] target) {
		for (int i = 0; i < target.length; i++) {
			target[i] = new KeyFrameList();
		}
	}

	@Override
	public List<KeyFrameList> getAllKfLists() {
		List<KeyFrameList> l = new ArrayList<>();

		for (int i = 0; i < 3; i++) {
			l.add(mtx[i]);
			l.add(mty[i]);
			l.add(msx[i]);
			l.add(msy[i]);
			l.add(mrot[i]);
			l.add(textureIndices[i]);
		}

		return l;
	}
}
