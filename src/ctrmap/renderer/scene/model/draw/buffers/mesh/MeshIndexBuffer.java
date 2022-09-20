
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.draw.buffers.Buffer;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;

public class MeshIndexBuffer extends Buffer {
	public final Mesh mesh;
	
	public final IndexBufferComponent idxBuf;
	
	public MeshIndexBuffer(Mesh mesh){
		this.mesh = mesh;
		idxBuf = new IndexBufferComponent(this);
		addComponent(idxBuf);
	}

	@Override
	public BufferTarget getTarget() {
		return BufferTarget.ELEMENT_ARRAY_BUFFER;
	}

	@Override
	public VertexListUsage getDrawType() {
		return mesh.bufferType;
	}

	@Override
	public boolean isBindEnabled() {
		return mesh.useIBO;
	}
}
