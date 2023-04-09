package ctrmap.renderer.scene.model.draw.buffers;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.buffers.mesh.MeshIndexBuffer;
import ctrmap.renderer.scene.model.draw.buffers.mesh.MeshVertexBuffer;

public class MeshBufferManager extends BufferManager {

	private Mesh mesh;

	public final MeshVertexBuffer vbo;
	public final MeshIndexBuffer ibo;

	private int lastUpdatedVertexCount = 0;
	private int lastUpdatedIndexCount = 0;

	public MeshBufferManager(Mesh mesh) {
		this.mesh = mesh;
		vbo = new MeshVertexBuffer(mesh);
		ibo = new MeshIndexBuffer(mesh);
		addBuffer(vbo);
		addBuffer(ibo);
	}

	public int vertexCount() {
		return lastUpdatedVertexCount;
	}

	public int indexCount() {
		return lastUpdatedIndexCount;
	}

	@Override
	public void updateAll() {
		super.updateAll();

		lastUpdatedVertexCount = mesh.getRealVertexCount();
		lastUpdatedIndexCount = mesh.indices.size();
	}
}
