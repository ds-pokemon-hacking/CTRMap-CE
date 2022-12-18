package ctrmap.renderer.backends.houston.gl2.uber;

import ctrmap.renderer.backends.base.RenderCapabilities;
import ctrmap.renderer.backends.base.RenderSettings;
import ctrmap.renderer.backends.houston.gl2.HoustonGL2;
import ctrmap.renderer.backends.houston.common.HoustonShaderAdapter;
import ctrmap.renderer.backends.base.flow.IShaderAdapter;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;

public class HoustonGL2Uber extends HoustonGL2 implements HoustonShaderAdapter {

	public static final String HOUSTON_DEOXYS_FSH = "Deoxys_UBER.fsh";

	public HoustonGL2Uber(){
		super();
	}
	
	public HoustonGL2Uber(RenderSettings settings, RenderCapabilities caps) {
		super(settings, caps);
	}

	@Override
	public IShaderAdapter getShaderHandler() {
		return HoustonUberShaderAdapter.INSTANCE;
	}
	
	@Override
	protected ShaderProgramManager createProgramManager() {
		return initProgramManager(new UberProgramManager());
	}
}
