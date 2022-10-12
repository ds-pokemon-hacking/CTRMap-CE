
package ctrmap.renderer.scene.model.draw.vtxlist;

import ctrmap.renderer.scene.model.Vertex;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class VertexArrayList extends AbstractVertexList {
	
	private List<Vertex> backend = new ArrayList<>();
	
	public VertexArrayList(){
		
	}
	
	public VertexArrayList(Collection<? extends Vertex> c){
		backend.addAll(c);
	}

	@Override
	public void add(int index, Vertex v) {
		backend.add(index, v);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Vertex> c){
		return backend.addAll(index, c);
	}
	
	@Override
	public Vertex set(int index, Vertex v) {
		return backend.set(index, v);
	}

	@Override
	public Vertex remove(int index) {
		return backend.remove(index);
	}

	@Override
	public Vertex get(int index) {
		return backend.get(index);
	}

	@Override
	public int size() {
		return backend.size();
	}

	@Override
	public Iterator<Vertex> iterator(){
		return backend.iterator();
	}

	@Override
	public VertexListType getType() {
		return VertexListType.SINGLE;
	}
}
