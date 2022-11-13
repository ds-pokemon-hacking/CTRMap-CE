
package ctrmap.renderer.scene.texturing;

import xstandard.math.vec.Vec2f;

public class TextureMapper {
	public String textureName;
	
	public MaterialParams.TextureMapMode mapMode = MaterialParams.TextureMapMode.UV_MAP;
	public int uvSetNo = 0;

	public MaterialParams.TextureWrap mapU = MaterialParams.TextureWrap.REPEAT;
	public MaterialParams.TextureWrap mapV = MaterialParams.TextureWrap.REPEAT;
	
	public MaterialParams.TextureMagFilter textureMagFilter = MaterialParams.TextureMagFilter.LINEAR;
	public MaterialParams.TextureMinFilter textureMinFilter = MaterialParams.TextureMinFilter.LINEAR;
	
	public Vec2f bindTranslation = new Vec2f();
	public float bindRotation = 0f;
	public Vec2f bindScale = new Vec2f(1f, 1f);
	
	public TextureMapper(){
		
	}
	
	public TextureMapper(TextureMapper src){
		mapU = src.mapU;
		mapV = src.mapV;
		textureMagFilter = src.textureMagFilter;
		textureMinFilter = src.textureMinFilter;
		bindRotation = src.bindRotation;
		bindScale = new Vec2f(src.bindScale);
		bindTranslation = new Vec2f(src.bindTranslation);
		textureName = src.textureName;
		uvSetNo = src.uvSetNo;
	}
	
	public TextureMapper(String textureName){
		this.textureName = textureName;
	}
	
	@Override
	public String toString(){
		if (textureName == null){
			return "(No texture)";
		}
		return textureName;
	}
}
