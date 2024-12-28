package ctrmap.formats.ntr.common.gfx;

import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.util.MaterialProcessor;

public class Nitroshader {

	public static final MaterialColorType NSH_RESERVE_MATERIAL_ALPHA_CCOL = MaterialColorType.CONSTANT5;
	public static final int POLYGON_ID_MASK = 0x1F;

	public static boolean isNshMaterialAlphaUsed(Material mat) {
		for (TexEnvStage stage : mat.tevStages.stages) {
			int constAlphaIdx = MaterialProcessor.getTEVSourceAlphaIdx(stage, TexEnvConfig.PICATextureCombinerSource.CONSTANT);
			if (constAlphaIdx != -1 && stage.constantColor == NSH_RESERVE_MATERIAL_ALPHA_CCOL) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean isNshStencilSchemeUsed(Material mat) {
		return mat.stencilOperation.fail == MaterialParams.StencilOp.KEEP && mat.stencilOperation.zFail == MaterialParams.StencilOp.KEEP
			&& mat.stencilOperation.zPass == MaterialParams.StencilOp.REPLACE && mat.stencilTest.enabled && mat.stencilTest.testFunction == MaterialParams.TestFunction.ALWAYS
			&& mat.stencilTest.bufferMask == POLYGON_ID_MASK && mat.stencilTest.funcMask == POLYGON_ID_MASK;
	}

	public static void ensureNsh(Material mat) {
		if (!isNshMaterialAlphaUsed(mat)) {
			setMaterialAlphaToNshCCol(mat);
		}
		if (!isNshStencilSchemeUsed(mat)) {
			setNshStencilScheme(mat, 0);
		}
	}

	public static void setNshAlphaValue(Material mat, int value255) {
		ensureNsh(mat);
		mat.getMaterialColor(NSH_RESERVE_MATERIAL_ALPHA_CCOL).a = (short) value255;
	}

	public static int getNshAlphaValue255(Material mat) {
		if (isNshMaterialAlphaUsed(mat)) {
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
	
	public static void setNshStencilScheme(Material mat, int polygonID) {
		//same as in 3DS pokemon games
		mat.stencilOperation.fail = MaterialParams.StencilOp.KEEP;
		mat.stencilOperation.zFail = MaterialParams.StencilOp.KEEP;
		mat.stencilOperation.zPass = MaterialParams.StencilOp.REPLACE;

		mat.stencilTest.enabled = true;
		mat.stencilTest.testFunction = MaterialParams.TestFunction.ALWAYS;
		mat.stencilTest.funcMask = POLYGON_ID_MASK;
		mat.stencilTest.bufferMask = POLYGON_ID_MASK;
		mat.stencilTest.reference = polygonID;
	}
}
