
package ctrmap.renderer.scene.model.draw.vtxlist;

import ctrmap.renderer.scene.model.Vertex;
import java.util.AbstractList;
import java.util.Collection;

public abstract class AbstractVertexList extends AbstractList<Vertex> implements IVertexList {
	
	public AbstractVertexList(Collection<? extends Vertex> c){
		addAll(c);
	}
	
	public AbstractVertexList(){
		
	}
	
	@Override
	public abstract void add(int index, Vertex v);

	@Override
	public abstract Vertex set(int index, Vertex v);

	@Override
	public abstract Vertex remove(int index);
	
	public abstract VertexListType getType();
	
}
