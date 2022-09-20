package ctrmap.renderer.backends.houston.gl2.shaderengine;

import ctrmap.renderer.backends.houston.common.shaderengine.GLProgramManager;

public class GL2ProgramManager extends GLProgramManager {

	private static final int HOUSTON_GLSL_VERSION = 110;
	private static final String HOUSTON_GL2SL_DEFINE = "GL2";

	public GL2ProgramManager() {

	}
	
	@Override
	protected int getGLSLVersion(){
		return HOUSTON_GLSL_VERSION;
	}
	
	@Override
	protected String getGLSLDefinition(){
		return HOUSTON_GL2SL_DEFINE;
	}
}
