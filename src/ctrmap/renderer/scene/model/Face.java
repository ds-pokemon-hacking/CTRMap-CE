package ctrmap.renderer.scene.model;

public class Face {

	public final PrimitiveType primitiveType;
	public final Vertex[] vertices;
	
	public final int vertexBufferOffset;

	public Face(PrimitiveType primitiveType, Mesh mesh, int off) {
		this.primitiveType = primitiveType;
		vertexBufferOffset = off;
		int stride = PrimitiveType.getPrimitiveTypeSeparationSize(primitiveType, mesh);
		vertices = new Vertex[stride];
		for (int i = 0; i < stride; i++) {
			if (mesh.useIBO) {
				vertices[i] = mesh.vertices.get(mesh.indices.get(off + i));
			}
			else {
				vertices[i] = mesh.vertices.get(off + i);
			}
		}
	}
}
