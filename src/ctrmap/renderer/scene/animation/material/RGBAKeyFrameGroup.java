
package ctrmap.renderer.scene.animation.material;

import ctrmap.renderer.scene.animation.KeyFrame;
import ctrmap.renderer.scene.animation.KeyFrameList;
import xstandard.math.vec.RGBA;

public class RGBAKeyFrameGroup {
	public boolean isConstantIndexAbsolute = false;
	
	public KeyFrameList r = new KeyFrameList();
	public KeyFrameList g = new KeyFrameList();
	public KeyFrameList b = new KeyFrameList();
	public KeyFrameList a = new KeyFrameList();
		
	public void addColor(float frame, RGBA color) {
		r.add(new KeyFrame(frame, color.getR()));
		g.add(new KeyFrame(frame, color.getG()));
		b.add(new KeyFrame(frame, color.getB()));
		a.add(new KeyFrame(frame, color.getA()));
	}
}
