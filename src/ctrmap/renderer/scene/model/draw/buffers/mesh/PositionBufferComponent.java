package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Joint;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Skeleton;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import ctrmap.renderer.scenegraph.G3DResourceState;
import xstandard.math.MatrixUtil;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec3f;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PositionBufferComponent extends BufferComponent {

	private Mesh mesh;

	private FloatBuffer positionBuffer = null;

	public PositionBufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
		this.mesh = buffer.mesh;
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
		positionBuffer = FloatBuffer.allocate(mesh.vertices.size() * 3);

		for (Vertex v : mesh.vertices) {
			positionBuffer.put(v.position.x);
			positionBuffer.put(v.position.y);
			positionBuffer.put(v.position.z);
		}
	}
}
