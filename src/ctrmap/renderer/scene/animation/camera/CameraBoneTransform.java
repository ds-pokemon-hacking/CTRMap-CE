
package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.scene.animation.AbstractBoneTransform;
import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.math.MathEx;
import java.util.List;

/**
 *
 */
public abstract class CameraBoneTransform extends AbstractBoneTransform {
	
	public boolean isRadians;

	public KeyFrameList fov = new KeyFrameList();
	public KeyFrameList zNear = new KeyFrameList();
	public KeyFrameList zFar = new KeyFrameList();
	
	public KeyFrameList tx = new KeyFrameList();
	public KeyFrameList ty = new KeyFrameList();
	public KeyFrameList tz = new KeyFrameList();
	
	@Override
	public abstract List<KeyFrameList> getAllKfLists();

	public abstract <T extends CameraAnimationFrame> T getFrame(float frame, boolean doNotInterpolate);
	
	public boolean hasTranslation() {
		return KeyFrameList.existAny(tx, ty, tz);
	}
	
	public <T extends CameraAnimationFrame> T getFrame(float frame){
		return getFrame(frame, false);
	}
	
	protected void setCommonFrame(CameraAnimationFrame frm, float frame, boolean doNotInterpolate){
		frm.fov = getValueAt(fov, frame, doNotInterpolate);
		if (isRadians && frm.fov.exists) {
			frm.fov.value *= MathEx.RADIANS_TO_DEGREES;
		}
		frm.zNear = getValueAt(zNear, frame, doNotInterpolate);
		frm.zFar = getValueAt(zFar, frame, doNotInterpolate);
		frm.tx = getValueAt(tx, frame, doNotInterpolate);
		frm.ty = getValueAt(ty, frame, doNotInterpolate);
		frm.tz = getValueAt(tz, frame, doNotInterpolate);
	}
	
	public boolean exists() {
		return KeyFrameList.existAny(fov, zNear, zFar, tx, ty, tz);
	}
}
