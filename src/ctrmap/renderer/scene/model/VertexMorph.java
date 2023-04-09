
package ctrmap.renderer.scene.model;

import ctrmap.renderer.scene.model.draw.vtxlist.AbstractVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexArrayList;
import ctrmap.renderer.scenegraph.NamedResource;

public class VertexMorph implements NamedResource {
	public String name;
	public AbstractVertexList vertices = new VertexArrayList();
	
	public VertexMorph() {
		
	}
	
	public VertexMorph(VertexMorph morph) {
		name = morph.name;
		vertices.addAll(morph.vertices);
	}
	
	public static VertexMorph fromMesh(Mesh mesh) {
		VertexMorph morph = new VertexMorph();
		morph.name = mesh.name;
		for (Vertex vtx : mesh.vertices) {
			morph.vertices.add(vtx);
		}
		return morph;
	}
	
	public Mesh toMesh() {
		Mesh out = new Mesh();
		out.name = name;
		out.vertices.addAll(this.vertices);
		return out;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
