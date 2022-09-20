package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAE;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DAEDict<T extends DAEIDAble> implements Iterable<T>{
	public List<T> content = new ArrayList<>();
	
	public void putNode(T n){
		content.add(n);
	}
	
	public T get(String id){
		for (T c : content){
			if (id.equals(c.getID())){
				return c;
			}
		}
		return null;
	}
	
	public T getByUrl(String url){
		return get(DAE.idFromUrl(url));
	}

	@Override
	public Iterator<T> iterator() {
		return content.iterator();
	}
}
