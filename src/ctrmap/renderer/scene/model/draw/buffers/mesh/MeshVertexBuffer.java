
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.draw.buffers.Buffer;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListUsage;

public class MeshVertexBuffer extends Buffer {
	public final Mesh mesh;
	
	public final PositionABufferComponent posA;
	public final MirrorBufferComponent posB;
	public final NormalBufferComponent nrmA;
	public final MirrorBufferComponent nrmB;
	public final TangentBufferComponent tgtA;
	public final MirrorBufferComponent tgtB;
	public final ColorBufferComponent col;
	public final UVBufferComponent[] uv = new UVBufferComponent[3];
	public final BoneIndexBufferComponent bidx;
	public final BoneWeightBufferComponent bwgt;
	
	public MeshVertexBuffer(Mesh mesh){
		this.mesh = mesh;
		addComponent(posA = new PositionABufferComponent(this));
		addComponent(posB = new MirrorBufferComponent(this, posA));
		addComponent(nrmA = new NormalBufferComponent(this));
		addComponent(nrmB = new MirrorBufferComponent(this, nrmA));
		addComponent(tgtA = new TangentBufferComponent(this));
		addComponent(tgtB = new MirrorBufferComponent(this, tgtA));
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
