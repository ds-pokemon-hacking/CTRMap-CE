package ctrmap.creativestudio.ngcs;

import ctrmap.formats.generic.interchange.CMIFFile;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.util.ListenableList;
import java.util.HashMap;
import java.util.Map;

/*
Used to synchronize CMIF OtherFiles with G3DResource MetaData.
 */
public class NGCSOtherSyncListener implements ListenableList.ElementChangeListener {

	private final G3DResource res;

	private Map<CMIFFile.OtherFile, MetaDataValue> valMap = new HashMap<>();
	private Map<Integer, CMIFFile.OtherFile> idxMap = new HashMap<>();

	public NGCSOtherSyncListener(G3DResource resParent) {
		this.res = resParent;
	}

	private void shiftIndicesSince(int starting, int addend) {
		Map<Integer, CMIFFile.OtherFile> newMap = new HashMap<>();
		
		for (Map.Entry<Integer, CMIFFile.OtherFile> e : idxMap.entrySet()) {
			int key = e.getKey();
			CMIFFile.OtherFile value = e.getValue();
			if (key >= starting) {
				newMap.put(key + addend, value);
			}
			else {
				newMap.put(key, value);
			}
		}
		
		idxMap.clear();
		idxMap.putAll(newMap);
	}
	
	@Override
	public void onEntityChange(ListenableList.ElementChangeEvent evt) {
		CMIFFile.OtherFile otf = (CMIFFile.OtherFile) evt.element;

		switch (evt.type) {
			case ADD:
				valMap.put(otf, res.metaData.putValue(otf.name, otf.data));
				shiftIndicesSince(evt.index + 1, 1);
				break;
			case REMOVE:
				res.metaData.removeValue(valMap.remove(otf));
				shiftIndicesSince(evt.index, -1);
				break;
			case MODIFY:
				MetaDataValue val = valMap.get(otf);
				if (val != null) {
					val.setName(otf.name);
					val.setValue(otf.data);
				}
				else {
					CMIFFile.OtherFile oldEntry = idxMap.get(evt.index);
					if (oldEntry != null) {
						MetaDataValue oldValue = valMap.get(oldEntry);
						if (oldValue != null) {
							res.metaData.removeValue(val);
						}
					}
					addMapEntry(otf);
				}
				break;
		}
		
		idxMap.put(evt.index, otf);
	}
	
	private void addMapEntry(CMIFFile.OtherFile otf) {
		valMap.put(otf, res.metaData.putValue(otf.name, otf.data));
	}
}
