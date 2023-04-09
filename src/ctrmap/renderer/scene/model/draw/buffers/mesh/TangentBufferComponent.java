
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.FloatBuffer;

public class TangentBufferComponent extends MeshBufferComponent {
	
	private FloatBuffer tangentBuffer = null;
	
	public TangentBufferComponent(MeshVertexBuffer buffer){
		super(buffer);
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
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.TANGENT);
			tangentBuffer = FloatBuffer.allocate(mesh.vertices.sizeForAttribute(VertexAttributeType.TANGENT) * 3);

			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
				tangentBuffer.put(v.tangent.x);
				tangentBuffer.put(v.tangent.y);
				tangentBuffer.put(v.tangent.z);
			}
		}
		else {
			tangentBuffer = null;
		}
	}

	@Override
	public int getElementCount() {
		return 3;
	}
}
