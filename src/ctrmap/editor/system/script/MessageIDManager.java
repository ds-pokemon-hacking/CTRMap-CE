package ctrmap.editor.system.script;

import ctrmap.editor.system.workspace.CTRMapProject;
import ctrmap.editor.system.workspace.UserData;
import ctrmap.editor.gui.editors.text.alias.ITextAliasManager;
import ctrmap.editor.gui.editors.text.loaders.ITextArcType;
import ctrmap.pokescript.LangConstants;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlListElement;
import xstandard.formats.yaml.YamlNode;
import xstandard.fs.FSFile;
import xstandard.text.FormattingUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MessageIDManager implements ITextAliasManager {

	public static final String MSG_PACKAGE_NAME = "messages";
	public static final String MSG_CLASS_NAME = "MSGID";
	public static final String SCRMSG_PACKAGE_NAME = "script";
	public static final String SYSMSG_PACKAGE_NAME = "system";

	private static final String MSGFILE_NODE_PREFIX = "MSGFILE_";

	private static final String MSGID_NODE_KEY_MSGID = "MsgID";
	private static final String MSGID_NODE_KEY_ALIAS = "Alias";
	private static final String MSGID_NODE_KEY_CONTENT = "Content";

	private final FSFile includeRoot;

	private final FSFile ymlRoot;
	private final Map<String, Yaml> ymls = new HashMap<>();

	private boolean savedataEnable = true;

	public MessageIDManager(CTRMapProject project) {
		includeRoot = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_INCLUDE).getChild("messages");
		ymlRoot = project.userData.getUserDataDir(UserData.UsrDirectory.SCRIPT_MNG).getChild("messages");
		ymlRoot.mkdirs();
	}
	
	private Yaml getYmlForArcType(ITextArcType arc) {
		Yaml yml = ymls.get(arc.friendlyName());
		if (yml == null) {
			yml = new Yaml(ymlRoot.getChild(arc.friendlyName() + ".yml"));
			ymls.put(arc.friendlyName(), yml);
		}
		return yml;
	}

	@Override
	public String getMsgidAlias(MessageTag tag) {
		YamlNode msgNode = findNodeByTag(tag, false);
		if (msgNode != null) {
			return msgNode.getChildValue(MSGID_NODE_KEY_ALIAS);
		}
		return null;
	}

	private YamlNode findNodeByTag(MessageTag tag, boolean createIfAbsent) {
		Yaml yml = getYmlForArcType(tag.arc);
		String fileNodeName = MSGFILE_NODE_PREFIX + FormattingUtils.getIntWithLeadingZeros(4, tag.fileNo);
		YamlNode fileNode = yml.getRootNodeKeyNode(fileNodeName);
		if (fileNode == null) {
			if (createIfAbsent) {
				fileNode = yml.root.addChildKey(fileNodeName);
			}
			else {
				return null;
			}
		}
		for (YamlNode ch : fileNode.children) {
			if (ch.getChildIntValue(MSGID_NODE_KEY_MSGID) == tag.msgId) {
				return ch;
			}
		}
		if (createIfAbsent) {
			YamlNode listElemNode = new YamlNode(new YamlListElement());
			YamlNode msgidNode = new YamlNode(MSGID_NODE_KEY_MSGID, tag.msgId);
			listElemNode.addChild(msgidNode);
			fileNode.addChild(listElemNode);
			return listElemNode;
		}
		return null;
	}

	@Override
	public void setMsgidAlias(MessageTag tag, String alias) {
		YamlNode currentValue = findNodeByTag(tag, alias != null);
		if (currentValue != null && !Objects.equals(currentValue.getValue(), alias)) {
			if (alias == null) {
				currentValue.parent.removeChild(currentValue);
			} else {
				currentValue.getEnsureChildByName(MSGID_NODE_KEY_ALIAS).setValue(alias);
			}
			if (savedataEnable) {
				getYmlForArcType(tag.arc).write();
				updateIncludeByTag(tag);
			}
		}
	}

	public void updateIncludeByTag(MessageTag tag) {
		String msgSubpkgName = "Msg" + FormattingUtils.getIntWithLeadingZeros(4, tag.fileNo);
		FSFile destDir = includeRoot.getChild(tag.arc.getManagerPackageName()).getChild(msgSubpkgName);
		destDir.mkdirs();
		FSFile destFile = destDir.getChild(MSG_CLASS_NAME + LangConstants.LANG_GENERAL_HEADER_EXTENSION);

		IncludePrintStream out = new IncludePrintStream(destFile.getNativeOutputStream());

		out.setPackage(MSG_PACKAGE_NAME + "." + tag.arc.getManagerPackageName() + "." + msgSubpkgName);

		out.printClassBrief("Message file " + tag.fileNo + " definitions");
		out.beginClass(MSG_CLASS_NAME);

		Yaml yml = getYmlForArcType(tag.arc);
		YamlNode fileNode = yml.getRootNodeKeyNode(MSGFILE_NODE_PREFIX + FormattingUtils.getIntWithLeadingZeros(4, tag.fileNo));
		if (fileNode != null) {
			for (YamlNode msgNode : fileNode.children) {
				int msgId = msgNode.getChildIntValue(MSGID_NODE_KEY_MSGID);
				if (msgId != -1) {
					String value = msgNode.getChildValue(MSGID_NODE_KEY_ALIAS);
					if (value != null) {
						String content = msgNode.getChildValue(MSGID_NODE_KEY_CONTENT);
						if (content != null) {
							out.printDoxygenComment(content.replace("\\n", "\n"));
						}
						out.printConstantInt(value, msgId);
					}
				}
			}
		}

		out.endClass();

		out.close();
	}

	@Override
	public void setSavedataEnable(boolean value) {
		savedataEnable = value;
	}

	@Override
	public void saveDataManual(ITextArcType arcType, int fileNo) {
		Yaml yml = getYmlForArcType(arcType);
		yml.write();
		updateIncludeByTag(new MessageTag(arcType, fileNo, -1));
	}

	@Override
	public void setMsgidAliasContent(MessageTag tag, String content) {
		YamlNode node = findNodeByTag(tag, false);
		if (node != null) {
			if (content == null) {
				node.removeChildByName(MSGID_NODE_KEY_CONTENT);
			} else {
				node.getEnsureChildByName(MSGID_NODE_KEY_CONTENT).setValue(content);
			}
			if (savedataEnable) {
				getYmlForArcType(tag.arc).write();
				updateIncludeByTag(tag);
			}
		}
	}
}
