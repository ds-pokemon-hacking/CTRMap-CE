
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.draw.buffers.Buffer;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;

public class MeshVertexBuffer extends Buffer {
	public final Mesh mesh;
	
	public final PositionBufferComponent pos;
	public final NormalBufferComponent nrm;
	public final TangentBufferComponent tgt;
	public final ColorBufferComponent col;
	public final UVBufferComponent[] uv = new UVBufferComponent[3];
	public final BoneIndexBufferComponent bidx;
	public final BoneWeightBufferComponent bwgt;
	
	public MeshVertexBuffer(Mesh mesh){
		this.mesh = mesh;
		addComponent(pos = new PositionBufferComponent(this));
		addComponent(nrm = new NormalBufferComponent(this));
		addComponent(tgt = new TangentBufferComponent(this));
		addComponent(col = new ColorBufferComponent(this));
		for (int i = 0; i < 3; i++){
			addComponent(uv[i] = new UVBufferComponent(this, i));
		}
		addComponent(bidx = new BoneIndexBufferComponent(this));
		addComponent(bwgt = new BoneWeightBufferComponent(this));
	}

	@Override
	public BufferTarget getTarget() {
		return BufferTarget.ARRAY_BUFFER;
	}

	@Override
	public VertexListUsage getDrawType() {
		return mesh.bufferType;
	}

	@Override
	public boolean isBindEnabled() {
		return true;
	}
}
