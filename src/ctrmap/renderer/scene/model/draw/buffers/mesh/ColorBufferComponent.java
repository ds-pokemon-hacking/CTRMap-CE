
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 *
 */
public class ColorBufferComponent extends BufferComponent {

	private Mesh mesh;
	
	private ByteBuffer colorBuffer;
	
	public ColorBufferComponent(MeshVertexBuffer buffer){
		super(buffer);
		this.mesh = buffer.mesh;
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
			colorBuffer = ByteBuffer.allocateDirect(mesh.vertices.size() * 4);

			for (Vertex v : mesh.vertices) {
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
}
