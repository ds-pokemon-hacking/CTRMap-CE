
package ctrmap.formats.pokemon.containers.util;

import ctrmap.formats.pokemon.containers.GFContainer;
import xstandard.fs.FSFile;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.access.MemoryStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ContainerFile extends FSFile{

	private int idx;
	private GFContainer cont;
	
	public ContainerFile(GFContainer cont, int idx){
		this.idx = idx;
		this.cont = cont;
	}
	
	@Override
	public FSFile getChild(String forName) {
		return null;
	}

	@Override
	public FSFile getParent() {
		return cont.getOriginFile();
	}

	@Override
	public void mkdir() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void delete() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int length() {
		return cont.getFileSize(idx);
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public String getName() {
		return String.valueOf(idx);
	}

	@Override
	public ReadableStream getInputStream() {
		return getIO();
	}

	@Override
	public WriteableStream getOutputStream() {
		return new ContainerIO(this, true);
	}

	@Override
	public IOStream getIO() {
		return new ContainerIO(this, false);
	}

	@Override
	public List<FSFile> listFiles() {
		return new ArrayList<>();
	}

	@Override
	public int getChildCount() {
		return 0;
	}
	
	@Override
	public void setBytes(byte[] bytes){
		cont.storeFile(idx, bytes);
	}
	
	@Override
	public byte[] getBytes(){
		return cont.getFile(idx);
	}

	@Override
	public void setPath(String newPath) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getPermissions() {
		return FSF_ATT_READ | FSF_ATT_WRITE;
	}
	
	private static class ContainerIO extends MemoryStream {
		private ContainerFile cf;
		
		public ContainerIO(ContainerFile cf, boolean isOutputOnly){
			super();
			if (!isOutputOnly){
				buffer = cf.getBytes();
				limit = buffer.length;
			}
			this.cf = cf;
		}
		
		@Override
		public void close() throws IOException{
			super.close();
			cf.setBytes(toByteArray());
		}
	}
}
