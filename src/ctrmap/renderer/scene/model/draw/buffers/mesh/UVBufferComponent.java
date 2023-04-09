
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import xstandard.math.vec.Vec2f;
import java.nio.Buffer;
import java.nio.FloatBuffer;

public class UVBufferComponent extends MeshBufferComponent {
	
	private FloatBuffer uvBuffer;

	public UVBufferComponent(MeshVertexBuffer buf, int setNo) {
		super(buf, setNo);
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
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.UV, setNo);
			uvBuffer = FloatBuffer.allocate(count * 2);
			
			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
				Vec2f uv = new Vec2f(v.uv[setNo].x, v.uv[setNo].y);
				uvBuffer.put(uv.x);
				uvBuffer.put(uv.y);
			}
		}
		else {
			uvBuffer = null;
		}
	}

	@Override
	public int getElementCount() {
		return 2;
	}
}
