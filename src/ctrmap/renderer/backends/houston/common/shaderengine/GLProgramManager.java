package ctrmap.renderer.backends.houston.common.shaderengine;

import ctrmap.renderer.backends.houston.common.GLMaterialShaderManager;
import ctrmap.renderer.backends.base.shaderengine.ShaderDefinition;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgramManager;

public abstract class GLProgramManager extends ShaderProgramManager {

	public GLProgramManager() {

	}
	
	protected abstract int getGLSLVersion();
	
	protected abstract String getGLSLDefinition();

	@Override
	protected String createExtendedShaderSource(String shaderSource) {
		StringBuilder sb = new StringBuilder();

		sb.append("#version ");
		sb.append(getGLSLVersion());
		sb.append("\n");
		sb.append("#define ");
		sb.append(getGLSLDefinition());
		sb.append("\n");

		for (ShaderDefinition def : definitions) {
			sb.append("#define ");
			sb.append(def.key);
			sb.append(" ");
			sb.append(def.value);
			sb.append("\n");
		}

		StringBuilder lb = new StringBuilder();
		int maxChar = shaderSource.length() - 1;

		for (int i = 0; i <= maxChar; i++) {
			char c = shaderSource.charAt(i);

			lb.append(c);

			if (c == '\n' || i == maxChar) {
				String line = lb.toString();

				if (line.startsWith("#include ")) {
					String includePath = line.substring("#include ".length()).trim();

					String includedCode = ushManager.getShaderSource(includePath, this, false);

					if (includedCode != null) {
						sb.append(includedCode);

						sb.append("\n");
					} else {
						throw new RuntimeException("Unresolved shader include: " + includePath);
					}
				} else {
					sb.append(line);
				}

				lb.delete(0, lb.length());
			}
		}

		return sb.toString();
	}

	@Override
	protected GLMaterialShaderManager createFshManager() {
		return new GLMaterialShaderManager();
	}
}
