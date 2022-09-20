package ctrmap.renderer.backends.base.flow;

import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.buffers.Buffer;
import ctrmap.renderer.scene.texturing.Texture;
import xstandard.util.EnumBitflags;
import java.util.HashMap;
import java.util.Map;

public class BufferObjectMemory {

	private final IRenderDriver driver;

	public Map<Integer, Buffer> buffers = new HashMap<>();
	public Map<Integer, Texture> textures = new HashMap<>();
	public Map<Integer, ShaderProgram> programs = new HashMap<>();

	public BufferObjectMemory(IRenderDriver driver) {
		this.driver = driver;
	}

	public BufferObjectMemory diff(BufferObjectMemory last) {
		BufferObjectMemory diff = new BufferObjectMemory(driver);

		diffMap(diff.buffers, buffers, last.buffers);
		diffMap(diff.textures, textures, last.textures);
		diffMap(diff.programs, programs, last.programs);

		return diff;
	}

	public void gc(EnumBitflags<GCFilter> filter) {
		if (filter.isSet(GCFilter.TEXTURES)) {
			int[] textureHandles = new int[textures.size()];

			int index = 0;
			for (Map.Entry<Integer, Texture> tex : textures.entrySet()) {
				int ptr = tex.getKey();
				textureHandles[index] = ptr;
				tex.getValue().deletePointer(driver);
				index++;
			}

			if (textureHandles.length > 0) {
				driver.deleteTextures(textureHandles);
			}
		}

		if (filter.isSet(GCFilter.BUFFERS)) {
			int[] bufferHandles = new int[buffers.size()];

			int index = 0;
			for (Map.Entry<Integer, Buffer> buf : buffers.entrySet()) {
				int ptr = buf.getKey();
				bufferHandles[index] = ptr;
				buf.getValue().deleteHandle(driver);
				index++;
			}

			if (bufferHandles.length > 0) {
				driver.deleteBuffers(bufferHandles);
			}
		}

		if (filter.isSet(GCFilter.PROGRAMS)) {
			for (Map.Entry<Integer, ShaderProgram> program : programs.entrySet()) {
				ShaderProgram p = program.getValue();
				driver.deleteProgram(p);
				p.handle.remove(driver);
			}
		}
	}

	private void diffMap(Map diffList, Map newList, Map oldList) {
		diffList.putAll(oldList);
		for (Object key : newList.keySet()) {
			diffList.remove(key);
		}
	}

	public void registTexture(Texture tex) {
		int handle = tex.getPointer(driver);
		if (handle != -1) {
			textures.put(handle, tex);
		}
	}

	public void registMesh(Mesh mesh) {
		for (Buffer buf : mesh.buffers.buffers) {
			int handle = buf.getHandleForDriver(driver);
			if (handle != -1) {
				buffers.put(handle, buf);
			}
		}
	}

	public void registProgram(ShaderProgram program) {
		int handle = program.handle.get(driver);
		if (handle != -1) {
			programs.put(handle, program);
		}
	}

	public static enum GCFilter {
		BUFFERS,
		TEXTURES,
		PROGRAMS
	}
}
