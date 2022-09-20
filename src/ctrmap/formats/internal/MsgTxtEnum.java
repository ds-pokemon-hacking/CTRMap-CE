package ctrmap.formats.internal;

import ctrmap.CTRMapResources;
import xstandard.formats.msgtxt.MsgTxt;
import ctrmap.formats.common.GameInfo;
import java.util.Map;

public class MsgTxtEnum extends MsgTxt {

	private int max = -1;
	
	public MsgTxtEnum(String enumName) {
		this(enumName, null);
	}
	
	public MsgTxtEnum(String enumName, GameInfo game) {
		super(CTRMapResources.ACCESSOR.getStream("message/enumeration/" + enumName + (game == null ? "" : game.isOA() ? "_OA" : "_XY") + ".msgtxt"));
		for (String key : entries.keySet()){
			int v = Integer.parseInt(key);
			max = Math.max(max, v);
		}
		max++;
	}

	public String getName(int enumOrdinal) {
		String key = String.valueOf(enumOrdinal);
		if (entries.containsKey(key)) {
			return entries.get(key);
		}
		return key;
	}

	public int getOrdinal(String enumName) {
		for (Map.Entry<String, String> e : entries.entrySet()) {
			if (e.getValue().equals(enumName)){
				return Integer.parseInt(e.getKey());
			}
		}
		return -1;
	}
	
	public int getOrdinalMax(){
		return max;
	}
	
	public String[] getSortedValues(){
		String[] arr = new String[getOrdinalMax()];
		for (int i = 0; i < arr.length; i++){
			arr[i] = getName(i);
		}
		return arr;
	}
}
