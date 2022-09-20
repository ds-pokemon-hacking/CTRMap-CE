package ctrmap.editor.system.workspace.backup;

import xstandard.formats.yaml.YamlListElement;
import xstandard.formats.yaml.YamlNode;
import java.util.ArrayList;
import java.util.List;

public class ProjectBackupConfig {

	private YamlNode node;

	public long backupPeriod = -1;
	public long lastBackupTimestamp = -1;

	public List<BackupInfo> backupInfos = new ArrayList<>();

	public ProjectBackupConfig(YamlNode node) {
		this.node = node;
		if (node.hasChildren("BackupPeriodMillis")) {
			backupPeriod = node.getChildLongValue("BackupPeriodMillis");
		}
		if (backupPeriod > 0 && backupPeriod < 5 * 60 * 1000) {
			backupPeriod = 0; //someone played with the config. Let's give them 5 minutes, but no less than that.
		}
		if (node.hasChildren("LastBackupMillis")) {
			lastBackupTimestamp = node.getChildLongValue("LastBackupMillis");
		}
		else {
			lastBackupTimestamp = 0;
		}
		YamlNode backups = node.getChildByName("Backups");
		if (backups != null) {
			for (YamlNode child : backups.children) {
				backupInfos.add(new BackupInfo(child));
			}
		}
	}
	
	public BackupInfo initNewBackupInfo(String name){
		BackupInfo i = new BackupInfo(name);
		backupInfos.add(i);
		return i;
	}
	
	public void sortBackupInfosDateDescending(){
		List<BackupInfo> sorted = new ArrayList<>();
		for (BackupInfo e : backupInfos) {
			int i = 0;
			for (; i < sorted.size(); i++) {
				if (e.timestamp > sorted.get(i).timestamp) {
					break;
				}
			}
			sorted.add(i, e);
		}
		backupInfos.clear();
		backupInfos.addAll(sorted);
	}

	public void saveToNode() {
		node.getEnsureChildByName("BackupPeriodMillis").setValueLong(backupPeriod);
		node.getEnsureChildByName("LastBackupMillis").setValueLong(lastBackupTimestamp);
		YamlNode backups = node.getEnsureChildByName("Backups");
		backups.removeAllChildren();
		for (BackupInfo i : backupInfos){
			backups.addChild(i.createNode());
		}
	}
	
	public void setNowLastBackupTimestamp(long playtime){
		lastBackupTimestamp = playtime;
	}

	public static class BackupInfo {

		public String path;
		public long timestamp;
		public boolean automatic;

		public BackupInfo(YamlNode node) {
			path = node.getChildValue("DirectoryPath");
			timestamp = node.getChildLongValue("Timestamp");
			automatic = node.getChildBoolValue("IsAutoBackup");
		}

		public BackupInfo(String pathName) {
			path = pathName;
			timestamp = System.currentTimeMillis();
		}
		
		public YamlNode createNode(){
			YamlNode n = new YamlNode(new YamlListElement());
			n.getEnsureChildByName("DirectoryPath").setValue(path);
			n.getEnsureChildByName("Timestamp").setValueLong(timestamp);
			n.getEnsureChildByName("IsAutoBackup").setValueBool(automatic);
			return n;
		}
		
		public long getAge() {
			return System.currentTimeMillis() - timestamp;
		}
	}
}
