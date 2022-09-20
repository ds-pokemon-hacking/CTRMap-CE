package ctrmap.renderer.scene.texturing;

import xstandard.math.vec.RGBA;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TexEnvConfig implements Cloneable {

	public static final int STAGE_COUNT = 6;

	public TexEnvStage[] stages = new TexEnvStage[STAGE_COUNT];
	public RGBA inputBufferColor = new RGBA(0, 0, 0, 0);

	public float[] alphaScale = new float[STAGE_COUNT];
	public float[] colorScale = new float[STAGE_COUNT];
	public int[] writeAlphaBuffer = new int[STAGE_COUNT];
	public int[] writeColorBuffer = new int[STAGE_COUNT];
	public int[] alphaCombineOperator = new int[STAGE_COUNT];
	public int[] colorCombineOperator = new int[STAGE_COUNT];
	public int[] alphaSrc0 = new int[STAGE_COUNT];
	public int[] alphaSrc1 = new int[STAGE_COUNT];
	public int[] alphaSrc2 = new int[STAGE_COUNT];
	public int[] colorSrc0 = new int[STAGE_COUNT];
	public int[] colorSrc1 = new int[STAGE_COUNT];
	public int[] colorSrc2 = new int[STAGE_COUNT];
	public int[] alphaOp0 = new int[STAGE_COUNT];
	public int[] alphaOp1 = new int[STAGE_COUNT];
	public int[] alphaOp2 = new int[STAGE_COUNT];
	public int[] colorOp0 = new int[STAGE_COUNT];
	public int[] colorOp1 = new int[STAGE_COUNT];
	public int[] colorOp2 = new int[STAGE_COUNT];

	private boolean areUniformsGenerated = false;

	public void generateUniforms() {
		if (areUniformsGenerated) {
			return;
		}
		areUniformsGenerated = true;
		for (int i = 0; i < 6; i++) {
			TexEnvStage s = stages[i];
			alphaScale[i] = s.getAlphaScaleFloat();
			colorScale[i] = s.getRgbScaleFloat();
			writeAlphaBuffer[i] = s.writeAlphaBuffer ? 1 : 0;
			writeColorBuffer[i] = s.writeColorBuffer ? 1 : 0;
			alphaCombineOperator[i] = s.alphaCombineOperator.ordinal();
			colorCombineOperator[i] = s.rgbCombineOperator.ordinal();
			alphaSrc0[i] = s.alphaSource[0].ordinal();
			alphaSrc1[i] = s.alphaSource[1].ordinal();
			alphaSrc2[i] = s.alphaSource[2].ordinal();
			colorSrc0[i] = s.rgbSource[0].ordinal();
			colorSrc1[i] = s.rgbSource[1].ordinal();
			colorSrc2[i] = s.rgbSource[2].ordinal();
			alphaOp0[i] = s.alphaOperand[0].ordinal();
			alphaOp1[i] = s.alphaOperand[1].ordinal();
			alphaOp2[i] = s.alphaOperand[2].ordinal();
			colorOp0[i] = s.rgbOperand[0].ordinal();
			colorOp1[i] = s.rgbOperand[1].ordinal();
			colorOp2[i] = s.rgbOperand[2].ordinal();
		}
	}

	@Override
	public TexEnvConfig clone() {
		try {
			super.clone();

			TexEnvConfig c = new TexEnvConfig();

			c.inputBufferColor = new RGBA(inputBufferColor);

			for (int i = 0; i < STAGE_COUNT; i++) {
				TexEnvStage s = c.stages[i];
				TexEnvStage s2 = stages[i];
				s.constantColor = s2.constantColor;
				s.rgbCombineOperator = s2.rgbCombineOperator;
				s.alphaCombineOperator = s2.alphaCombineOperator;
				s.rgbScale = s2.rgbScale;
				s.alphaScale = s2.alphaScale;
				System.arraycopy(s2.rgbSource, 0, s.rgbSource, 0, 3);
				System.arraycopy(s2.alphaSource, 0, s.alphaSource, 0, 3);
				System.arraycopy(s2.rgbOperand, 0, s.rgbOperand, 0, 3);
				System.arraycopy(s2.alphaOperand, 0, s.alphaOperand, 0, 3);
				s.writeAlphaBuffer = s2.writeAlphaBuffer;
				s.writeColorBuffer = s2.writeColorBuffer;
			}
			return c;
		} catch (CloneNotSupportedException ex) {
			Logger.getLogger(TexEnvConfig.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}
	}

	public TexEnvConfig() {
		stages[0] = new TexEnvStage(TexEnvStage.TexEnvTemplate.VCOL);
		stages[1] = new TexEnvStage(TexEnvStage.TexEnvTemplate.PASSTHROUGH);
		stages[2] = new TexEnvStage(TexEnvStage.TexEnvTemplate.PASSTHROUGH);
		stages[3] = new TexEnvStage(TexEnvStage.TexEnvTemplate.PASSTHROUGH);
		stages[4] = new TexEnvStage(TexEnvStage.TexEnvTemplate.PASSTHROUGH);
		stages[5] = new TexEnvStage(TexEnvStage.TexEnvTemplate.PASSTHROUGH);
		for (int i = 0; i < stages.length; i++) {
			stages[i].constantColor = MaterialColorType.forCColIndex(i);
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof TexEnvConfig) {
			TexEnvConfig t = (TexEnvConfig) o;
			boolean is = t.inputBufferColor.equals(inputBufferColor);
			for (int i = 0; i < STAGE_COUNT; i++) {
				if (is == false) {
					break;
				}
				is = t.stages[i].equals(stages[i]);
			}
			return is;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 13 * hash + Arrays.deepHashCode(this.stages);
		hash = 13 * hash + Objects.hashCode(this.inputBufferColor);
		return hash;
	}

	public static boolean isTexSource(PICATextureCombinerSource src) {
		switch (src) {
			case TEX0:
			case TEX1:
			case TEX2:
			case TEX3:
				return true;
		}
		return false;
	}

	public int getActiveStageCount() {
		for (int i = stages.length - 1; i >= 0; i--) {
			if (!stages[i].isPassThrough()) {
				return i + 1;
			}
		}
		return 0;
	}

	public static int getCombinerModeArgumentCount(PICATextureCombinerMode mode) {
		switch (mode) {
			case REPLACE:
				return 1;
			case MODULATE:
			case ADD:
			case ADD_SIGNED:
			case SUBTRACT:
			case DOT3_RGB:
			case DOT3_RGBA:
				return 2;
			case ADD_MULT:
			case MULT_ADD:
			case INTERPOLATE:
				return 3;
		}
		return 1;
	}

	public enum Scale {
		ONE(1f),
		TWO(2f),
		FOUR(4f);

		public final float floatValue;

		private Scale(float fv) {
			this.floatValue = fv;
		}
		
		public static Scale forFloat(float f) {
			if (f == FOUR.floatValue) {
				return FOUR;
			}
			if (f == TWO.floatValue) {
				return TWO;
			}
			return ONE;
		}
	}

	public enum PICATextureCombinerMode {
		REPLACE,
		MODULATE,
		ADD,
		ADD_SIGNED,
		INTERPOLATE,
		SUBTRACT,
		DOT3_RGB,
		DOT3_RGBA,
		MULT_ADD,
		ADD_MULT
	}

	public enum PICATextureCombinerSource {
		PRIMARY_COLOR,
		FRAG_PRIMARY_COLOR,
		FRAG_SECONDARY_COLOR,
		TEX0,
		TEX1,
		TEX2,
		TEX3,
		DUMMY_7,
		DUMMY_8,
		DUMMY_9,
		DUMMY_10,
		DUMMY_11,
		DUMMY_12,
		PREVIOUS_BUFFER,
		CONSTANT,
		PREVIOUS_STAGE;

		private static final PICATextureCombinerSource[] vals = values();

		public static PICATextureCombinerSource getTexSource(int idx) {
			if (idx > 3) {
				return null;
			}
			return vals[TEX0.ordinal() + idx];
		}
	}

	public enum PICATextureCombinerColorOp {
		SRC_COLOR,
		ONE_MINUS_SRC_COLOR,
		SRC_ALPHA,
		ONE_MINUS_SRC_ALPHA,
		RED,
		ONE_MINUS_RED,
		DUMMY_6,
		DUMMY_7,
		GREEN,
		ONE_MINUS_GREEN,
		DUMMY_9,
		DUMMY_10,
		BLUE,
		ONE_MINUS_BLUE
	}

	public enum PICATextureCombinerAlphaOp {
		SRC_ALPHA,
		ONE_MINUS_SRC_ALPHA,
		RED,
		ONE_MINUS_RED,
		GREEN,
		ONE_MINUS_GREEN,
		BLUE,
		ONE_MINUS_BLUE
	}
}
