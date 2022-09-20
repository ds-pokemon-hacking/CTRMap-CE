package ctrmap.formats.ntr.rom;

import ctrmap.formats.ntr.common.compression.BLZ;
import xstandard.formats.yaml.Key;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.yaml.YamlNode;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.DataInputEx;
import xstandard.io.base.iface.DataOutputEx;
import xstandard.io.base.impl.ext.data.DataInStream;
import xstandard.io.base.impl.ext.data.DataOutStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class OverlayTable {

	private FSFile source;

	public List<OverlayInfo> overlays = new ArrayList<>();

	public OverlayTable(FSFile fsf) {
		try {
			DataInStream in = fsf.getDataInputStream();

			int ovlCount = in.available() / OverlayInfo.BYTES;
			for (int i = 0; i < ovlCount; i++) {
				overlays.add(new OverlayInfo(in));
			}

			in.close();
			source = fsf;
		} catch (IOException ex) {
			Logger.getLogger(OverlayTable.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException("Invalid overlay table.");
		}
	}

	public boolean setYML(Yaml yml) {
		List<OverlayInfo> newOverlays = new ArrayList<>();
		for (YamlNode n : yml.root.children) {
			OverlayInfo i = OverlayInfo.makeOvlInfoFromYmlNode(n);
			if (i == null) {
				return false;
			}
			newOverlays.add(i);
		}
		overlays.clear();
		overlays.addAll(newOverlays);
		return true;
	}

	public Yaml getYML() {
		Yaml yml = new Yaml();

		for (OverlayInfo ov : overlays) {
			yml.root.addChild(ov.getYMLNode());
		}

		return yml;
	}

	private OverlayInfo getDummyNewOverlayInfo(int ovlId) {
		OverlayInfo i = new OverlayInfo();
		i.bssSize = 0;
		i.compressed = false;
		i.signed = true;
		i.ovlId = ovlId;
		i.fileId = ovlId;
		if (!overlays.isEmpty()) {
			OverlayInfo template = overlays.get(overlays.size() - 1);
			i.mountAddress = template.mountAddress;
			i.staticInitializersStart = i.mountAddress;
			i.staticInitializersEnd = i.mountAddress;
		}
		return i;
	}

	public OverlayInfo getOverlayInfo(int ovlNo) {
		if (ovlNo < overlays.size()) {
			return overlays.get(ovlNo);
		}
		return null;
	}

	public OverlayInfo getOrCreateOverlayInfo(int ovlNo) {
		while (ovlNo >= overlays.size()) {
			overlays.add(getDummyNewOverlayInfo(overlays.size()));
		}
		return overlays.get(ovlNo);
	}

	public void updateByDir(FSFile overlayDir) {
		for (OverlayInfo ov : overlays) {
			FSFile f = overlayDir.getChild(String.format("overlay_%04d.bin", ov.ovlId));
			updateByFile(ov.ovlId, f);
		}
	}

	public void updateByFile(int ovlId, FSFile f) {
		OverlayInfo ov = overlays.get(ovlId);
		if (f.isFile()) {
			BLZ.BLZHeader blz = BLZ.getBLZHeader(f);
			boolean compressed = false;
			int decompressedSize = f.length();
			if (blz != null) {
				if (blz.valid()) {
					compressed = true;
					decompressedSize = blz.getDecLength();
				}
			}

			ov.compressed = compressed;
			ov.compressedSize = compressed ? f.length() : 0;
			ov.mountSize = decompressedSize;
		}
	}

	public void write() {
		if (source != null) {
			try {
				DataOutStream out = source.getDataOutputStream();

				for (OverlayInfo ov : overlays) {
					ov.write(out);
				}

				out.close();
			} catch (IOException ex) {
				Logger.getLogger(OverlayTable.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public static class OverlayInfo {

		public static final int BYTES = 0x20;

		public int ovlId;
		public int mountAddress;
		public int mountSize;
		public int bssSize;

		public int staticInitializersStart;
		public int staticInitializersEnd;

		public int fileId;
		public int compressedSize;

		public boolean compressed;
		public boolean signed;

		public OverlayInfo(DataInputEx in) throws IOException {
			ovlId = in.readInt();
			mountAddress = in.readInt();
			mountSize = in.readInt();
			bssSize = in.readInt();
			staticInitializersStart = in.readInt();
			staticInitializersEnd = in.readInt();
			fileId = in.readInt();
			compressedSize = in.readUnsignedInt24();
			int flags = in.readUnsignedByte();
			compressed = (flags & 1) != 0;
			signed = (flags & 2) != 0;
		}

		public OverlayInfo() {

		}

		public static OverlayInfo makeOvlInfoFromYmlNode(YamlNode node) {
			if (!node.isKeyInt() || !node.hasChildren(
					"MountAddress",
					"MountSize",
					"BSSSize",
					"StaticInitializerAddress",
					"StaticInitializerEndAddress",
					"FileID",
					"Compressed",
					"CompressedSize",
					"Signed")) {
				return null;
			}
			OverlayInfo i = new OverlayInfo();
			i.ovlId = node.getKeyInt();
			i.mountAddress = node.getChildByName("MountAddress").getValueInt();
			i.mountSize = node.getChildByName("MountSize").getValueInt();
			i.bssSize = node.getChildByName("BSSSize").getValueInt();
			i.staticInitializersStart = node.getChildByName("StaticInitializerAddress").getValueInt();
			i.staticInitializersEnd = node.getChildByName("StaticInitializerEndAddress").getValueInt();
			i.fileId = node.getChildByName("FileID").getValueInt();
			i.compressed = node.getChildByName("Compressed").getValueBool();
			i.compressedSize = node.getChildByName("CompressedSize").getValueInt();
			i.signed = node.getChildByName("Signed").getValueBool();
			return i;
		}

		public YamlNode getYMLNode() {
			YamlNode n = new YamlNode(new Key(String.valueOf(ovlId)));

			n.addChild("MountAddress", mountAddress, true);
			n.addChild("MountSize", mountSize, true);
			n.addChild("BSSSize", bssSize, true);
			n.addChild("StaticInitializerAddress", staticInitializersStart, true);
			n.addChild("StaticInitializerEndAddress", staticInitializersEnd, true);
			n.addChild("FileID", fileId, false);
			n.addChild("Compressed", compressed);
			n.addChild("CompressedSize", compressedSize, true);
			n.addChild("Signed", signed);

			return n;
		}

		public void write(DataOutputEx out) throws IOException {
			out.writeInts(ovlId, mountAddress, mountSize, bssSize, staticInitializersStart, staticInitializersEnd, fileId);
			out.writeInt24(compressedSize);
			out.write((compressed ? 1 : 0) | (signed ? 2 : 0));
		}
	}
}
