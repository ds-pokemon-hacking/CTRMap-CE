
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.ByteBuffer;

public class ColorBufferComponent extends MeshBufferComponent {
	
	private ByteBuffer colorBuffer;
	
	public ColorBufferComponent(MeshVertexBuffer buffer){
		super(buffer);
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.hasColor;
	}

	@Override
	public Buffer getBuffer() {
		return colorBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.BYTE;
	}

	@Override
	public void updateImpl() {
		createColorBuffer();
	}
	
	public void createColorBuffer() {
		if (isEnabled()) {
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.COLOR);
			colorBuffer = ByteBuffer.allocateDirect(count * 4);

			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
				colorBuffer.put((byte)v.color.r);
				colorBuffer.put((byte)v.color.g);
				colorBuffer.put((byte)v.color.b);
				colorBuffer.put((byte)v.color.a);
			}
		}
		else {
			colorBuffer = null;
		}
	}

	@Override
	public int getElementCount() {
		return 4;
	}
}
