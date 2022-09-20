
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class TangentBufferComponent extends BufferComponent {

	private Mesh mesh;
	
	private FloatBuffer tangentBuffer = null;
	
	public TangentBufferComponent(MeshVertexBuffer buffer){
		super(buffer);
		this.mesh = buffer.mesh;
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.hasTangent;
	}

	@Override
	public Buffer getBuffer() {
		return tangentBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.FLOAT;
	}

	@Override
	public void updateImpl() {
		createTangentBuffer();
	}
	
	public void createTangentBuffer() {
		if (isEnabled()) {
			tangentBuffer = FloatBuffer.allocate(mesh.vertices.size() * 3);

			for (Vertex v : mesh.vertices) {
				tangentBuffer.put(v.tangent.x);
				tangentBuffer.put(v.tangent.y);
				tangentBuffer.put(v.tangent.z);
			}
		}
		else {
			tangentBuffer = null;
		}
	}
}
