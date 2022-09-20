package ctrmap.renderer.scenegraph;

import xstandard.util.ListenableList;
import java.util.ArrayList;
import java.util.Collection;

public class G3DResInstanceList<T extends G3DResourceInstance> extends ListenableList<T> implements Iterable<T>, Collection<T> {

	private G3DResourceInstance parent = null;

	public G3DResInstanceList(G3DResourceInstance parent) {
		setParent(parent);
	}

	public void setParent(G3DResourceInstance parent) {
		this.parent = parent;
		for (G3DResourceInstance ch : this) {
			ch.parent = parent;
		}
	}

	@Override
	public boolean add(T o) {
		if (contains(o)) {
			return false;
		}
		o.parent = parent;
		return super.add(o);
	}

	@Override
	public void add(int index, T o) {
		if (contains(o)) {
			return;
		}
		o.parent = parent;
		super.add(index, o);
	}

	@Override
	public boolean addAll(Collection<? extends T> o) {
		Collection<T> clean = new ArrayList<>();
		for (T e : o) {
			if (!contains(e)) {
				e.parent = parent;
				clean.add(e);
			}
		}
		boolean rsl = super.addAll(clean);
		for (T t : clean) {
			fireAddEvent(t);
		}
		return rsl;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> o) {
		Collection<T> clean = new ArrayList<>();
		for (T e : o) {
			if (!contains(e)) {
				e.parent = parent;
				clean.add(e);
			}
		}
		boolean rsl = super.addAll(index, clean);
		for (T t : clean) {
			fireAddEvent(t);
		}
		return rsl;
	}

	@Override
	public T set(int idx, T o) {
		if (contains(o)) {
			return null;
		}
		o.parent = parent;
		T r = super.set(idx, o);
		return r;
	}
	
	@Override
	public void clear() {
		for (G3DResourceInstance ch : this) {
			ch.parent = null;
		}
		super.clear();
	}
	
	@Override
	public T remove(int index) {
		T result = super.remove(index);
		result.parent = null;
		return result;
	}
	
	@Override
	public void setModify(int idx, T o) {
		super.setModify(idx, o);
		o.parent = parent;
	}

	@Override
	public boolean remove(Object elem) {
		boolean rsl = super.remove(elem);
		if (elem instanceof G3DResourceInstance) {
			((G3DResourceInstance) elem).parent = null;
		}
		return rsl;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		for (Object o : c) {
			if (o instanceof G3DResourceInstance) {
				((G3DResourceInstance) o).parent = null;
			}
		}
		return super.removeAll(c);
	}
}
