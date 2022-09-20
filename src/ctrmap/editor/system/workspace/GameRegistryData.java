package ctrmap.editor.system.workspace;

import ctrmap.util.CMPrefs;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class GameRegistryData {
		
	private Preferences prefs;
	
	public final Map<String, String> entries = new LinkedHashMap<>();
	
	public GameRegistryData(String dirNodeName){
		try {
			prefs = CMPrefs.node(dirNodeName);
			
			for (String entryKey : prefs.keys()){
				entries.put(entryKey, prefs.get(entryKey, null));
			}
		} catch (BackingStoreException ex) {
			Logger.getLogger(GameRegistryData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void putGamePath(String path){
		path = path.replace('\\', '/');
		String gameName = "game" + UUID.randomUUID();
		entries.put(gameName, path);
		prefs.put(gameName, path);
	}
	
	public void removeEntry(String path){
		for (Map.Entry<String, String> e : entries.entrySet()){
			if (e.getValue().equals(path)){
				entries.remove(e.getKey());
				prefs.remove(e.getKey());
				break;
			}
		}
	}
}
