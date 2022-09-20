package ctrmap.editor.system.workspace;

import xstandard.fs.FSFile;
import ctrmap.util.CMPrefs;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ProjectRegistryData {
	
	private static final int MAX_RECENTS_COUNT = 15;
	private static final String DATA_SEPARATOR = ";";
	
	private Preferences prefs;
	
	public List<ProjectRegistryEntry> entries = new ArrayList<>();
	
	public ProjectRegistryData(String dirNodeName){
		try {
			prefs = CMPrefs.node(dirNodeName);
						
			for (String entryKey : prefs.keys()){
				entries.add(new ProjectRegistryEntry(entryKey, prefs));
			}
			
			sort();
		} catch (BackingStoreException ex) {
			Logger.getLogger(ProjectRegistryData.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	public void putNewEntry(ProjectRegistryEntry e){
		entries.add(e);
		prefs.put(e.projectName, e.getCompiledValue());
		while (entries.size() > MAX_RECENTS_COUNT){
			removeEntry(getOldest());
		}
		sort();
	}
	
	public void updateEntry(ProjectRegistryEntry e){
		if (entries.contains(e)){
			prefs.put(e.projectName, e.getCompiledValue());
			sort();
		}
		else {
			putNewEntry(e);
		}
	}
	
	private void sort(){
		List<ProjectRegistryEntry> sorted = new ArrayList<>();
		for (ProjectRegistryEntry e : entries) {
			int i = 0;
			for (; i < sorted.size(); i++) {
				if (e.lastUsed > sorted.get(i).lastUsed) {
					break;
				}
			}
			sorted.add(i, e);
		}
		entries = sorted;
	}
	
	public void removeEntry(ProjectRegistryEntry e){
		entries.remove(e);
		prefs.remove(e.projectName);
	}
	
	public ProjectRegistryEntry getOldest(){
		ProjectRegistryEntry old = null;
		for (ProjectRegistryEntry e : entries){
			if (old == null){
				old = e;
			}
			else {
				if (e.lastUsed < old.lastUsed){
					old = e;
				}
			}
		}
		return old;
	}
	
	public ProjectRegistryEntry getNewest(){
		ProjectRegistryEntry nyu = null;
		for (ProjectRegistryEntry e : entries){
			if (nyu == null){
				nyu = e;
			}
			else {
				if (e.lastUsed > nyu.lastUsed){
					nyu = e;
				}
			}
		}
		return nyu;
	}
	
	public static class ProjectRegistryEntry{
		public String projectName;
		public long lastUsed;
		public String projectDataPath;
		
		public ProjectRegistryEntry(FSFile prjFile){
			projectName = prjFile.getNameWithoutExtension();
			projectDataPath = prjFile.getPath();
			lastUsed = System.currentTimeMillis();
		}
		
		public ProjectRegistryEntry(String key, Preferences prefs){
			projectName = key;
			String dataStr = prefs.get(key, "0;C:/");
			String[] data = dataStr.split(DATA_SEPARATOR);
			lastUsed = Long.parseLong(data[0]);
			projectDataPath = data[1];
		}
		
		public String getCompiledValue(){
			return String.valueOf(lastUsed) + DATA_SEPARATOR + projectDataPath;
		}
	}
}
