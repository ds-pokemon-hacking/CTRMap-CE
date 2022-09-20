package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class BoneWeightBufferComponent extends BufferComponent {

	private Mesh mesh;

	private FloatBuffer weightBuffer = null;

	public BoneWeightBufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
		this.mesh = buffer.mesh;
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
			weightBuffer = FloatBuffer.allocate(mesh.vertices.size() * 4);

			int listSize;
			float weightSum;
			float w;
			for (Vertex v : mesh.vertices) {
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
}
