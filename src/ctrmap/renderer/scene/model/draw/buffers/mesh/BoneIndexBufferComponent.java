package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 *
 */
public class BoneIndexBufferComponent extends MeshBufferComponent {

	private ByteBuffer idxBuffer;

	public BoneIndexBufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
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
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.BONE_INDEX);
			idxBuffer = ByteBuffer.allocateDirect(count * 4);

			int listSize;
			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
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

	@Override
	public int getElementCount() {
		return 4;
	}
}
