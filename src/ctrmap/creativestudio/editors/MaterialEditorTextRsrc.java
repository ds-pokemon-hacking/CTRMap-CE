
package ctrmap.creativestudio.editors;

import ctrmap.renderer.scene.texturing.TexEnvConfig;

public class MaterialEditorTextRsrc {
	public static final String[] texcoordGenModes = new String[]{"UV Map", "Cube Map", "Sphere map", "Projection map"};
	public static final String[] textureWrapModes = new String[]{"Clamp to Edge", "Clamp to Border", "Repeat", "Mirrored Repeat"};
	public static final String[] blendFunctions = new String[]{
		"Zero",
		"One",
		"Source color",
		"One minus Source Color",
		"Destination Color",
		"One minus Destination Color",
		"Source Alpha",
		"One minus Source Alpha",
		"Destination Alpha",
		"One minus Destination Alpha",
		"Constant Color",
		"One minus Constant Color",
		"Constant Alpha",
		"One minus Constant Alpha",
		"Source Alpha saturate"
	};
	public static final String[] blendEquations = new String[]{
		"Add",
		"Subtract",
		"Reverse Subtract",
		"Min",
		"Max"
	};
	public static final String[] combinerModes = new String[]{
		"Replace",
		"Modulate",
		"Add",
		"Signed Add",
		"Interpolate",
		"Subtract",
		"Dot product (RGB)",
		"Dot product (RGBA)",
		"Multiply + Add",
		"Add * Multiply"
	};
	public static final String[] combinerModeEquations = new String[]{
		"(A)",
		"(A * B)",
		"(A + B)",
		"(A + B) - 0.5",
		"(A * C + B * (1 - C))",
		"(A - B)",
		"(dot(A.rgb, B.rgb))",
		"(dot(A.rgba, B.rgba))",
		"(A * B + C)",
		"(clamp(A + B) * C)"
	};
	public static final String[] combinerSources = new String[]{
		"Vertex color",	//0
		"Fragment lighting primary color", //1
		"Fragment lighting secondary color",//2
		"Texture 0",//3
		"Texture 1",//4
		"Texture 2",//5
		"Texture 3",//6
		"Combiner buffer",//7
		"Constant color",//8
		"Previous stage"//9
	};
	public static final String[] testFunctions = new String[]{
		"Never",
		"Always",
		"Equal",
		"Not Equal",
		"Less",
		"Less Or Equal",
		"Greater",
		"Greater Or Equal"
	};
	public static final String[] stencilOps = new String[]{
		"Keep",
		"Zero",
		"Replace",
		"Increment",
		"Decrement",
		"Invert",
		"Increment (Wrap)",
		"Decrement (Wrap)"
	};
	
	public static int getSourceStrIndexForEnum(TexEnvConfig.PICATextureCombinerSource src){
		switch (src){
			case CONSTANT:
				return 8;
			case FRAG_PRIMARY_COLOR:
				return 1;
			case FRAG_SECONDARY_COLOR:
				return 2;
			case PREVIOUS_BUFFER:
				return 7;
			case PREVIOUS_STAGE:
				return 9;
			case PRIMARY_COLOR:
				return 0;
			case TEX0:
				return 3;
			case TEX1:
				return 4;
			case TEX2:
				return 5;
			case TEX3:
				return 6;
		}
		return 0;
	}
	
	public static TexEnvConfig.PICATextureCombinerSource getSourceEnumForStrIndex(int idx){
		switch (idx){
			case 0:
				return TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
			case 1:
				return TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR;
			case 2:
				return TexEnvConfig.PICATextureCombinerSource.FRAG_SECONDARY_COLOR;
			case 3:
				return TexEnvConfig.PICATextureCombinerSource.TEX0;
			case 4:
				return TexEnvConfig.PICATextureCombinerSource.TEX1;
			case 5:
				return TexEnvConfig.PICATextureCombinerSource.TEX2;
			case 6:
				return TexEnvConfig.PICATextureCombinerSource.TEX3;
			case 7:
				return TexEnvConfig.PICATextureCombinerSource.PREVIOUS_BUFFER;
			case 8:
				return TexEnvConfig.PICATextureCombinerSource.CONSTANT;
			case 9:
				return TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
		}
		return TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
	}
	
	public static final String[] combinerColorOps = new String[]{
		"Color",
		"1 - Color",
		"Alpha",
		"1 - Alpha",
		"Red channel",
		"1 - Red channel",
		"Green channel",
		"1 - Green channel",
		"Blue channel",
		"1 - Blue channel"
	};
	
	public static int getColorOpStrIndexForEnum(TexEnvConfig.PICATextureCombinerColorOp op){
		switch (op){
			case SRC_COLOR:
				return 0;
			case ONE_MINUS_SRC_COLOR:
				return 1;
			case SRC_ALPHA:
				return 2;
			case ONE_MINUS_SRC_ALPHA:
				return 3;
			case RED:
				return 4;
			case ONE_MINUS_RED:
				return 5;
			case GREEN:
				return 6;
			case ONE_MINUS_GREEN:
				return 7;
			case BLUE:
				return 8;
			case ONE_MINUS_BLUE:
				return 9;
		}
		return 0;
	}
	
	public static TexEnvConfig.PICATextureCombinerColorOp getColorOpEnumForStrIndex(int idx){
		switch (idx){
			case 0:
				return TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR;
			case 1:
				return TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_SRC_COLOR;
			case 2:
				return TexEnvConfig.PICATextureCombinerColorOp.SRC_ALPHA;
			case 3:
				return TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_SRC_ALPHA;
			case 4:
				return TexEnvConfig.PICATextureCombinerColorOp.RED;
			case 5:
				return TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_RED;
			case 6:
				return TexEnvConfig.PICATextureCombinerColorOp.GREEN;
			case 7:
				return TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_GREEN;
			case 8:
				return TexEnvConfig.PICATextureCombinerColorOp.BLUE;
			case 9:
				return TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_BLUE;
		}
		return TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR;
	}
	
	public static final String[] combinerAlphaOps = new String[]{
		"Alpha",
		"1 - Alpha",
		"Red channel",
		"1 - Red channel",
		"Green channel",
		"1 - Green channel",
		"Blue channel",
		"1 - Blue channel"
	};
}
