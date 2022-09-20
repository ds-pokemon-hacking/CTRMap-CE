package ctrmap.editor.system.workspace;

import xstandard.fs.accessors.arc.ArcInput;
import xstandard.fs.FSFile;
import xstandard.fs.FSManager;
import xstandard.fs.FSWildCardManager;
import xstandard.fs.VFS;
import xstandard.fs.VFSRootFile;
import xstandard.fs.accessors.arc.ArcFile;
import xstandard.fs.accessors.arc.ArcFileAccessor;
import xstandard.io.base.iface.IOStream;
import xstandard.io.base.iface.ReadableStream;
import xstandard.io.base.iface.WriteableStream;
import xstandard.io.base.impl.access.MemoryStream;
import xstandard.util.ProgressMonitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import rtldr.JExtensionStateListener;
import rtldr.JRTLDRCore;
import xstandard.io.base.impl.ext.data.DataInStream;
import rtldr.JExtensionReceiver;

public class WSFS implements FSManager, ArcFileAccessor, JExtensionReceiver<IWSFSPlugin> {

	public VFS vfs;
	
	private Map<String, IWSFSPlugin> arcPlugins = new HashMap<>();

	public WSFS(VFSRootFile wsPath, VFSRootFile gamePath, FSFile userDataPath) {
		JRTLDRCore.bindExtensionManager("WSFSPlugin", this, new JExtensionStateListener<IWSFSPlugin>() {
			@Override
			public void onExtensionLoaded(IWSFSPlugin ext) {
				arcPlugins.put(ext.getArcFileMagic(), ext);
			}

			@Override
			public void onExtensionUnloaded(IWSFSPlugin ext) {
				arcPlugins.remove(ext.getArcFileMagic());
			}
		});
		vfs = new VFS(FSWildCardManager.BLANK_WILD_CARD_MNG, this);
		vfs.initVFS(gamePath, wsPath);
		vfs.createChangeBlacklist(userDataPath.getChild("vfs_blacklist_temp.tmp"));
	}
	
	public void free() {
		JRTLDRCore.unregistExtensionManager(this);
		vfs.terminate();
	}

	public void setWildCardManager(FSWildCardManager mng) {
		vfs.setWildCardManager(mng);
	}

	@Override
	public FSFile getFsFile(String path) {
		return vfs.getFile(path);
	}

	public FSFile getBaseFsFile(String path) {
		return vfs.getBaseFSFile(path);
	}

	public FSFile getOvFSFile(String path) {
		return vfs.getOvFSFile(path);
	}
	
	private FSFile lastMagicArc;
	private String lastArcMagic;
	
	private String getArcFileMagic(FSFile arc) {
		if (arc == null) {
			return null;
		}
		if (lastMagicArc == arc) {
			return lastArcMagic;
		}
		if (arc instanceof ArcFile) {
			ArcFile af = (ArcFile) arc;
			if (af.getSource() == lastMagicArc) {
				return lastArcMagic;
			}
		}
		try (DataInStream in = arc.getDataInputStream()) {
			String magic = in.readPaddedString(4);
			
			lastMagicArc = arc;
			lastArcMagic = magic;
			
			return magic;
		}
		catch (IOException ioe) {
			return null;
		}
	}
	
	private IWSFSPlugin getAFAPlugin(ArcFile arc) {
		return arcPlugins.get(getArcFileMagic(arc));
	}

	@Override
	public List<? extends FSFile> getArcFiles(ArcFile arc) {
		try (DataInStream in = arc.getDataInputStream()) {
			String magic = in.readPaddedString(4);
			
			IWSFSPlugin accessor = arcPlugins.get(magic);
			
			return accessor.getArcFiles(arc);
		} catch (IOException ex) {
			Logger.getLogger(WSFS.class.getName()).log(Level.SEVERE, null, ex);
		}
		return new ArrayList<>();
	}

	@Override
	public ReadableStream getInputStreamForArcMember(ArcFile arc, String path) {
		try {
			//System.out.println("Requested file from ArcFile " + arc.getPath() + " with path " + path);
			//Copy the file directly into the OvFS for cached decompression and stuff
			FSFile ovFsFile = getOvFSFile(arc.getPath() + "/" + path);

			byte[] decData = getAFAPlugin(arc).getUncompFileData(arc, path);

			//Explanation: The VFS oftentimes wants to return the actual data from the arc instead of the cached decompressed data
			//Example of this is the fileCmp done to check for changes in files in the blacklist
			//In turn, we have to return a memory ByteArrayInputStream if the ArcFileMember input stream is requested.
			//This should be a good approach since if the file is already extracted into OvFS, the VFS will return that file instead of the ArcMember.
			//As a result, this behavior is only exhibited it the system requests specifically for the BaseFS file, which to my knowledge is only in the case of the MonitoredStream fileCmp.
			if (!ovFsFile.exists()) {
				vfs.notifyOvFsNewFileInit(ovFsFile.getPathRelativeTo(vfs.getOvFSRoot()));
				WriteableStream os = getSafeOutputStream(ovFsFile);
				os.write(decData);
				os.close();

				//The VFS file containing the ArcFileMember's getExistingFile method should now detect the existing OvFS file and link it
				return ovFsFile.getInputStream();
			} else {
				return new MemoryStream(decData);
			}
		} catch (IOException ex) {
			Logger.getLogger(WSFS.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	@Override
	public WriteableStream getOutputStreamForArcMember(ArcFile arc, String path) {
		return getAFAPlugin(arc).getOutputStreamForArcMember(arc, path);
	}

	@Override
	public IOStream getIOForArcMember(ArcFile arc, String path) {
		return getAFAPlugin(arc).getIOForArcMember(arc, path);
	}

	public static WriteableStream getSafeOutputStream(FSFile fsf) {
		if (!fsf.exists()) {
			fsf.getParent().mkdirs();
			//Create parent directories to ensure the following OS can write
		}
		return fsf.getOutputStream();
	}

	@Override
	public boolean isArcFile(FSFile f) {
		if (f instanceof ArcFile) {
			return true;
		}
		if (f != null && f.exists() && !f.isDirectory()) {
			return arcPlugins.containsKey(getArcFileMagic(f));
		}
		return false;
	}

	@Override
	public void writeToArcFile(ArcFile arc, ProgressMonitor monitor, ArcInput... inputs) {
		getAFAPlugin(arc).writeToArcFile(arc, monitor, inputs);
	}

	@Override
	public int getDataSizeForArcMember(ArcFile arc, String path) {
		return getAFAPlugin(arc).getDataSizeForArcMember(arc, path);
	}
}
