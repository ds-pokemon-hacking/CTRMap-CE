package ctrmap.formats.ntr.rom.srl.newlib;

import ctrmap.formats.ntr.rom.OverlayTable;
import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.FSFileAdapter;
import xstandard.fs.accessors.InlineFile;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.text.FormattingUtils;
import xstandard.util.ArraysEx;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NDSROMFile extends FSFileAdapter {

	private SRLHeader header;

	private InlineFile headerBin;
	private InlineFile bannerBin;
	private NTRFSFile data;
	private InlineFile y7;
	private InlineFile y9;
	private NTRFSFile overlay;
	private NTRFSFile overlay7;

	private OverlayTable ovl9;
	private OverlayTable ovl7;

	public NDSROMFile(FSFile fsf) {
		super(fsf);
		if (fsf.isFile()) {
			loadROM(fsf);
		} else if (fsf.isDirectory()) {

		} else {
			throw new RuntimeException("Source NDSROMFile inexistent!");
		}
	}
	
	public static void main(String[] args) {
		NDSROMFile rom = new NDSROMFile(new DiskFile("D:\\_REWorkspace\\PlatTest\\rom.nds"));
		rom.tree(System.out);
		FSUtil.copyDirectory(rom, new DiskFile("D:\\_REWorkspace\\PlatTest\\outtest"));
	}

	@Override
	public boolean isDirectory() {
		return true;
	}
	
	@Override
	public FSFile getChild(String forName) {
		return FSUtil.getChildByListing(this, forName);
	}
	
	@Override
	public List<? extends FSFile> listFiles() {
		return ArraysEx.asList(headerBin, bannerBin, data, y7, y9, overlay7, overlay);
	}

	private void loadROM(FSFile romFile) {
		try (DataIOStream io = romFile.getDataIOStream()) {
			header = new SRLHeader(io);

			headerBin = new InlineFile(this, "header.bin", 0, 0x200);
			bannerBin = new InlineFile(this, "banner.bin", header.iconOffset, header.iconOffset + 0x840);

			Map<Integer, NTRFSFileInfo> fsFileInfo = new HashMap<>();

			io.seek(header.fatOffset);
			int fatCount = header.fatSize >> 3;
			for (int i = 0; i < fatCount; i++) {
				fsFileInfo.put(i, NTRFSFileInfo.makeFileInfo(io.readInt(), io.readInt()));
			}

			io.seek(header.fntOffset);

			data = new NTRFSFile(romFile, NTRFSFileInfo.makeDirInfo("data"));
			readROMDir(data, 0xf000, io, fsFileInfo);

			y9 = new InlineFile(this, "y9.bin", header.arm9OverlayOffset, header.arm9OverlayOffset + header.arm9OverlaySize);
			y7 = new InlineFile(this, "y7.bin", header.arm7OverlayOffset, header.arm7OverlayOffset + header.arm7OverlaySize);
			ovl9 = new OverlayTable(y9);
			ovl7 = new OverlayTable(y7);
			overlay = new NTRFSFile(this, NTRFSFileInfo.makeDirInfo("overlay"));
			overlay7 = new NTRFSFile(this, NTRFSFileInfo.makeDirInfo("overlay7"));

			readOverlays(ovl9, overlay, fsFileInfo);
			readOverlays(ovl7, overlay7, fsFileInfo);
		} catch (IOException ex) {
			Logger.getLogger(NDSROMFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
	
	private void loadDir(FSFile rootDir) {
		try {
			header = new SRLHeader(rootDir.getChild("header.bin"));
		} catch (IOException ex) {
			Logger.getLogger(NDSROMFile.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void readOverlays(OverlayTable ovlTable, NTRFSFile ovlDir, Map<Integer, NTRFSFileInfo> fsFileInfo) {
		for (int ovlIdx = 0; ovlIdx < ovlTable.overlays.size(); ovlIdx++) {
			NTRFSFileInfo fi = fsFileInfo.get(ovlTable.overlays.get(ovlIdx).fileId);
			fi.name = "overlay_" + FormattingUtils.getIntWithLeadingZeros(4, ovlIdx) + ".bin";
			NTRFSFile ovlFile = new NTRFSFile(this, fi);
			overlay.addChild(ovlFile);
		}
	}

	private void readROMDir(NTRFSFile dest, int dirHeader, DataIOStream io, Map<Integer, NTRFSFileInfo> fsFileInfo) throws IOException {
		io.checkpoint();
		io.seek(header.fntOffset + ((dirHeader & 0xFFF) << 3));

		int childrenOffset = io.readInt();
		int fileId = io.readUnsignedShort();
		int parentDirID = io.readUnsignedShort();

		io.seek(header.fntOffset + childrenOffset);

		int ident;

		while (((ident = io.read()) & 0x7F) != 0) {
			String childName = io.readPaddedString(ident & 0x7F);
			if (fntIdentIsDirectory(ident)) {
				int childDirHeader = io.readUnsignedShort();

				NTRFSFile dirFsFile = new NTRFSFile(source, NTRFSFileInfo.makeDirInfo(childName));

				readROMDir(dirFsFile, childDirHeader, io, fsFileInfo);

				dest.addChild(dirFsFile);
			} else {
				NTRFSFileInfo fi = fsFileInfo.get(fileId);
				fi.name = childName;

				NTRFSFile child = new NTRFSFile(source, fi);
				dest.addChild(child);

				fileId++;
			}
		}
		io.resetCheckpoint();
	}

	private boolean fntIdentIsDirectory(int ident) {
		return (ident & 0x80) != 0;
	}
}
