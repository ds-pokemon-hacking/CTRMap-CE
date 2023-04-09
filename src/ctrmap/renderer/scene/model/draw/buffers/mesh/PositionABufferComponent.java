
package ctrmap.renderer.scene.model.draw.buffers.mesh;

import ctrmap.renderer.scene.model.VertexMorph;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;

public class PositionABufferComponent extends PositionBufferComponent {

	public PositionABufferComponent(MeshVertexBuffer buffer) {
		super(buffer);
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
