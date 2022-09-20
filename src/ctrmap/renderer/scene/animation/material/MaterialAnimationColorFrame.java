
package ctrmap.renderer.scene.animation.material;

import ctrmap.renderer.scene.texturing.MaterialColorType;

public class MaterialAnimationColorFrame {
	public AnimatedColor[] colors = new AnimatedColor[MaterialColorType.values().length];
	public boolean[] constantIndexAbsolute = new boolean[colors.length];
	
	public AnimatedColor getColorForType(MaterialColorType t){
		int ord = t.ordinal();
		return constantIndexAbsolute[ord] ? null : colors[ord];
	}
	
	public AnimatedColor getColorForTypeAbsolute(MaterialColorType t){
		int ord = t.ordinal();
		return constantIndexAbsolute[ord] ? colors[ord] : null;
	}
}
