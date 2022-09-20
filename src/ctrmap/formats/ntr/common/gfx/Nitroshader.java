package ctrmap.formats.ntr.common.gfx;

import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.util.MaterialProcessor;

public class Nitroshader {

	public static final MaterialColorType NSH_RESERVE_MATERIAL_ALPHA_CCOL = MaterialColorType.CONSTANT5;

	public static boolean isNshReady(Material mat) {
		for (TexEnvStage stage : mat.tevStages.stages) {
			int constAlphaIdx = MaterialProcessor.getTEVSourceAlphaIdx(stage, TexEnvConfig.PICATextureCombinerSource.CONSTANT);
			if (constAlphaIdx != -1 && stage.constantColor == NSH_RESERVE_MATERIAL_ALPHA_CCOL) {
				return true;
			}
		}
		return false;
	}

	public static void ensureNsh(Material mat) {
		if (!isNshReady(mat)) {
			setMaterialAlphaToNshCCol(mat);
		}
	}

	public static void setNshAlphaValue(Material mat, int value255) {
		ensureNsh(mat);
		mat.getMaterialColor(NSH_RESERVE_MATERIAL_ALPHA_CCOL).a = (short) value255;
	}

	public static int getNshAlphaValue255(Material mat) {
		if (isNshReady(mat)) {
			return mat.getMaterialColor(NSH_RESERVE_MATERIAL_ALPHA_CCOL).a;
		}
		return 255;
	}

	public static void setMaterialAlphaToNshCCol(Material mat) {
		boolean isSet = false;
		int stageIdx = 0;
		for (TexEnvStage stage : mat.tevStages.stages) {
			int vtxAlphaIdx = MaterialProcessor.getTEVSourceAlphaIdx(stage, TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR);
			if (vtxAlphaIdx != -1) {
				if (!MaterialProcessor.hasTEVSource(stage, TexEnvConfig.PICATextureCombinerSource.CONSTANT)) {
					stage.alphaSource[vtxAlphaIdx] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
					stage.constantColor = NSH_RESERVE_MATERIAL_ALPHA_CCOL;
					isSet = true;
				}
				else {
					stage.alphaSource[vtxAlphaIdx] = TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
				}
			}
			if (!isSet && stage.isPassThroughAlpha()) {
				stage.alphaOperand[0] = TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA;
				stage.alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
				if (stageIdx == 0) {
					stage.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
				} else {
					stage.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
					stage.alphaOperand[1] = TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA;
					stage.alphaSource[1] = TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
				}

				stage.constantColor = NSH_RESERVE_MATERIAL_ALPHA_CCOL;
				isSet = true;
			}
			stageIdx++;
		}
	}
}
