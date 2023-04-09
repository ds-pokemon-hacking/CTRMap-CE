
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.VertexMorph;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListType;
import java.nio.Buffer;

public class MirrorBufferComponent extends MeshBufferComponent {

	private MeshBufferComponent src;
	
	public MirrorBufferComponent(MeshVertexBuffer buffer, MeshBufferComponent src) {
		super(buffer);
		this.src = src;
	}
	
	@Override
	public boolean isEnabled() {
		return mesh.vertices.getType() == VertexListType.MORPH && src.isEnabled();
	}

	@Override
	public void processVertexMorph(MorphableVertexList vl) {
		VertexMorph m = vl.lastMorph();
		if (m != null) {
			morphIndex = vl.morphIndex(m);
		}
		else {
			morphIndex = 0;
		}
	}
	
	@Override
	public int getOffset() {
		return src.getOffset() + (morphIndex - src.morphIndex) * src.getMorphChannelSize();
	}
	
	@Override
	public int getStride() {
		return src.getStride();
	}

	@Override
	public Buffer getBuffer() {
		return null;
	}

	@Override
	public int getElementCount() {
		return src.getElementCount();
	}

	@Override
	public BufferComponentType getType() {
		return src.getType();
	}

	@Override
	protected void updateImpl() {
		
	}
}
