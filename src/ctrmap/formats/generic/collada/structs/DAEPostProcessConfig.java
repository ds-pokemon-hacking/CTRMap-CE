package ctrmap.formats.generic.collada.structs;

public class DAEPostProcessConfig {
	public boolean isConfigApplied = false;
	
	public boolean isBlenderAny = false;
	public boolean isShitBlender = false;
	
	public DAEUpAxis upAxis = DAEUpAxis.UNDEFINED;
	
	public static enum DAEUpAxis{
		Y_UP,
		Z_UP,
		UNDEFINED
	}
}
