package ctrmap.renderer.backends.base;

import ctrmap.renderer.scene.texturing.Material;

public class BackendMaterialOverride extends Material {

	private int flags = 0;

	public BackendMaterialOverride(Material src) {
		super(src);
	}

	public BackendMaterialOverride() {
		super();
	}

	public boolean enabled(){
		return (flags & 1) > 0; //faster method since it will be used a lot
	}
	
	public boolean hasOverrideCap(OverrideCapability flg) {
		return ((flags >> flg.ordinal()) & 1) > 0;
	}
	
	public void enableOverrideCap(OverrideCapability flg){
		setOverrideCap(flg, true);
	}
	
	public void disableOverrideCap(OverrideCapability flg){
		setOverrideCap(flg, false);
	}
	
	public void setOverrideCap(OverrideCapability flg, boolean enable){
		int bit = (1 << flg.ordinal());
		if (enable){
			flags |= bit;
		}
		else {
			flags &= ~bit;
		}
	}

	public enum OverrideCapability {
		ENABLED,
		DEPTH_TEST,
		ALPHA_TEST,
		ALPHA_BLEND,
		STENCIL_TEST,
		STENCIL_OPERATION,
		FACE_CULLING,
		LIGHTING_LAYER,
		LIGHT_SET_INDEX,
	}
}
