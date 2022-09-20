package ctrmap.renderer.backends.houston.gl2.uber;

import ctrmap.renderer.backends.houston.common.GLMaterialShaderManager;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;
import ctrmap.renderer.backends.houston.HoustonResources;
import ctrmap.renderer.backends.houston.common.HoustonConstants;
import ctrmap.renderer.scene.texturing.Material;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UberFshManager extends GLMaterialShaderManager{

	private String uberFshSource;
	
	private Map<Integer, String> extendedSources = new HashMap<>();
	
	public UberFshManager(){
		uberFshSource = new String(HoustonResources.ACCESSOR.getByteArray(HoustonConstants.HOUSTON_ROOT + "/" + HoustonGL2Uber.HOUSTON_DEOXYS_FSH), StandardCharsets.UTF_8);
	}
	
	@Override
	public String getShaderSource(Material mat, ShaderProgramManager prgMng, String... extensions){
		int hash = prgMng.getCombHash(0, extensions);
		if (!extendedSources.containsKey(hash)){
			extendedSources.put(hash, prgMng.getExtendedShaderSource(uberFshSource, prgMng.getCombExtensions(extensions)));
		}
		return extendedSources.get(hash);
	}
}
