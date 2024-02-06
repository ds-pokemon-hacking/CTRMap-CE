package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import ctrmap.renderer.scene.model.draw.vtxlist.AbstractVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListType;

public abstract class MeshBufferComponent extends BufferComponent {

	protected final Mesh mesh;
	
	private int morphCount = 1;
	protected int morphIndex = 0;

	public MeshBufferComponent(MeshVertexBuffer buf) {
		this(buf, 0);
	}

	public MeshBufferComponent(MeshVertexBuffer buf, int setNo) {
		super(buf, setNo);
		this.mesh = buf.mesh;
	}
	
	public void processVertexMorph(MorphableVertexList vl) {
		
	}
	
	@Override
	public void update() {
		AbstractVertexList vl = mesh.vertices;
		if (vl.getType() == VertexListType.MORPH) {
			MorphableVertexList morph = (MorphableVertexList) vl;
			morphCount = morph.getMorphCount();
		}
		else {
			morphIndex = 0;
			morphCount = 1;
		}
		super.update();
	}
	
	public int getMorphChannelSize() {
		if (morphCount == 0) {
			return 0;
		}
		return getByteSize() / morphCount;
	}

	@Override
	public int getOffset() {
		return super.getOffset() + morphIndex * getMorphChannelSize();
	}
}
