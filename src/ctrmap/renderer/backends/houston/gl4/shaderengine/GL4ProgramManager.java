package ctrmap.renderer.backends.houston.gl4.shaderengine;

import ctrmap.renderer.backends.houston.common.shaderengine.GLProgramManager;

public class GL4ProgramManager extends GLProgramManager {

	private static final int HOUSTON_GLSL_VERSION = 400;
	private static final String HOUSTON_GL4SL_DEFINE = "GL4";

	public GL4ProgramManager() {

	}

	@Override
	protected int getGLSLVersion() {
		return HOUSTON_GLSL_VERSION;
	}

	@Override
	protected String getGLSLDefinition() {
		return HOUSTON_GL4SL_DEFINE;
	}
}
