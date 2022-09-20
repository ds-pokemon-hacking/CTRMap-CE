
package ctrmap.util;

import xstandard.fs.FSFile;
import xstandard.fs.FSManager;
import xstandard.fs.FSWildCardManager;
import xstandard.fs.accessors.arc.ArcFileAccessor;

/**
 *
 */
public class DirectFSManager implements FSManager {

	private FSFile root;
	private FSWildCardManager wcMng;
	
	public DirectFSManager(FSFile root){
		this(root, FSWildCardManager.BLANK_WILD_CARD_MNG);
	}
	
	public DirectFSManager(FSFile root, FSWildCardManager wcMng){
		this.root = root;
		this.wcMng = wcMng;
	}
	
	public FSWildCardManager getWildCardManager(){
		return wcMng;
	}

	@Override
	public FSFile getFsFile(String path) {
		return root.getChild(path);
	}

}
