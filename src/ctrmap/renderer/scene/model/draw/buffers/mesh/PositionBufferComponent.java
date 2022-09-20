
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
	
	public PositionBufferComponent(MeshVertexBuffer buffer){
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
		createPositionBuffer(null, null);
	}
	
	public void createPositionBuffer(G3DResourceState state, Model model) {
		positionBuffer = FloatBuffer.allocate(mesh.vertices.size() * 3);

		if (state != null && model != null) {
			List<Matrix4> perJointMatrices = new ArrayList<>();

			boolean isRigidSk = mesh.skinningType == Mesh.SkinningType.RIGID;

			for (Joint j : model.skeleton) {
				Matrix4 animatedTransformMatrix = state.animatedTransforms.get(j);
				if (!isRigidSk) {
					Matrix4 invBindPose = state.globalBindTransforms.get(j).clone();
					invBindPose.invert();
					animatedTransformMatrix = animatedTransformMatrix.clone();
					animatedTransformMatrix.mul(invBindPose);
				}
				perJointMatrices.add(animatedTransformMatrix);
			}

			for (Vertex v : mesh.vertices) {
				Vec3f p = state.transformVertex(v, perJointMatrices);
				positionBuffer.put(p.x);
				positionBuffer.put(p.y);
				positionBuffer.put(p.z);
			}
		} else {
			for (Vertex v : mesh.vertices) {
				positionBuffer.put(v.position.x);
				positionBuffer.put(v.position.y);
				positionBuffer.put(v.position.z);
			}
		}
	}
}
