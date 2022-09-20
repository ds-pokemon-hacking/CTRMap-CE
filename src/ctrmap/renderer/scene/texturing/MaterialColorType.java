
package ctrmap.renderer.scene.texturing;

public enum MaterialColorType {
	CONSTANT0,
	CONSTANT1,
	CONSTANT2,
	CONSTANT3,
	CONSTANT4,
	CONSTANT5,
	EMISSION,
	AMBIENT,
	DIFFUSE,
	SPECULAR0,
	SPECULAR1;
	
	private static final MaterialColorType[] VALUES = values();
	
	public static MaterialColorType forCColIndex(int index){
		return VALUES[index];
	}
}
