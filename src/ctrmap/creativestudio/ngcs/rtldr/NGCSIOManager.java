package ctrmap.creativestudio.ngcs.rtldr;

import ctrmap.creativestudio.ngcs.io.CSG3DIOContentType;
import ctrmap.creativestudio.ngcs.io.IG3DFormatHandler;
import xstandard.gui.file.ExtensionFilter;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NGCSIOManager {

	private static NGCSIOManager INSTANCE;
	
	private Map<CSG3DIOContentType, List<IG3DFormatHandler>> formatHandlers = new HashMap<>();
	
	private NGCSIOManager() {
		for (CSG3DIOContentType t : CSG3DIOContentType.values()) {
			formatHandlers.put(t, new ArrayList<>());
		}
	}
	
	public static NGCSIOManager getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NGCSIOManager();
		}
		return INSTANCE;
	}

	public void registHandler(IG3DFormatHandler h, CSG3DIOContentType... types) {
		if (h != null && types != null) {
			for (CSG3DIOContentType t : types) {
				if (t != null) {
					ArraysEx.addIfNotNullOrContains(formatHandlers.get(t), h);
				}
			}
		}
	}
	
	public void unregistHandler(IG3DFormatHandler h) {
		if (h != null) {
			for (List<IG3DFormatHandler> l : formatHandlers.values()) {
				l.remove(h);
			}
		}
	}

	public IG3DFormatHandler[] getAllFormatHandlers() {
		List<IG3DFormatHandler> out = new ArrayList<>();
		List<ExtensionFilter> efList = new ArrayList<>();

		for (CSG3DIOContentType t : CSG3DIOContentType.values()) {
			if (t != CSG3DIOContentType.UNKNOWN) {
				//in order
				List<IG3DFormatHandler> l = formatHandlers.get(t);
				for (IG3DFormatHandler h : l) {
					if (!out.contains(h)) {
						ExtensionFilter filter = h.getExtensionFilter();
						if (!efList.contains(filter)) {
							out.add(h);
							efList.add(filter);
						}
					}
				}
			}
		}
		return out.toArray(new IG3DFormatHandler[out.size()]);
	}

	public IG3DFormatHandler[] getFormatHandlers(CSG3DIOContentType type) {
		if (type == null) {
			return getAllFormatHandlers();
		} else {
			if (type == CSG3DIOContentType.ANIMATION_ANY_RESERVED) {
				List<IG3DFormatHandler> l = new ArrayList<>();
				l.addAll(formatHandlers.get(CSG3DIOContentType.ANIMATION_MULTI_EX));
				l.addAll(formatHandlers.get(CSG3DIOContentType.ANIMATION_SKL));
				l.addAll(formatHandlers.get(CSG3DIOContentType.ANIMATION_MAT));
				l.addAll(formatHandlers.get(CSG3DIOContentType.ANIMATION_CAM));
				l.addAll(formatHandlers.get(CSG3DIOContentType.ANIMATION_VIS));
				return l.toArray(new IG3DFormatHandler[l.size()]);
			}
			return formatHandlers.get(type).toArray(new IG3DFormatHandler[0]);
		}
	}
}
