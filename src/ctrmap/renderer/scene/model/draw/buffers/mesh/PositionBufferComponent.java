package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.FloatBuffer;

public class PositionBufferComponent extends MeshBufferComponent {

	private FloatBuffer positionBuffer = null;

	public PositionBufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public Buffer getBuffer() {
		return positionBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.FLOAT;
	}

	@Override
	public void updateImpl() {
		createPositionBuffer();
	}

	public void createPositionBuffer() {
		if (isEnabled()) {
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.POSITION);
			positionBuffer = FloatBuffer.allocate(count * 3);

			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
				positionBuffer.put(v.position.x);
				positionBuffer.put(v.position.y);
				positionBuffer.put(v.position.z);
			}
		}
		else {
			positionBuffer = null;
		}
	}

	@Override
	public int getElementCount() {
		return 3;
	}
}
