
package ctrmap.renderer.scene.animation.material;

import ctrmap.renderer.backends.RenderAllocator;
import ctrmap.renderer.scene.animation.AnimatedValue;
import static ctrmap.renderer.backends.RenderAllocator.allocAnimatedValue;

public class MaterialAnimationFrame {
	public AnimatedValue tx;
	public AnimatedValue ty;
	public AnimatedValue r;
	public AnimatedValue sx;
	public AnimatedValue sy;
		
	public String textureName;
	
	private boolean wasManualAlloc = false;
	
	public MaterialAnimationFrame(boolean manualAlloc) {
		wasManualAlloc = manualAlloc;
		if (manualAlloc) {
			tx = allocAnimatedValue();
			ty = allocAnimatedValue();
			r = allocAnimatedValue();
			sx = allocAnimatedValue();
			sy = allocAnimatedValue();
		}
		else {
			tx = new AnimatedValue();
			ty = new AnimatedValue();
			r = new AnimatedValue();
			sx = new AnimatedValue();
			sy = new AnimatedValue();
		}
	}
	
	public void free() {
		if (wasManualAlloc) {
			RenderAllocator.freeAnimatedValues(tx, ty, r, sx, sy);
		}
	}
}
