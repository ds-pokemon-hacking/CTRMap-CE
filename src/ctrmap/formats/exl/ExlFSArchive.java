package ctrmap.formats.exl;

import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.io.serialization.BinarySerializer;
import xstandard.io.serialization.ReferenceType;
import xstandard.io.serialization.annotations.MagicStr;
import xstandard.io.serialization.annotations.Version;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExlFSArchive {
	
	public static final String EXL_FSARCHIVE_MAGIC = "ARCX";

	@MagicStr(EXL_FSARCHIVE_MAGIC)
	public String magic;
	@Version
	public int fileVersion;
	public ExlHashTree dirTree;
	public ExlHashTree fileTree;
	public List<ExlFSDirectory> directories;
	public List<ExlFSFile> files;

	public ExlFSArchive(FSFile root) {
		magic = EXL_FSARCHIVE_MAGIC;
		fileVersion = 1;

		List<String> dirNames = new ArrayList<>();
		List<String> fileNames = new ArrayList<>();

		directories = new ArrayList<>();
		files = new ArrayList<>();

		for (FSFile f : root.listFiles()) {
			if (f.isDirectory()) {
				addDir(dirNames, fileNames, null, f, root);
			} else {
				addFile(fileNames, null, f, root);
			}
		}

		dirTree = new ExlHashTree(dirNames);
		fileTree = new ExlHashTree(fileNames);
	}

	private ExlFSFile addFile(List<String> fileNames, ExlFSDirectory parent, FSFile file, FSFile root) {
		ExlFSFile fsFile = new ExlFSFile(parent, file, ExlCompressionType.FASTLZ);
		fileNames.add(file.getPathRelativeTo(root));
		files.add(fsFile);
		return fsFile;
	}

	private ExlFSDirectory addDir(List<String> dirNames, List<String> fileNames, ExlFSDirectory parent, FSFile dirFile, FSFile root) {
		ExlFSDirectory dir = new ExlFSDirectory(parent, dirFile);
		dirNames.add(dirFile.getPathRelativeTo(root));
		directories.add(dir);
		for (FSFile sub : dirFile.listFiles()) {
			if (sub.isDirectory()) {
				dir.subDirectories.add(addDir(dirNames, fileNames, dir, sub, root));
			} else {
				dir.subFiles.add(addFile(fileNames, dir, sub, root));
			}
		}
		return dir;
	}

	public static void main(String[] args) {
		ExlFSArchive arc = new ExlFSArchive(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\extlib\\SampleData\\FSArchive"));
		arc.save(new DiskFile("D:\\_REWorkspace\\pokescript_genv\\codeinjection_new\\extlib\\SampleData\\Output.arc"));
	}

	public void save(FSFile dest) {
		try {
			BinarySerializer serializer = new BinarySerializer(dest.getIO(), ByteOrder.LITTLE_ENDIAN, ReferenceType.SELF_RELATIVE_POINTER);
			serializer.baseStream.setLength(0);
			serializer.serialize(this);
			serializer.baseStream.close();
		} catch (IOException ex) {
			Logger.getLogger(ExlFSArchive.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static class ExlFSDirectory {

		public String name;
		public ExlFSDirectory parent;
		public List<ExlFSDirectory> subDirectories;
		public List<ExlFSFile> subFiles;

		public ExlFSDirectory() {

		}

		public ExlFSDirectory(ExlFSDirectory parent, FSFile source) {
			this.name = source.getName();
			this.parent = parent;
			subDirectories = new ArrayList<>();
			subFiles = new ArrayList<>();
		}
	}

	public static class ExlFSFile {

		public String name;
		public ExlFSDirectory parent;
		public byte[] data;
		public int uncompSize;
		public ExlCompressionType compression;

		public ExlFSFile() {

		}

		public ExlFSFile(ExlFSDirectory parent, FSFile source, ExlCompressionType compression) {
			this.name = source.getName();
			this.parent = parent;
			data = source.getBytes();
			uncompSize = data.length;
			this.compression = compression;
			
			switch (compression) {
				case NONE:
					break;
				case FASTLZ:
					data = FastLZ5.compress(data);
					break;
			}
		}
	}
	
	public static enum ExlCompressionType {
		NONE,
		FASTLZ
	}
}
