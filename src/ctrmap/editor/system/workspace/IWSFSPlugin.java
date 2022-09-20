package ctrmap.editor.system.workspace;

import xstandard.fs.accessors.arc.ArcFile;
import xstandard.fs.accessors.arc.ArcFileAccessor;
import rtldr.RExtensionBase;

public interface IWSFSPlugin extends RExtensionBase<WSFS>, ArcFileAccessor {

	/**
	 * Gets the 4 character magic of the archive format that this plugin is compatible with.
	 *
	 * @return
	 */
	public String getArcFileMagic();

	/**
	 * Gets the data of a file within an archive, decompressing it if needed.
	 *
	 * @param arc The archive file handle.
	 * @param path Path of the file within the archive.
	 * @return
	 */
	public byte[] getUncompFileData(ArcFile arc, String path);
}
