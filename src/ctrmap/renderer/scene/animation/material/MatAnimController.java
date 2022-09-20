package ctrmap.renderer.scene.animation.material;

import xstandard.math.vec.Vec2f;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.scene.animation.AbstractAnimationController;
import java.util.HashMap;
import java.util.Map;

public class MatAnimController extends AbstractAnimationController {

	public Map<String, Float>[] translationX = new HashMap[3];
	public Map<String, Float>[] translationY = new HashMap[3];
	public Map<String, Float>[] rotation = new HashMap[3];
	public Map<String, Float>[] scaleX = new HashMap[3];
	public Map<String, Float>[] scaleY = new HashMap[3];
	
	public Map<String, String>[] textureName = new HashMap[3];
	
	public Map<String, MaterialAnimationColorFrame> colors = new HashMap<>();

	public MatAnimController(MaterialAnimation anm) {
		super(anm);
		createHashMaps(translationX);
		createHashMaps(translationY);
		createHashMaps(rotation);
		createHashMaps(scaleX);
		createHashMaps(scaleY);
		createHashMaps(textureName);
	}

	@Override
	public void advanceFrame(float globalStep, RenderSettings settings) {
		super.advanceFrame(globalStep, settings);
		
		float frameFinal = frame;
		if (settings.ANIMATION_USE_30FPS_MAT) {
			frameFinal = (float) Math.floor(frame);
		}
		
		makeAnimationVectors(frameFinal);
	}

	private void createHashMaps(Map[] target) {
		for (int i = 0; i < target.length; i++) {
			target[i] = new HashMap();
		}
	}

	private void clearHashMaps(Map[] target) {
		for (int i = 0; i < target.length; i++) {
			target[i].clear();
		}
	}

	public void makeAnimationVectors(float frame) {
		for (MatAnimBoneTransform bt : ((MaterialAnimation) anim).bones) {
			for (int i = 0; i < 3; i++) {
				MaterialAnimationFrame frm = bt.getFrame(frame, i);

				if (frm.tx.exists) translationX[i].put(bt.name, -frm.tx.value);
				if (frm.ty.exists) translationY[i].put(bt.name, -frm.ty.value);
				if (frm.sx.exists) scaleX[i].put(bt.name, frm.sx.value);
				if (frm.sy.exists) scaleY[i].put(bt.name, frm.sy.value);
				if (frm.r.exists) rotation[i].put(bt.name, frm.r.value);
				textureName[i].put(bt.name, frm.textureName);
			}
			colors.put(bt.name, bt.getColorFrame(frame));
		}
	}
}
