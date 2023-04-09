package ctrmap.renderer.scene.model.draw.vtxlist;

import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.model.VertexAttributeType;
import ctrmap.renderer.scene.model.VertexMorph;
import java.util.Iterator;
import xstandard.util.ListenableList;

public class MorphableVertexList extends AbstractVertexList {

	private final ListenableList<VertexMorph> vertexMorphs = new ListenableList<>();

	private VertexMorph lastMorph;
	private VertexMorph currentMorph;
	private float blendWeight;

	public ListenableList<VertexMorph> morphs() {
		return vertexMorphs;
	}
	
	public void addMorph(VertexMorph m) {
		vertexMorphs.add(m);
	}

	public void setMorph(VertexMorph m) {
		lastMorph = m;
		currentMorph = m;
		blendWeight = 1f;
	}
	
	public VertexMorph currentMorph() {
		return currentMorph;
	}
	
	public VertexMorph lastMorph() {
		return lastMorph;
	}
	
	public int getMorphCount() {
		return vertexMorphs.size();
	}
	
	public void removeMorph(VertexMorph morph) {
		vertexMorphs.remove(morph);
		if (lastMorph == morph) {
			lastMorph = null;
		}
		if (currentMorph == morph) {
			currentMorph = null;
		}
	}

	public void setMorph(VertexMorph l, VertexMorph r, float weight) {
		lastMorph = l;
		currentMorph = r;
		blendWeight = weight;
	}
	
	public int morphIndex(VertexMorph morph) {
		return vertexMorphs.indexOf(morph);
	}
	
	public float getMorphWeight() {
		return blendWeight;
	}
	
	@Override
	public int sizeForAttribute(VertexAttributeType type, int setNo) {
		int size = 0;
		for (VertexMorph m : vertexMorphs) {
			if (!m.vertices.isEmpty()) {
				if (!m.vertices.get(0).hasAttribute(type, setNo)) {
					break;
				}
			}
			size += m.vertices.size();
		}
		return size;
	}

	@Override
	public void add(int index, Vertex v) {
		for (VertexMorph m : vertexMorphs) {
			m.vertices.add(v);
		}
	}

	@Override
	public Vertex set(int index, Vertex v) {
		Vertex ret = null;
		for (VertexMorph m : vertexMorphs) {
			Vertex r = m.vertices.set(index, v);
			if (m == currentMorph) {
				ret = r;
			}
		}
		return ret;
	}

	@Override
	public Vertex remove(int index) {
		Vertex ret = null;
		for (VertexMorph m : vertexMorphs) {
			Vertex v = m.vertices.remove(index);
			if (m == currentMorph) {
				ret = v;
			}
		}
		return ret;
	}

	@Override
	public VertexListType getType() {
		return VertexListType.MORPH;
	}

	@Override
	public Vertex get(int index) {
		int size = 0;
		for (VertexMorph m : vertexMorphs) {
			if (index - size < m.vertices.size()) {
				return m.vertices.get(index - size);
			}
			size += m.vertices.size();
		}
		throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public int size() {
		int size = 0;
		for (VertexMorph m : vertexMorphs) {
			size += m.vertices.size();
		}
		return size;
	}

	@Override
	public Iterator<Vertex> iterator() {
		return new Iterator<Vertex>() {

			final Iterator<VertexMorph> morphIt = vertexMorphs.iterator();
			Iterator<Vertex> vtxIt = null;

			@Override
			public boolean hasNext() {
				while (vtxIt == null || !vtxIt.hasNext()) {
					if (morphIt.hasNext()) {
						vtxIt = morphIt.next().vertices.iterator();
					} else {
						return false;
					}
				}
				return vtxIt.hasNext();
			}

			@Override
			public Vertex next() {
				return vtxIt.next();
			}
		};
	}
}
