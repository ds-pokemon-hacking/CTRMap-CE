
package ctrmap.renderer.scene.animation.camera;

import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.util.ArraysEx;
import java.util.List;

public class CameraLookAtBoneTransform extends CameraBoneTransform {
	
	public KeyFrameList targetTX = new KeyFrameList();
	public KeyFrameList targetTY = new KeyFrameList();
	public KeyFrameList targetTZ = new KeyFrameList();
	
	public KeyFrameList upX = new KeyFrameList();
	public KeyFrameList upY = new KeyFrameList();
	public KeyFrameList upZ = new KeyFrameList();
	
	@Override
	public List<KeyFrameList> getAllKfLists(){
		return ArraysEx.asList(fov, tx, ty, tz, targetTX, targetTY, targetTZ, upX, upY, upZ);
	}

	@Override
	public CameraLookAtFrame getFrame(float frame, boolean doNotInterpolate) {
		CameraLookAtFrame frm = new CameraLookAtFrame();
		frm.targetX = getValueAt(targetTX, frame, doNotInterpolate);
		frm.targetY = getValueAt(targetTY, frame, doNotInterpolate);
		frm.targetZ = getValueAt(targetTZ, frame, doNotInterpolate);
		frm.upX = getValueAt(upX, frame, doNotInterpolate);
		frm.upY = getValueAt(upY, frame, doNotInterpolate);
		frm.upZ = getValueAt(upZ, frame, doNotInterpolate);
		setCommonFrame(frm, frame, doNotInterpolate);
		return frm;
	}
}
