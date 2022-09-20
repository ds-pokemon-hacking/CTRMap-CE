package ctrmap.renderer.backends.base.shaderengine;

import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 */
public class UserShaderManager {

	protected List<FSFile> shaderIncludeDirectories = new ArrayList<>();

	protected String defaultShaderName;
	protected String overrideShaderName;
	
	protected static Map<Integer, String> shaderSourcesRaw = new HashMap<>();
	protected static Map<Integer, String> shaderSourcesExtended = new HashMap<>();
	protected static Map<String, ShaderExtensionSource> extensionSources = new HashMap<>();

	public void addIncludeDirectory(FSFile fsf) {
		if (!fsf.isDirectory()) {
			System.err.println("WARN: Shader include is not a directory");
			return;
		}
		shaderIncludeDirectories.add(fsf);
	}

	public void setDefaultShaderName(String name) {
		defaultShaderName = name;
	}

	public String getDefaultShaderName() {
		return defaultShaderName;
	}
	
	public boolean setOverrideShaderName(String name) {
		boolean b;
		if (name == null) {
			b = overrideShaderName == null;
		} else {
			b = name.equals(overrideShaderName);
		}
		overrideShaderName = name;
		return !b;
	}

	public void clearOverrideShaderName() {
		overrideShaderName = null;
	}
	
	public String getShaderSource(String shaderPath, ShaderProgramManager prgMan, String... extensions) {
		return getShaderSource(shaderPath, prgMan, true, extensions);
	}

	public String getShaderSource(String shaderPath, ShaderProgramManager prgMan, boolean getExtData, String... extensions) {
		if (overrideShaderName != null && !shaderPath.equals(overrideShaderName) && getExtData) {
			return getShaderSource(overrideShaderName, prgMan);
		}
		int rawKey = Objects.hashCode(shaderPath);

		if (!shaderSourcesRaw.containsKey(rawKey)) {
			for (FSFile dir : shaderIncludeDirectories) {
				FSFile test = dir.getChild(shaderPath);
				if (test != null && test.isFile()) {
					String code = new String(FSUtil.readFileToBytes(test), StandardCharsets.UTF_8);
					if (code.startsWith("#version HOUSTON_EX")) {
						String baseSource = getShaderSource(defaultShaderName, prgMan, false);
						ShaderExtensionSource exSrc = new ShaderExtensionSource(code);
						shaderSourcesRaw.put(rawKey, exSrc.getExtendedShader(baseSource));
						extensionSources.put(shaderPath, exSrc);
					} else {
						shaderSourcesRaw.put(rawKey, code);
					}
				}
			}
		}

		String baseSource = shaderSourcesRaw.get(rawKey);

		if (!getExtData) {
			return baseSource;
		}
		if (baseSource == null) {
			baseSource = getShaderSource(defaultShaderName, prgMan, false);
		}
		if (baseSource == null){
			throw new RuntimeException("CRITICAL: COULD NOT OBTAIN DEFAULT SHADER SOURCE " + defaultShaderName);
		}

		int extKey = prgMan.getCombHash(rawKey, extensions);
		if (!shaderSourcesExtended.containsKey(extKey)) {
			shaderSourcesExtended.put(extKey, prgMan.getExtendedShaderSource(baseSource, extensions));
			
			//System.out.println("new extshader " + shaderPath + " ext: " + Arrays.toString(extensions));
		}
		else {
			//System.out.println("already satisfied " + shaderPath + " ext: " + Arrays.toString(extensions));
		}

		String shaderSource = shaderSourcesExtended.get(extKey);

		return shaderSource;
	}

	public ShaderExtensionSource getShaderExtensionSource(String shaderPath) {
		if (!extensionSources.containsKey(shaderPath)) {
			for (FSFile dir : shaderIncludeDirectories) {
				FSFile test = dir.getChild(shaderPath);
				if (test != null && test.isFile()) {
					extensionSources.put(shaderPath, new ShaderExtensionSource(new String(FSUtil.readFileToBytes(test), StandardCharsets.UTF_8)));
				}
			}
		}
		ShaderExtensionSource shaderSource = extensionSources.get(shaderPath);
		return shaderSource;
	}
}
