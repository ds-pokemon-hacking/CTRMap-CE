package ctrmap.creativestudio.ngcs.io;

import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.pokemon.containers.GFContainer;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.MemoryFile;
import java.util.List;

public class NGCSImporter {

	public static G3DResource importFiles(NGCS cs, FSFile... files) {
		return importFiles(cs, cs.getIOManager().getAllFormatHandlers(), files);
	}

	public static G3DResource importFiles(NGCS cs, IG3DFormatHandler[] formats, FSFile... files) {
		return importFiles(new NGCSIOProvider(cs, false), formats, files);
	}
	
	private static G3DResource importFiles(NGCSIOProvider prov, IG3DFormatHandler[] formats, FSFile... files) {
		G3DResource result = new G3DResource();

		for (FSFile file : files) {
			if (file.isDirectory()) {
				List<? extends FSFile> children = file.listFiles();
				result.mergeFull(importFiles(prov, formats, children.toArray(new FSFile[children.size()])));
			} else if (file.exists()) {
				G3DResource readResult = null;
				readResult = G3DIO.readFile(file, prov, formats);
				if (readResult != null) {
					result.mergeFull(readResult);
				} else {
					if (GFContainer.isContainer(file.getDataInputStream())) {
						GFContainer cont = new DefaultGamefreakContainer(file, "DM");
						int count = cont.getFileCount();

						for (int i = 0; i < count; i++) {
							result.mergeFull(importFiles(prov, formats, new MemoryFile("container file dummy", cont.getFile(i))));
						}
					}
				}
			}
		}
		return result;
	}
}
