
package ctrmap.renderer.backends.base.shaderengine;

import ctrmap.renderer.scene.texturing.Material;

public abstract class MaterialShaderManager {
	public abstract String getShaderSource(Material mat, ShaderProgramManager prgMng, String... extensions);
}
