
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.ShortBuffer;

public class IndexBufferComponent extends BufferComponent {

	private Mesh mesh;
	
	private ShortBuffer ibo = null;
	
	public IndexBufferComponent(MeshIndexBuffer buffer){
		super(buffer);
		this.mesh = buffer.mesh;
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.useIBO;
	}

	@Override
	public Buffer getBuffer() {
		return ibo;
	}

	@Override
	public BufferComponentType getType() {
		return BufferComponentType.SHORT;
	}

	@Override
	public void updateImpl() {
		int max = mesh.indices.size();
		ibo = ShortBuffer.allocate(max);
		for (int i = 0; i < max; i++){
			ibo.put((short)mesh.indices.get(i));
		}
	}
}
