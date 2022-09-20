package ctrmap.formats.generic.interchange;

import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scenegraph.G3DResourceType;
import ctrmap.renderer.scenegraph.G3DSceneTemplate;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.ExtensionFilter;
import xstandard.io.InvalidMagicException;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.util.StringIO;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SceneTemplateUtil {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Scene graph template", "*.ifsg");

	public static final String SCENE_TEMPLATE_MAGIC = "IFSG";

	public static void writeSceneTemplate(G3DSceneTemplate template, File f) {
		writeSceneTemplate(template, new DiskFile(f));
	}

	public static void writeSceneTemplate(G3DSceneTemplate template, FSFile f) {
		try {
			CMIFWriter dos = CMIFFile.writeLevel0File(SCENE_TEMPLATE_MAGIC);
			writeSceneTemplate(template, dos);
			dos.close();
			FSUtil.writeBytesToFile(f, dos.toByteArray());
		} catch (IOException ex) {
			Logger.getLogger(ModelUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void writeSceneTemplate(G3DSceneTemplate template, CMIFWriter dos) throws IOException {
		dos.writeStringUnterminated(SCENE_TEMPLATE_MAGIC);

		dos.writeString(template.name);			//Name
		writeSceneTemplateNode(template.root, dos);
	}

	private static void writeSceneTemplateNode(G3DSceneTemplate.G3DSceneTemplateNode node, CMIFWriter dos) throws IOException {
		dos.writeString(node.name);
		dos.writeEnum(node.parentMode);
		dos.writeString(node.parentAttachmentNode);
		node.location.write(dos);
		node.rotationDeg.write(dos);
		node.scale.write(dos);

		dos.writeInt(node.resourceLinks.size());
		for (G3DSceneTemplate.G3DSceneTemplateResourceLink resLnk : node.resourceLinks) {
			dos.writeEnum(resLnk.type);
			dos.writeString(resLnk.name);
		}

		dos.writeInt(node.controllerLinks.size());
		for (G3DSceneTemplate.G3DSceneTemplateControllerLink ctlLink : node.controllerLinks) {
			dos.writeEnum(ctlLink.type);
			dos.writeString(ctlLink.name);
			dos.writeFloat(ctlLink.frame);
		}

		MetaDataUtil.writeMetaData(node.metaData, dos);

		dos.writeInt(node.children.size());
		for (G3DSceneTemplate.G3DSceneTemplateNode child : node.children) {
			writeSceneTemplateNode(child, dos);
		}
	}

	public static G3DSceneTemplate readSceneTemplate(File f) {
		return readSceneTemplate(new DiskFile(f));
	}

	public static G3DSceneTemplate readSceneTemplate(FSFile f) {
		try {
			CMIFFile.Level0Info l0 = CMIFFile.readLevel0File(f, SCENE_TEMPLATE_MAGIC);

			G3DSceneTemplate sceneTemplate = readSceneTemplate(l0.io, l0.fileVersion);

			l0.io.close();
			return sceneTemplate;
		} catch (IOException ex) {
			Logger.getLogger(SceneTemplateUtil.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static G3DSceneTemplate readSceneTemplate(DataIOStream dis, int fileVersion) throws IOException {
		if (!StringIO.checkMagic(dis, SCENE_TEMPLATE_MAGIC)) {
			throw new InvalidMagicException("Invalid light magic.");
		}
		G3DSceneTemplate t = new G3DSceneTemplate();
		t.name = dis.readStringWithAddress();

		t.root = readSceneTemplateNode(dis, fileVersion);

		return t;
	}

	private static G3DSceneTemplate.G3DSceneTemplateNode readSceneTemplateNode(DataIOStream dis, int fileVersion) throws IOException {
		G3DSceneTemplate.G3DSceneTemplateNode n = new G3DSceneTemplate.G3DSceneTemplateNode();

		n.name = dis.readStringWithAddress();
		n.parentMode = G3DResourceInstance.ParentMode.VALUES[dis.read()];
		n.parentAttachmentNode = dis.readStringWithAddress();
		n.location.set(dis);
		n.rotationDeg.set(dis);
		n.scale.set(dis);
		
		int resLinkCount = dis.readInt();
		for (int i = 0; i < resLinkCount; i++) {
			G3DSceneTemplate.G3DSceneTemplateResourceLink l = new G3DSceneTemplate.G3DSceneTemplateResourceLink();
			l.type = G3DResourceType.VALUES[dis.read()];
			l.name = dis.readStringWithAddress();
			n.resourceLinks.add(l);
		}
		
		int ctlLinkCount = dis.readInt();
		for (int i = 0; i < ctlLinkCount; i++) {
			G3DSceneTemplate.G3DSceneTemplateControllerLink l = new G3DSceneTemplate.G3DSceneTemplateControllerLink();
			l.type = G3DResourceType.VALUES[dis.read()];
			l.name = dis.readStringWithAddress();
			l.frame = dis.readFloat();
			n.controllerLinks.add(l);
		}

		n.metaData = MetaDataUtil.readMetaData(dis, fileVersion);

		int childCount = dis.readInt();
		for (int i = 0; i < childCount; i++) {
			G3DSceneTemplate.G3DSceneTemplateNode child = readSceneTemplateNode(dis, fileVersion);
			n.addChild(child);
		}

		return n;
	}
}
