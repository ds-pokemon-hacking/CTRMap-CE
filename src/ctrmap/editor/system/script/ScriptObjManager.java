package ctrmap.editor.system.script;

import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.formats.pokemon.IScriptObject;
import ctrmap.pokescript.LangConstants;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlListElement;
import xstandard.formats.yaml.YamlNode;
import xstandard.formats.yaml.YamlNodeName;
import xstandard.formats.yaml.YamlReflectUtil;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.text.FormattingUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 */
public class ScriptObjManager {

	public static final String SCROBJMNG_FILE_EXT = ".sdef2";

	public static final String SCROBJMNG_FILE_ZONE_ID_KEY = "ZoneID";
	public static final String SCROBJMNG_FILE_OBJBINDS_KEY = "ObjectBindings";
	
	public static final String SCRIDDEF_CLASS_NAME = "SCRID";
	
	public static final int EXTRA_SCRID_BIT = (1 << 30);
	public static final int EXTRA_SCRID_TYPEID = -1;

	private FSFile dataDir;
	public final FSFile headersDir;
	private List<ScriptObjLookup> zoneSCRIDMap = new ArrayList<>();

	private IScriptObjUniqueIDAssigner uidAssigner;

	public ScriptObjManager(CTRMapProject project) {
		dataDir = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_MNG);
		headersDir = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_INCLUDE);

		for (FSFile sub : dataDir.listFiles()) {
			if (sub.getName().endsWith(SCROBJMNG_FILE_EXT)) {
				Yaml yml = new Yaml(sub);

				int zoneID = yml.getRootNodeKeyValueInt(SCROBJMNG_FILE_ZONE_ID_KEY);

				ScriptObjLookup target = new ScriptObjLookup(zoneID);

				for (YamlNode ch : yml.getEnsureRootNodeKeyNode(SCROBJMNG_FILE_OBJBINDS_KEY).children) {
					target.add(YamlReflectUtil.deserialize(ch, ScriptObjBinding.class));
				}

				zoneSCRIDMap.add(target);
			}
		}
	}

	public void saveData(int zoneID) {
		if (getMapForZoneIfExist(zoneID) != null) {
			List<ScriptObjBinding> map = getMapForZone(zoneID);
			File tgt = new File(dataDir + "/Zone" + zoneID + SCROBJMNG_FILE_EXT);
			if (!map.isEmpty() || tgt.exists()) {
				Yaml yml = new Yaml();

				yml.getEnsureRootNodeKeyNode(SCROBJMNG_FILE_ZONE_ID_KEY).setValueInt(zoneID);

				YamlNode bindings = yml.getEnsureRootNodeKeyNode(SCROBJMNG_FILE_OBJBINDS_KEY);

				for (ScriptObjBinding b : map) {
					YamlNode childRoot = new YamlNode(new YamlListElement());
					YamlReflectUtil.addFieldsToNode(childRoot, b);
					bindings.addChild(childRoot);
				}

				yml.writeToFile(new DiskFile(tgt));
			}
			
			generateHeaders(zoneID);
		}
	}

	public void setUIDAssigner(IScriptObjUniqueIDAssigner assigner) {
		this.uidAssigner = assigner;
	}

	public int getSCRIDMax(int zoneId) {
		ScriptObjLookup map = getMapForZone(zoneId);
		int max = 0;
		for (ScriptObjBinding b : map) {
			int i = b.objectSCRID;
			i++;
			if (i > max) {
				max = i;
			}
		}
		return max;
	}

	public ScriptObjLookup getMapForZoneIfExist(int zoneID) {
		for (ScriptObjLookup l : zoneSCRIDMap) {
			if (l.zoneId == zoneID) {
				return l;
			}
		}
		return null;
	}

	public ScriptObjLookup getMapForZone(int zoneID) {
		ScriptObjLookup l = getMapForZoneIfExist(zoneID);
		if (l == null) {
			l = new ScriptObjLookup(zoneID);
			zoneSCRIDMap.add(l);
		}
		return l;
	}

	public boolean hasNameAtOtherObj(int zoneID, String name, IScriptObject thisObj) {
		ScriptObjBinding b = getMapForZone(zoneID).findObjByName(name);
		if (b == null) {
			return false;
		}
		return b.objectUID != uidAssigner.getUniqueID(thisObj);
	}
	
	public boolean hasNameAtOtherBnd(int zoneID, String name, ScriptObjBinding bnd) {
		ScriptObjBinding b = getMapForZone(zoneID).findObjByName(name);
		if (b == null) {
			return false;
		}
		return b.objectUID != bnd.objectUID;
	}

	public void setScrObjID(int zoneID, IScriptObject obj, String name) {
		int SCRID = obj.getSCRID();
		if (SCRID >= 2000) {
			return;
		}
		if (name == null) {
			getMapForZone(zoneID).removeByUID(uidAssigner.getUniqueID(obj));
		} else {
			//This shouldn't be called here, since it breaks setting script IDs on actors whose names haven't changed
			/*if (name.equals(getNameForScriptObj(zoneID, obj))) {
				return;
			}*/
			getMapForZone(zoneID).updateByAllCreate(uidAssigner.getUniqueID(obj), obj.getSCRID(), name);
		}

		saveData(zoneID);
	}

	public String getNameForScriptObj(int zoneId, IScriptObject obj) {
		if (obj.getObjectTypeID() == EXTRA_SCRID_TYPEID) {
			return ((ExtraScrObj)obj).binding.objectName;
		}
		int uid = uidAssigner.getUniqueID(obj);
		if (uid == -1) {
			throw new IllegalArgumentException("Could not resolve UID for object " + obj + "!");
		}
		ScriptObjBinding b = getMapForZone(zoneId).findObjByUID(uid);
		return b == null ? null : b.objectName;
	}

	public String getAnyNameForScriptObj(int zoneId, IScriptObject obj) {
		String n = getNameForScriptObj(zoneId, obj);
		if (n == null) {
			n = "SCRID_" + obj.getSCRID();
		}
		return n;
	}

	public FSFile getZoneIncludeDir(int zoneID) {
		FSFile targetDir = headersDir.getChild("zones").getChild(String.valueOf(zoneID));
		targetDir.mkdirs();
		return targetDir;
	}

	public void generateHeaders(int zoneID) {
		if (getMapForZoneIfExist(zoneID) != null) {
			ScriptObjLookup l = getMapForZone(zoneID);
			FSFile dir = getZoneIncludeDir(zoneID);
			FSFile hdrFile = dir.getChild(SCRIDDEF_CLASS_NAME + LangConstants.LANG_GENERAL_HEADER_EXTENSION);
			if (!l.isEmpty() || hdrFile.exists()) {
				generateHeader(l, zoneID, hdrFile);
			}
		}
	}

	public static void generateHeader(ScriptObjLookup map, int zoneID, FSFile tgt) {
		IncludePrintStream out = new IncludePrintStream(tgt.getNativeOutputStream());

		out.setPackage("zones." + zoneID);
		
		out.printClassBrief("SCRID definition for zone " + zoneID);
		out.beginClass("SCRID");
		
		for (ScriptObjBinding b : map) {
			out.printConstantInt(b.objectName, b.objectSCRID);
		}
		
		out.endClass();

		out.close();
	}

	public static class ScriptObjLookup extends ArrayList<ScriptObjBinding> {

		public int zoneId;

		public ScriptObjLookup(int zoneId) {
			this.zoneId = zoneId;
		}
		
		public ExtraScrObj addExtraScriptObject(int scrId) {
			int scrObjCount = 0;
			for (ScriptObjBinding b : this) {
				if ((b.objectUID & EXTRA_SCRID_BIT) != 0) {
					scrObjCount++;
				}
			}
			ScriptObjBinding b = updateByAllCreate(scrObjCount | EXTRA_SCRID_BIT, scrId, "EX_SCROBJ_" + FormattingUtils.getIntWithLeadingZeros(4, scrObjCount));
			return new ExtraScrObj(b);
		}
		
		public List<ExtraScrObj> getExtraScriptObjects() {
			List<ExtraScrObj> l = new ArrayList<>();
			for (ScriptObjBinding b : this) {
				if ((b.objectUID & EXTRA_SCRID_BIT) != 0) {
					l.add(new ExtraScrObj(b));
				}
			}
			l.sort((ExtraScrObj o1, ExtraScrObj o2) -> (o1.binding.objectUID & 0xFFFF) - (o2.binding.objectUID & 0xFFFF));
			return l;
		}

		public void removeByUID(int uid) {
			for (ScriptObjBinding b : this) {
				if (b.objectUID == uid) {
					remove(b);
					return;
				}
			}
		}

		public ScriptObjBinding findObjByUID(int uid) {
			for (ScriptObjBinding b : this) {
				if (b.objectUID == uid) {
					return b;
				}
			}
			return null;
		}

		public ScriptObjBinding findObjByName(String name) {
			for (ScriptObjBinding b : this) {
				if (Objects.equals(b.objectName, name)) {
					return b;
				}
			}
			return null;
		}

		public ScriptObjBinding updateByAllCreate(int uid, int SCRID, String name) {
			ScriptObjBinding b = findObjByUID(uid);
			if (b == null) {
				b = new ScriptObjBinding();
				b.objectUID = uid;
				add(b);
			}
			b.objectSCRID = SCRID;
			b.objectName = name;
			return b;
		}
	}

	public static class ScriptObjBinding {

		@YamlNodeName("ObjectUID")
		public int objectUID;
		@YamlNodeName("ObjectName")
		public String objectName;
		@YamlNodeName("ObjectSCRID")
		public int objectSCRID;

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj != null && obj instanceof ScriptObjBinding) {
				final ScriptObjBinding other = (ScriptObjBinding) obj;
				return objectUID == other.objectUID;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 53 * hash + this.objectUID;
			return hash;
		}

	}
}
