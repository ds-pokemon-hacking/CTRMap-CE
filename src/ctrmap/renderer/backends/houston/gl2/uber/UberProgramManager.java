package ctrmap.renderer.backends.houston.gl2.uber;

import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.backends.houston.common.GLMaterialShaderManager;
import ctrmap.renderer.backends.houston.gl2.shaderengine.GL2ProgramManager;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UberProgramManager extends GL2ProgramManager {

	public static final String HOUSTON_GL3SL_DEFINE = "GL3";
	
	private Map<Integer, ShaderProgram> uberShaderPrograms = new HashMap<>();
	
	@Override
	protected String getGLSLDefinition(){
		return HOUSTON_GL3SL_DEFINE;
	}

	private static int getMatUshHash(Material mat) {
		int hash = 0;
		if (mat != null) {
			hash = Objects.hashCode(mat.vertexShaderName);
			hash = 37 * hash + Arrays.deepHashCode(mat.getShaderExtensions());
		}
		return hash;
	}

	private int compileUberShader(IRenderDriver gl, Material mat) {
		int vsh = getVertexShader(gl, mat == null ? getUserShManager().getDefaultShaderName() : mat.vertexShaderName, mat == null ? new String[0] : mat.getShaderExtensions());
		int fsh = getFragmentShader(gl, null, mat == null ? new String[0] : mat.getShaderExtensions());

		int uber = compileShaderProgramSafe(gl, vsh, fsh, "Uber shader");
		if (uber == -1) {
			System.err.println("NULL UBERSHADER !!");
			return uber;
		}

		return uber;
	}
	
	private ShaderProgram last = null;

	@Override
	public ShaderProgram getShaderProgram(IRenderDriver gl, Material mat) {
		if (mat == null || mat.fshType == MaterialParams.FragmentShaderType.CTR_COMBINER) {
			int key = getCombHash(getMatUshHash(mat));
			ShaderProgram o = uberShaderPrograms.get(key);
			if (o == null) {
				o = new ShaderProgram();
				uberShaderPrograms.put(key, o);
			}
			
			int handle = o.handle.get(gl);
			if (handle == -1) {
				o.handle.set(gl, compileUberShader(gl, mat));
			}
			
			if (o != last) {
				last = o;
				//System.out.println("change shprg " + mat.name + " vsh " + mat.vertexShaderName + " prg " + o);
			}
			return o;
		}
		else {
			return super.getShaderProgram(gl, mat);
		}
	}

	@Override
	public GLMaterialShaderManager createFshManager() {
		return new UberFshManager();
	}

	@Override
	public int getGLSLVersion() {
		return 130;
	}
}
