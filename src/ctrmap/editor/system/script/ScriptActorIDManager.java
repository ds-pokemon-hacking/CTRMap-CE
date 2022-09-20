package ctrmap.editor.system.script;

import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.pokescript.LangConstants;
import xstandard.formats.msgtxt.MsgTxt;
import xstandard.fs.FSFile;
import xstandard.text.FormattingUtils;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ScriptActorIDManager {

	public static final String ENTITYMNG_FILE_EXT = ".edef";

	public static final String ENTITYMNG_FILE_ZONE_ID_KEY = "ZoneID";
	
	public static final String ACTORDEF_CLASSNAME = "Actors";

	private FSFile dataDir;
	private FSFile headersDir;
	private Map<Integer, Map<Integer, String>> zoneSCRIDMap = new HashMap<>();

	public ScriptActorIDManager(CTRMapProject project) {
		dataDir = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_MNG);
		headersDir = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_INCLUDE);

		for (FSFile sub : dataDir.listFiles()) {
			if (sub.getName().endsWith(ENTITYMNG_FILE_EXT)) {
				MsgTxt msgtxt = new MsgTxt(sub);

				int zoneID = Integer.parseInt(msgtxt.getLineForName(ENTITYMNG_FILE_ZONE_ID_KEY));

				Map<Integer, String> target = new HashMap<>();

				for (Map.Entry<String, String> e : msgtxt.getMap().entrySet()) {
					String key = e.getKey();
					if (!key.equals(ENTITYMNG_FILE_ZONE_ID_KEY)) {
						target.put(Integer.parseInt(key), e.getValue());
					}
				}

				zoneSCRIDMap.put(zoneID, target);
			}
		}
	}

	public void saveData(int zoneID) {
		if (zoneSCRIDMap.containsKey(zoneID)) {
			Map<Integer, String> map = zoneSCRIDMap.get(zoneID);
			File tgt = new File(dataDir + "/Zone" + zoneID + ENTITYMNG_FILE_EXT);
			if (!map.isEmpty() || tgt.exists()) {

				MsgTxt msgtxt = new MsgTxt();

				msgtxt.putLine(ENTITYMNG_FILE_ZONE_ID_KEY, String.valueOf(zoneID));

				for (Map.Entry<Integer, String> scre : map.entrySet()) {
					msgtxt.putLine(String.valueOf(scre.getKey()), scre.getValue());
				}

				msgtxt.writeToFile(tgt);
			}
		}
	}

	public Map<Integer, String> getMapForZone(int zoneID) {
		Map<Integer, String> map = zoneSCRIDMap.get(zoneID);
		if (map == null) {
			map = new HashMap<>();
			zoneSCRIDMap.put(zoneID, map);
		}
		return map;
	}

	public static String getUniqEntityName(int OBJID, String name) {
		return "OBJ" + FormattingUtils.getIntWithLeadingZeros(3, OBJID) + "_" + name;
	}

	public void setObjID(int zoneID, int OBJID, String name) {
		if (OBJID >= 255) {
			return;
		}
		if (name == null) {
			getMapForZone(zoneID).remove(OBJID);
		} else {
			if (name.equals(getNameForOBJID(zoneID, OBJID))) {
				return;
			}
			getMapForZone(zoneID).put(OBJID, name);
		}
		saveData(zoneID);
		generateHeaders(zoneID);
	}

	public String getNameForOBJID(int zoneId, int SCRID) {
		return getMapForZone(zoneId).get(SCRID);
	}

	public FSFile getZoneIncludeDir(int zoneID) {
		FSFile targetDir = headersDir.getChild("zones").getChild(String.valueOf(zoneID));
		targetDir.mkdirs();
		return targetDir;
	}

	public void generateHeaders(int zoneID) {
		if (zoneSCRIDMap.containsKey(zoneID)) {
			Map<Integer, String> map = zoneSCRIDMap.get(zoneID);
			FSFile dir = getZoneIncludeDir(zoneID);
			FSFile hdrFile = dir.getChild(ACTORDEF_CLASSNAME + LangConstants.LANG_GENERAL_HEADER_EXTENSION);
			if (!map.isEmpty() || hdrFile.exists()) {
				generateHeader(map, zoneID, hdrFile);
			}
		}
	}

	public static void generateHeader(Map<Integer, String> map, int zoneID, FSFile tgt) {
		IncludePrintStream out = new IncludePrintStream(tgt.getNativeOutputStream());
		
		out.setPackage("zones." + zoneID);
		
		out.printClassBrief("Actor ID definition for zone " + zoneID);
		out.beginClass("Actors");

		for (Map.Entry<Integer, String> e : map.entrySet()) {
			out.printConstantInt(getUniqEntityName(e.getKey(), e.getValue()), e.getKey());
		}
		
		out.endClass();

		out.close();
	}
}
