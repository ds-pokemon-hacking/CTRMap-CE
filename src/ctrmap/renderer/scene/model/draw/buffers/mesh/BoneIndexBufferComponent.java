package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 *
 */
public class BoneIndexBufferComponent extends BufferComponent {

	private Mesh mesh;

	private ByteBuffer idxBuffer;

	public BoneIndexBufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
		this.mesh = buffer.mesh;
	}

	@Override
	public boolean isEnabled() {
		return mesh.hasBoneIndices;
	}

	@Override
	public Buffer getBuffer() {
		return idxBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.BYTE;
	}

	@Override
	public void updateImpl() {
		createBoneIndexBuffer();
	}

	public void createBoneIndexBuffer() {
		if (isEnabled()) {
			idxBuffer = ByteBuffer.allocateDirect(mesh.vertices.size() * 4);

			int listSize;
			for (Vertex v : mesh.vertices) {
				listSize = Math.min(4, v.boneIndices.size());
				for (int i = 0; i < listSize; i++) {
					idxBuffer.put((byte)v.boneIndices.get(i));
				}
				for (int j = listSize; j < 4; j++){
					idxBuffer.put((byte)255);
				}
			}
		} else {
			idxBuffer = null;
		}
	}
}
