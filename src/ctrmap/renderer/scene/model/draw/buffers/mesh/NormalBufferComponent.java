
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.VertexMorph;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import java.nio.Buffer;
import java.nio.FloatBuffer;

/**
 *
 */
public class NormalBufferComponent extends MeshBufferComponent {
	
	private FloatBuffer normalBuffer = null;
	
	public NormalBufferComponent(MeshVertexBuffer buffer){
		super(buffer);
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.hasNormal;
	}

	@Override
	public Buffer getBuffer() {
		return normalBuffer;
	}

	@Override
	public BufferComponent.BufferComponentType getType() {
		return BufferComponent.BufferComponentType.FLOAT;
	}

	@Override
	public void updateImpl() {
		createNormalBuffer();
	}
	
	public void createNormalBuffer() {
		if (isEnabled()) {
			int count = mesh.vertices.sizeForAttribute(VertexAttributeType.NORMAL);
			normalBuffer = FloatBuffer.allocate(count * 3);

			int index = 0;
			for (Vertex v : mesh.vertices) {
				if (index++ >= count) {
					break;
				}
				normalBuffer.put(v.normal.x);
				normalBuffer.put(v.normal.y);
				normalBuffer.put(v.normal.z);
			}
		}
		else {
			normalBuffer = null;
		}
	}

	@Override
	public int getElementCount() {
		return 3;
	}
	
	@Override
	public void processVertexMorph(MorphableVertexList vl) {
		VertexMorph m = vl.currentMorph();
		if (m != null) {
			morphIndex = vl.morphIndex(m);
		}
		else {
			morphIndex = 0;
		}
	}
}
