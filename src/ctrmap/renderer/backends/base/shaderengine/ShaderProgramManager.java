package ctrmap.renderer.backends.base.shaderengine;

import ctrmap.renderer.backends.DriverHandle;
import ctrmap.renderer.backends.base.flow.IRenderDriver;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ShaderProgramManager {

	private final List<String> activeOmniShaderExtensions = new ArrayList<>();

	protected final List<ShaderDefinition> definitions = new ArrayList<>();

	private final Map<Integer, ShaderProgram> programPointers = new HashMap<>();
	private final Map<Integer, DriverHandle> vshPointers = new HashMap<>();
	private final Map<Integer, DriverHandle> fshPointers = new HashMap<>();

	private final List<ShaderUniform> extraUniforms = new ArrayList<>();

	protected UserShaderManager ushManager = new UserShaderManager();
	protected MaterialShaderManager fshManager = createFshManager();

	protected abstract MaterialShaderManager createFshManager();

	public UserShaderManager getUserShManager() {
		return ushManager;
	}

	public MaterialShaderManager getFshManager() {
		return fshManager;
	}

	public void setFshManager(MaterialShaderManager man) {
		this.fshManager = man;
	}
	
	public void freeDriver(IRenderDriver drv) {
		for (ShaderProgram p : programPointers.values()) {
			drv.deleteProgram(p);
		}
		
		for (DriverHandle h : vshPointers.values()) {
			int p = h.get(drv);
			if (p != -1) {
				drv.deleteShader(p);
			}
		}
		
		for (DriverHandle h : fshPointers.values()) {
			int p = h.get(drv);
			if (p != -1) {
				drv.deleteShader(p);
			}
		}
	}

	protected abstract String createExtendedShaderSource(String shaderSource);

	public void clearShaderCache() {
		vshPointers.clear();
		programPointers.clear();
		fshPointers.clear();
	}

	public List<String> getExtensions() {
		return activeOmniShaderExtensions;
	}

	public List<ShaderUniform> getExtraUniforms() {
		return extraUniforms;
	}

	public void loadExtension(String name) {
		if (ArraysEx.addIfNotNullOrContains(activeOmniShaderExtensions, name)) {
			getExtendedShaderSource(name, getCombExtensions());
		}
	}

	public void unloadExtension(String name) {
		activeOmniShaderExtensions.remove(name);
	}

	public void clearExtensions() {
		activeOmniShaderExtensions.clear();
	}

	public void clearOverrideVshName() {
		setOverrideVshName(null);
	}

	public void setOverrideVshName(String name) {
		if (ushManager.setOverrideShaderName(name)) {
			clearShaderCache();
		}
	}

	public void addExtraUniform(ShaderUniform val) {
		ArraysEx.addIfNotNullOrContains(extraUniforms, val);
	}

	public void removeExtraUniform(ShaderUniform val) {
		extraUniforms.remove(val);
	}

	public void clearExtraUniforms() {
		extraUniforms.clear();
	}

	public void addShaderDefinition(ShaderDefinition def) {
		definitions.add(def);
	}

	public void removeShaderDefinition(ShaderDefinition def) {
		definitions.remove(def);
	}

	public void removeShaderDefinition(String key) {
		for (ShaderDefinition def : definitions) {
			if (def.key.equals(key)) {
				definitions.remove(def);
				return;
			}
		}
	}

	public String[] getCombExtensions(String... additionalExtensions) {
		String[] result = Arrays.copyOf(additionalExtensions, additionalExtensions.length + activeOmniShaderExtensions.size());
		System.arraycopy(activeOmniShaderExtensions.toArray(new String[activeOmniShaderExtensions.size()]), 0, result, additionalExtensions.length, activeOmniShaderExtensions.size());
		return result;
	}

	public int compileShaderProgramSafe(IRenderDriver gl, int vsh, int fsh, String programIdentifier) {
		try {
			return gl.linkProgram(vsh, fsh);
		} catch (ShaderCompilerException ex) {
			System.err.println("--- SHADER COMPILATION FAILED ---");
			System.err.println("Program: " + programIdentifier);
			System.err.println();
			Logger.getLogger(ShaderProgramManager.class.getName()).log(Level.SEVERE, null, ex);
			System.err.println("---------------------------------");
		}
		return -1;
	}

	public int getVertexShader(IRenderDriver drv, String shaderName, String... extensions) {
		if (shaderName == null) {
			shaderName = ushManager.defaultShaderName;
		}
		int key = getCombHash(shaderName, extensions);

		DriverHandle hnd = vshPointers.get(key);
		if (hnd == null) {
			hnd = new DriverHandle();
			vshPointers.put(key, hnd);
		}

		int ptr = hnd.get(drv);
		if (ptr == -1) {
			ptr = drv.compileShader(ushManager.getShaderSource(shaderName, this, getCombExtensions(extensions)), IRenderDriver.ShaderType.VERTEX_SHADER);
		}

		return ptr;
	}

	public int getFragmentShader(IRenderDriver drv, Material mat, String... extensions) {
		if (mat == null || mat.fshType == MaterialParams.FragmentShaderType.CTR_COMBINER) {
			int shadingHash = mat == null ? 0 : mat.getFragmentShaderHash();
			shadingHash = getCombHash(shadingHash, extensions);

			DriverHandle hnd = fshPointers.get(shadingHash);
			if (hnd == null) {
				hnd = new DriverHandle();
				fshPointers.put(shadingHash, hnd);
			}

			int fsh = hnd.get(drv);

			if (fsh == -1) {
				fsh = drv.compileShader(getFshManager().getShaderSource(mat, this, extensions), IRenderDriver.ShaderType.FRAGMENT_SHADER);
			}
			return fsh;
		} else {
			return compileFragmentUserShader(drv, mat.fragmentShaderName, extensions);
		}
	}

	public int compileFragmentUserShader(IRenderDriver drv, String shaderName, String... extensions) {
		int shadingHash = Objects.hashCode(shaderName);
		shadingHash = getCombHash(shadingHash, extensions);

		DriverHandle hnd = fshPointers.get(shadingHash);
		if (hnd == null) {
			hnd = new DriverHandle();
			fshPointers.put(shadingHash, hnd);
		}

		int fsh = hnd.get(drv);

		if (fsh == -1) {
			fsh = drv.compileShader(ushManager.getShaderSource(shaderName, this, getCombExtensions(extensions)), IRenderDriver.ShaderType.FRAGMENT_SHADER);
		}
		return fsh;
	}

	public int compileShaderProgram(IRenderDriver renderer, String vshName, String fshName) {
		return compileShaderProgramSafe(renderer,
			getVertexShader(
				renderer,
				vshName,
				new String[0]
			),
			compileFragmentUserShader(renderer, fshName),
			vshName + "/" + fshName
		);
	}

	public int compileShaderProgram(IRenderDriver renderer, Material material) {
		return compileShaderProgramSafe(renderer,
			getVertexShader(
				renderer,
				material == null ? null : material.vertexShaderName,
				material == null ? new String[0] : material.getShaderExtensions()
			),
			getFragmentShader(renderer, material, material == null ? new String[0] : material.getShaderExtensions()),
			material == null ? "Generic shader" : material.name
		);
	}

	public ShaderProgram getShaderProgram(IRenderDriver drv, Material material) {
		int materialHash = getCombHash(material == null ? 0 : material.getShadingHash());
		ShaderProgram program = programPointers.get(materialHash);
		if (program == null) {
			program = new ShaderProgram();
			programPointers.put(materialHash, program);
		}
		if (program.handle.get(drv) == -1) {
			program.handle.set(drv, compileShaderProgram(drv, material));
		}
		return program;
	}
	
	public ShaderProgram getShaderProgram(IRenderDriver drv, String vertShaderName, String fragShaderName) {
		int nameHash = fragShaderName.hashCode() * 37 + vertShaderName.hashCode();
		int fullHash = getCombHash(nameHash);
		ShaderProgram program = programPointers.get(fullHash);
		if (program == null) {
			program = new ShaderProgram();
			programPointers.put(fullHash, program);
		}
		if (program.handle.get(drv) == -1) {
			program.handle.set(drv, compileShaderProgram(drv, vertShaderName, fragShaderName));
		}
		return program;
	}

	public String getExtendedShaderSource(String shaderSource, String... extensions) {
		/*for (String extName : activeOmniShaderExtensions) {
			ShaderExtensionSource exSrc = ushManager.getShaderExtensionSource(extName);
			if (exSrc != null) {
				shaderSource = exSrc.getExtendedShader(shaderSource);
			}
		}*/
		for (String extName : extensions) {
			ShaderExtensionSource exSrc = ushManager.getShaderExtensionSource(extName);
			if (exSrc != null) {
				shaderSource = exSrc.getExtendedShader(shaderSource);
			} else {
				System.err.println("Could not find shader extension " + extName + "!");
			}
		}

		shaderSource = createExtendedShaderSource(shaderSource);

		return shaderSource;
	}

	public int getCombHash(Object baseKey, String... additionalExtensions) {
		int hash = 7;
		hash = 37 * hash + Objects.hashCode(baseKey);
		hash = 37 * hash + Arrays.hashCode(additionalExtensions);
		hash = 37 * hash + getExtensionsHash();
		return hash;
	}

	private int getExtensionsHash() {
		int hash = 7;
		for (String ext : activeOmniShaderExtensions) {
			hash = 37 * hash + Objects.hashCode(ext);
		}
		return hash;
	}
}
