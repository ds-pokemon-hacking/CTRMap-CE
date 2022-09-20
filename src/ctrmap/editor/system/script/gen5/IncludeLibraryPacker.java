package ctrmap.editor.system.script.gen5;

import ctrmap.pokescript.ide.system.beaterscript.BS2PKS;
import ctrmap.scriptformats.pkslib.LibraryManifest;
import xstandard.formats.yaml.Yaml;
import xstandard.formats.zip.ZipArchive;
import xstandard.fs.FSFile;
import xstandard.fs.TempFileAccessor;
import xstandard.fs.accessors.DiskFile;
import xstandard.fs.accessors.MemoryFile;
import xstandard.net.FileDownloader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IncludeLibraryPacker {

	private static final String REPOSITORY_ZIP_PATH = "https://github.com/HelloOO7/PokeScriptSDK5/archive/refs/heads/master.zip";

	public static void main(String[] args) {
		File tempFile = TempFileAccessor.createTempFile("IncludeLibraryZip.zip");
		tempFile.delete();
		FileDownloader.downloadToFile(tempFile, REPOSITORY_ZIP_PATH);

		ZipArchive arc = new ZipArchive(new DiskFile(tempFile));
		FSFile dataRoot = arc.getChild("PokeScriptSDK5-master");
		FSFile outRoot = new DiskFile("src/ctrmap/resources/scripting/cm_ide/sdk/EV_GEN_V/");

		convertHeaderSet(outRoot, dataRoot, "yml/B2W2", "SDK5-B2W2-Generated.lib");
		convertHeaderSet(outRoot, dataRoot, "yml/BW", "SDK5-BW-Generated.lib");

		tempFile.delete();
	}

	private static void convertHeaderSet(FSFile outRoot, FSFile inRoot, String ymlFolderPath, String outFileName) {
		MemoryFile libRoot = MemoryFile.createDirectory("VirtualZipRoot");
		MemoryFile sourceRoot = libRoot.createChildDir("src");
		MemoryFile outYmlRoot = libRoot.createChildDir("yml");

		List<Yaml> ymls = new ArrayList<>();
		for (FSFile child : inRoot.getChild(ymlFolderPath).listFiles()) {
			if (child.getName().endsWith(Yaml.EXTENSION_FILTER.getPrimaryExtension())) {
				outYmlRoot.linkChild(child);
				ymls.add(new Yaml(child));
			}
		}

		inRoot.getChild(LibraryManifest.LIBRARY_MANIFEST_NAME).copyTo(libRoot);

		BS2PKS.makePKSIncludes(sourceRoot, ymls.toArray(new Yaml[ymls.size()]));

		ZipArchive.pack(libRoot, outRoot.getChild(outFileName));
	}
}
