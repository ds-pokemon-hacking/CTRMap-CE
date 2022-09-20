
package ctrmap.renderer.scene.texturing;

import java.util.Objects;

public class LUT extends TextureMapper {

	public MaterialParams.LUTTarget target;
	public MaterialParams.LUTSource source;
	
	public LUT(){
		mapU = MaterialParams.TextureWrap.REPEAT;
		mapV = MaterialParams.TextureWrap.CLAMP_TO_EDGE;
	}
	
	public LUT(LUT lut) {
		super(lut);
		this.source = lut.source;
		this.target = lut.target;
	}
	
	public LUT(String texName, MaterialParams.LUTSource src, MaterialParams.LUTTarget tgt){
		this();
		textureName = texName;
		this.source = src;
		this.target = tgt;
	}

	@Override
	public int hashCode() {
		int hash = 3;
		hash = 47 * hash + Objects.hashCode(this.target);
		hash = 47 * hash + Objects.hashCode(this.source);
		return hash;
	}

}
