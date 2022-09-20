package ctrmap.renderer.scene.model.draw.vtxlist;

import ctrmap.renderer.scene.model.Vertex;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class VertexArrayListMulti extends AbstractVertexList implements IVertexListMulti {

	private Stack<VertexListMultiComponent> components = new Stack<>();
	
	public VertexArrayListMulti(){
		clear();
	}
	
	public Iterable<List<Vertex>> lists(){
		return () -> new Iterator<List<Vertex>>() {
			
			private Iterator<VertexListMultiComponent> compIt = components.iterator();
			
			@Override
			public boolean hasNext() {
				return compIt.hasNext();
			}
			
			@Override
			public List<Vertex> next() {
				return compIt.next();
			}
		};
	}
	
	private VertexListMultiComponent getListForIdx(int index){
		for (VertexListMultiComponent l : components){
			if (index >= l.bottom() && index < l.top()){
				return l;
			}
		}
		throw new ArrayIndexOutOfBoundsException();
	}
	
	public void newList(){
		components.push(new VertexListMultiComponent(components.peek()));
	}
	
	public void removeList(){
		if (components.size() > 1){
			components.pop();
		}
		else {
			components.peek().clear();
		}
	}
	
	@Override
	public final void clear(){
		components.clear();
		components.add(new VertexListMultiComponent(null));
	}
	
	@Override
	public void add(int index, Vertex v) {
		getListForIdx(index).add(index, v);
	}

	@Override
	public Vertex set(int index, Vertex v) {
		return getListForIdx(index).set(index, v);
	}

	@Override
	public Vertex remove(int index) {
		return getListForIdx(index).remove(index);
	}

	@Override
	public Vertex get(int index) {
		return getListForIdx(index).get(index);
	}

	@Override
	public int size() {
		int size = 0;
		for (VertexListMultiComponent l : components){
			size += l.size();
		}
		return size;
	}

	@Override
	public Iterator<Vertex> iterator() {
		return new Iterator<Vertex>() {
			
			private int componentIndex = 0;
			private Iterator<Vertex> subListIt = components.get(0).iterator();
			
			@Override
			public boolean hasNext() {
				if (subListIt.hasNext()){
					return true;
				}
				else {
					componentIndex++;
					if (componentIndex < components.size()){
						subListIt = components.get(componentIndex).iterator();
					}
					else {
						return false;
					}
					return subListIt.hasNext();
				}
			}

			@Override
			public Vertex next() {
				return subListIt.next();
			}
		};
	}

	@Override
	public int[] first() {
		int[] first = new int[components.size()];
		
		for (int i = 0; i < components.size(); i++){
			first[i] = components.get(i).bottom();
		}
		
		return first;
	}
	
	@Override
	public int[] count() {
		int[] count = new int[components.size()];
		
		for (int i = 0; i < components.size(); i++){
			count[i] = components.get(i).size();
		}
		return count;
	}

	@Override
	public VertexListType getType() {
		return VertexListType.MULTI;
	}

	private static class VertexListMultiComponent extends ArrayList<Vertex> {

		private VertexListMultiComponent previous;
		private VertexListMultiComponent next;

		public VertexListMultiComponent(VertexListMultiComponent previous) {
			this.previous = previous;
			if (previous != null){
				previous.next = this;
			}
		}
		
		public int bottom(){
			return previous == null ? 0 : previous.top();
		}
		
		public int top(){
			if (previous == null){
				return size();
			}
			else {
				return previous.top() + size();
			}
		}

		@Override
		public void add(int index, Vertex v) {
			super.add(index - top(), v);
		}

		@Override
		public Vertex set(int index, Vertex v) {
			return super.set(index - top(), v);
		}

		@Override
		public Vertex remove(int index) {
			return super.remove(index - top());
		}
		
		@Override
		public int indexOf(Object o){
			return super.indexOf(o) + top();
		}
		
		@Override
		public int lastIndexOf(Object o){
			return super.lastIndexOf(o) + top();
		}

		@Override
		public Vertex get(int index) {
			return super.get(index - top());
		}
	}
}
