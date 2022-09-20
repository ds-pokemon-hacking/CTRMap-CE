package ctrmap.renderer.scene.metadata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MetaData implements Iterable<MetaDataValue> {

	private List<MetaDataValue> values = new ArrayList<>();

	public MetaData(){
		
	}
	
	public MetaData(MetaData md){
		values.addAll(md.values);
	}
	
	public MetaDataValue putValue(String name, Object value) {
		return putValue(name, value, false);
	}
	
	public MetaDataValue putValue(String name, Object value, boolean uniform) {
		removeValue(name);
		MetaDataValue v = new MetaDataValue(name, value, uniform);
		values.add(v);
		return v;
	}
	
	public void putValue(MetaDataValue val){
		removeValue(val.getName());
		values.add(val);
	}
	
	public void putValues(List<MetaDataValue> vals){
		for (MetaDataValue v : vals){
			putValue(v);
		}
	}

	public MetaDataValue getValue(String name) {
		for (MetaDataValue v : values) {
			if (v.getName().equals(name)) {
				return v;
			}
		}
		return null;
	}
	
	public void removeValue(MetaDataValue val) {
		values.remove(val);
	}

	public void removeValue(String name) {
		MetaDataValue toRemove = getValue(name);
		if (toRemove != null) {
			values.remove(toRemove);
		}
	}

	public boolean hasValue(String name) {
		return getValue(name) != null;
	}

	public List<MetaDataValue> getValues() {
		return values;
	}
	
	public List<MetaDataValue> getWriteableValues() {
		List<MetaDataValue> l = new ArrayList<>();
		for (MetaDataValue v : values) {
			if (!v.getIsTransient()) {
				l.add(v);
			}
		}
		return l;
	}
	
	public boolean isEmpty() {
		return values.isEmpty();
	}
	
	public int getValueCount() {
		return values.size();
	}

	@Override
	public Iterator<MetaDataValue> iterator() {
		return values.iterator();
	}
}
