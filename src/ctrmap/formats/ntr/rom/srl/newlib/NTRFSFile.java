package ctrmap.formats.ntr.rom.srl.newlib;

import xstandard.fs.FSFile;
import xstandard.fs.FSUtil;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.ext.SubInputStream;
import java.util.ArrayList;
import java.util.List;

public class NTRFSFile extends FSFile {

	private final FSFile rom;
	
	private NTRFSFileInfo fileInfo;
	private String name;
	private boolean isDir;
	private NTRFSFile parent;
	private List<NTRFSFile> children = new ArrayList<>();

	public NTRFSFile(FSFile rom, NTRFSFileInfo fileInfo) {
		this.fileInfo = fileInfo;
		this.rom = rom;
	}
	
	public NTRFSFileInfo getFileInfo() {
		return fileInfo;
	}

	void addChild(NTRFSFile f) {
		if (f != null) {
			f.parent = this;
			children.add(f);
		}
	}

	@Override
	public FSFile getChild(String forName) {
		return FSUtil.getChildByListing(this, forName);
	}

	@Override
	public FSFile getParent() {
		if (parent == null) {
			return rom;
		}
		return parent;
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void setPath(String newPath) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public int length() {
		return fileInfo.endOffset - fileInfo.offset;
	}

	@Override
	public boolean isDirectory() {
		return fileInfo.isDirectory;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getName() {
		return fileInfo.name;
	}

	@Override
	public ReadableStream getInputStream() {
		return new SubInputStream(rom.getInputStream(), fileInfo.offset, fileInfo.endOffset);
	}

	@Override
	public WriteableStream getOutputStream() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public IOStream getIO() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<? extends FSFile> listFiles() {
		return children;
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ;
	}

}
