package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.FloatBuffer;

public class BoneWeightBufferComponent extends MeshBufferComponent {

	private FloatBuffer weightBuffer = null;

	public BoneWeightBufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
	}

	@Override
	public boolean isEnabled() {
		return mesh.hasBoneWeights;
	}

	@Override
	public Buffer getBuffer() {
		return weightBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.FLOAT;
	}

	@Override
	public void updateImpl() {
		createBoneWeightBuffer();
	}

	public void createBoneWeightBuffer() {
		if (isEnabled()) {
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.BONE_WEIGHT);
			weightBuffer = FloatBuffer.allocate(count * 4);

			int listSize;
			float weightSum;
			float w;
			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
				listSize = Math.min(4, v.weights.size());
				weightSum = 0f;
				for (int i = 0; i < listSize; i++) {
					w = v.weights.get(i);
					weightBuffer.put(w);
					weightSum += w;
				}
				for (int j = listSize; j < 4; j++) {
					if (j < v.boneIndices.size()) {
						weightBuffer.put(1f - weightSum);
						weightSum = 1f;
					} else {
						weightBuffer.put(0f);
					}
				}
			}
		} else {
			weightBuffer = null;
		}
	}

	@Override
	public int getElementCount() {
		return 4;
	}
}
