package ctrmap.renderer.backends.houston.gl4;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.houston.common.HoustonShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.texturing.Material;

public interface GL4AlphaTestHoustonShaderAdapter extends HoustonShaderAdapter {

	public static final GL4AlphaTestHoustonShaderAdapter INSTANCE = new GL4AlphaTestHoustonShaderAdapter() {
	};
	
	@Override
	public default void setUpMaterialUniforms(Material mat, IRenderDriver driver, ShaderProgram program) {
		HoustonShaderAdapter.super.setUpMaterialUniforms(mat, driver, program);
		if (mat != null) {
			driver.uniform1i(program.getUniformLocation("alphaTestEnable", driver), mat.alphaTest.enabled ? 1 : 0);
			driver.uniform1i(program.getUniformLocation("alphaTestFunction", driver), mat.alphaTest.testFunction.ordinal());
			driver.uniform1f(program.getUniformLocation("alphaTestReference", driver), mat.alphaTest.reference / 255f);
		}
		else {
			driver.uniform1i(program.getUniformLocation("alphaTestEnable", driver), 0);
		}
	}
}
