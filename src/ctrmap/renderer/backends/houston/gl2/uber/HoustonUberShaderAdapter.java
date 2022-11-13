package ctrmap.renderer.backends.houston.gl2.uber;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.houston.common.HoustonShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.texturing.LUT;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;

/**
 *
 */
public interface HoustonUberShaderAdapter extends HoustonShaderAdapter {

	public static final HoustonUberShaderAdapter INSTANCE = new HoustonUberShaderAdapter() {
	};
	
	@Override
	public default void setUpMaterialUniforms(Material mat, IRenderDriver driver, ShaderProgram program) {
		HoustonShaderAdapter.super.setUpMaterialUniforms(mat, driver, program);
		if (mat != null) {
			passTEVUniforms(mat.tevStages, program, driver);
			passLUTUniforms(mat, program, driver);
		}
	}

	public static void passTEVUniforms(TexEnvConfig tev, ShaderProgram program, IRenderDriver gl) {
		tev.generateUniforms();

		gl.uniform4fv(program.getUniformLocation(UberUniforms.TEV_IN_BUF_COLOR, gl), tev.inputBufferColor);
		//gl.glUniform4fv(TEV_CONSTANT_COLOR_LOC, tev.constantColor); //not needed, is shared with the procedural shader
		gl.uniform1fv(program.getUniformLocation(UberUniforms.TEV_SCALE_COLOR, gl), tev.colorScale);
		gl.uniform1fv(program.getUniformLocation(UberUniforms.TEV_SCALE_ALPHA, gl), tev.alphaScale);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_BUF_WRITE_ALPHA, gl), tev.writeAlphaBuffer);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_BUF_WRITE_COLOR, gl), tev.writeColorBuffer);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBMODE_ALPHA, gl), tev.alphaCombineOperator);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBMODE_COLOR, gl), tev.colorCombineOperator);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBSRC_ALPHA_0, gl), tev.alphaSrc0);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBSRC_ALPHA_1, gl), tev.alphaSrc1);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBSRC_ALPHA_2, gl), tev.alphaSrc2);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBSRC_COLOR_0, gl), tev.colorSrc0);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBSRC_COLOR_1, gl), tev.colorSrc1);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBSRC_COLOR_2, gl), tev.colorSrc2);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBOPERAND_ALPHA_0, gl), tev.alphaOp0);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBOPERAND_ALPHA_1, gl), tev.alphaOp1);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBOPERAND_ALPHA_2, gl), tev.alphaOp2);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBOPERAND_COLOR_0, gl), tev.colorOp0);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBOPERAND_COLOR_1, gl), tev.colorOp1);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEV_CMBOPERAND_COLOR_2, gl), tev.colorOp2);
	}

	public static void passLUTUniforms(Material mat, ShaderProgram program, IRenderDriver gl) {
		int cnt = MaterialParams.LUTTarget.values().length;
		int[] LUTTargetsEnabled = new int[cnt];
		int[] LUTSources = new int[cnt];
		for (LUT lut : mat.LUTs) {
			LUTTargetsEnabled[lut.target.ordinal()] = 1;
			LUTSources[lut.target.ordinal()] = lut.source.ordinal();
		}
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEX_SAMPLER_LUT_ENABLED, gl), LUTTargetsEnabled);
		gl.uniform1iv(program.getUniformLocation(UberUniforms.TEX_SAMPLER_LUT_INPUTS, gl), LUTSources);
	}
}
