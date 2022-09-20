
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import xstandard.math.vec.Vec2f;
import java.nio.Buffer;
import java.nio.FloatBuffer;

public class UVBufferComponent extends BufferComponent {

	private Mesh mesh;
	
	private FloatBuffer uvBuffer;
	
	public UVBufferComponent(MeshVertexBuffer buffer, int uvIndex){
		super(buffer, uvIndex);
		this.mesh = buffer.mesh;
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.hasUV(setNo);
	}

	@Override
	public Buffer getBuffer() {
		return uvBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.FLOAT;
	}

	@Override
	public void updateImpl() {
		createUVBuffer();
	}
	
	public void createUVBuffer() {
		if (isEnabled()) {
			uvBuffer = FloatBuffer.allocate(mesh.vertices.size() * 2);

			for (Vertex v : mesh.vertices) {
				Vec2f uv = new Vec2f(v.uv[setNo].x, v.uv[setNo].y);
				uvBuffer.put(uv.x);
				uvBuffer.put(uv.y);
			}
		}
		else {
			uvBuffer = null;
		}
	}
}
