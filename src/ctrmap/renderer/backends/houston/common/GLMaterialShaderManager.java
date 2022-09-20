package ctrmap.renderer.backends.houston.common;

import ctrmap.renderer.backends.houston.common.shaderengine.GLTEVShaderGenerator;
import ctrmap.renderer.backends.base.shaderengine.MaterialShaderManager;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.scene.texturing.Material;
import java.util.HashMap;
import java.util.Map;

public class GLMaterialShaderManager extends MaterialShaderManager {

	//The sources have to be stored per backend class, because some backends have implementation specific definitions (see GL4 alpha test)
	private static final Map<Class<? extends ShaderProgramManager>, Map<Integer, String>> baseShaderSources = new HashMap<>();
	private static final Map<Class<? extends ShaderProgramManager>, Map<Integer, String>> extendedShaderSources = new HashMap<>();

	@Override
	public String getShaderSource(Material mat, ShaderProgramManager prgMng, String... extensions) {
		Class prgMngClass = prgMng.getClass();
		
		Map<Integer, String> bss = baseShaderSources.get(prgMngClass);
		if (bss == null){
			bss = new HashMap<>();
			baseShaderSources.put(prgMngClass, bss);
		}
		Map<Integer, String> ess = extendedShaderSources.get(prgMngClass);
		if (ess == null){
			ess = new HashMap<>();
			extendedShaderSources.put(prgMngClass, ess);
		}
		
		int shadingHash = mat == null ? 0 : mat.getFragmentShaderHash();

		String baseSource = bss.get(shadingHash);
		if (baseSource == null) {
			baseSource = GLTEVShaderGenerator.createShader(mat);
			bss.put(shadingHash, baseSource);
		}

		int extendedHash = prgMng.getCombHash(shadingHash, extensions);
		String extendedSource = ess.get(extendedHash);
		if (extendedSource == null) {
			extendedSource = prgMng.getExtendedShaderSource(baseSource, prgMng.getCombExtensions(extensions));
			ess.put(extendedHash, extendedSource);
		}

		return extendedSource;
	}
}
