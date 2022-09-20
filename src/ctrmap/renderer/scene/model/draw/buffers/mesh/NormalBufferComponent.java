
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class NormalBufferComponent extends BufferComponent {

	private Mesh mesh;
	
	private FloatBuffer normalBuffer = null;
	
	public NormalBufferComponent(MeshVertexBuffer buffer){
		super(buffer);
		this.mesh = buffer.mesh;
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.hasNormal;
	}

	@Override
	public Buffer getBuffer() {
		return normalBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.FLOAT;
	}

	@Override
	public void updateImpl() {
		createNormalBuffer();
	}
	
	public void createNormalBuffer() {
		if (isEnabled()) {
			normalBuffer = FloatBuffer.allocate(mesh.vertices.size() * 3);

			for (Vertex v : mesh.vertices) {
				normalBuffer.put(v.normal.x);
				normalBuffer.put(v.normal.y);
				normalBuffer.put(v.normal.z);
			}
		}
		else {
			normalBuffer = null;
		}
	}
}
