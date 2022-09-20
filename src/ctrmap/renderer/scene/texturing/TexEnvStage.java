package ctrmap.renderer.scene.texturing;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 */
public class TexEnvStage {

	public TexEnvConfig.PICATextureCombinerMode rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
	public TexEnvConfig.PICATextureCombinerMode alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
	public TexEnvConfig.PICATextureCombinerSource[] rgbSource = new TexEnvConfig.PICATextureCombinerSource[3];
	public TexEnvConfig.PICATextureCombinerColorOp[] rgbOperand = new TexEnvConfig.PICATextureCombinerColorOp[3];
	public TexEnvConfig.PICATextureCombinerSource[] alphaSource = new TexEnvConfig.PICATextureCombinerSource[3];
	public TexEnvConfig.PICATextureCombinerAlphaOp[] alphaOperand = new TexEnvConfig.PICATextureCombinerAlphaOp[3];

	public TexEnvConfig.Scale rgbScale = TexEnvConfig.Scale.ONE;
	public TexEnvConfig.Scale alphaScale = TexEnvConfig.Scale.ONE;

	public MaterialColorType constantColor = MaterialColorType.CONSTANT0;

	public boolean writeColorBuffer = false;
	public boolean writeAlphaBuffer = false;

	public boolean isPassThrough() {
		return isPassThroughAlpha() && isPassThroughRGB();
	}

	public boolean isPassThroughAlpha() {
		return alphaCombineOperator == TexEnvConfig.PICATextureCombinerMode.REPLACE 
			&& alphaSource[0] == TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE 
			&& alphaOperand[0] == TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA 
			&& alphaScale == TexEnvConfig.Scale.ONE && !writeAlphaBuffer;
	}

	public boolean isPassThroughRGB() {
		return rgbCombineOperator == TexEnvConfig.PICATextureCombinerMode.REPLACE
			&& rgbSource[0] == TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE
			&& rgbOperand[0] == TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR
			&& rgbScale == TexEnvConfig.Scale.ONE
			&& !writeColorBuffer;
	}

	private static float getScaleFloat(TexEnvConfig.Scale s) {
		switch (s) {
			case ONE:
				return 1.0f;
			case TWO:
				return 2.0f;
			case FOUR:
				return 4.0f;
		}
		return 1.0f;
	}

	public float getRgbScaleFloat() {
		return getScaleFloat(rgbScale);
	}

	public float getAlphaScaleFloat() {
		return getScaleFloat(alphaScale);
	}

	public TexEnvStage() {
		clear();
	}

	public TexEnvStage(TexEnvTemplate template) {
		this();
		setTemplate(template);
	}

	public final void clear() {
		clearRGB();
		clearAlpha();
	}

	public final void clearRGB() {
		rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
		for (int i = 0; i < 3; i++) {
			rgbSource[i] = TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
			rgbOperand[i] = TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR;
		}
		writeAlphaBuffer = false;
		alphaScale = TexEnvConfig.Scale.ONE;
	}

	public final void clearAlpha() {
		alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
		for (int i = 0; i < 3; i++) {
			alphaSource[i] = TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
			alphaOperand[i] = TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA;
		}
		writeColorBuffer = false;
		rgbScale = TexEnvConfig.Scale.ONE;
	}

	public final void setTemplate(TexEnvTemplate template) {
		clear();
		switch (template) {
			case TEX0:
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				break;
			case TEX0_VCOL:
				rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
				alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
				rgbSource[1] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				alphaSource[1] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				break;
			case TEX0_CCOL:
				rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
				alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				rgbSource[1] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				alphaSource[1] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				break;
			case TEX0_CCOL_ADD:
				rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.ADD;
				alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.ADD;
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				rgbSource[1] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				alphaSource[1] = TexEnvConfig.PICATextureCombinerSource.TEX0;
				break;
			case PASSTHROUGH:
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
				break;
			case VCOL:
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
				break;
			case CCOL:
				rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				break;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof TexEnvStage) {
			TexEnvStage s = (TexEnvStage) o;
			return s.writeAlphaBuffer == writeAlphaBuffer && s.writeColorBuffer == writeColorBuffer && s.alphaCombineOperator == alphaCombineOperator && s.rgbCombineOperator == rgbCombineOperator && s.alphaScale == alphaScale && s.rgbScale == rgbScale && s.constantColor.equals(constantColor) && Arrays.equals(s.alphaOperand, alphaOperand) && Arrays.equals(s.rgbOperand, rgbOperand) && Arrays.equals(s.alphaSource, alphaSource) && Arrays.equals(s.rgbSource, rgbSource);
		}
		return false;
	}
	int lasthash = 0;

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 97 * hash + Objects.hashCode(this.rgbCombineOperator);
		hash = 97 * hash + Objects.hashCode(this.alphaCombineOperator);
		hash = 97 * hash + Arrays.deepHashCode(this.rgbSource);
		hash = 97 * hash + Arrays.deepHashCode(this.rgbOperand);
		hash = 97 * hash + Arrays.deepHashCode(this.alphaSource);
		hash = 97 * hash + Arrays.deepHashCode(this.alphaOperand);
		hash = 97 * hash + Objects.hashCode(this.rgbScale);
		hash = 97 * hash + Objects.hashCode(this.alphaScale);
		//hash = 97 * hash + Objects.hashCode(this.constantColor);
		hash = 97 * hash + (this.writeColorBuffer ? 1 : 0);
		hash = 97 * hash + (this.writeAlphaBuffer ? 1 : 0);
		return hash;
	}

	public static enum TexEnvTemplate {
		TEX0,
		VCOL,
		CCOL,
		TEX0_VCOL,
		TEX0_CCOL,
		TEX0_CCOL_ADD,
		PASSTHROUGH
	}
}
