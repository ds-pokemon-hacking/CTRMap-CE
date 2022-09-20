package ctrmap.creativestudio.ngcs.io;

import ctrmap.creativestudio.ngcs.NGCS;
import ctrmap.renderer.scenegraph.G3DResource;
import xstandard.fs.FSFile;

public class NGCSExporter {

	public static void exportFiles(NGCS cs, IG3DFormatHandler handler, G3DResourceExportParam... params) {
		NGCSExporter.exportFiles(new NGCSIOProvider(cs, true), handler, params);
	}

	private static void exportFiles(NGCSIOProvider prov, IG3DFormatHandler handler, G3DResourceExportParam... params) {
		for (G3DResourceExportParam prm : params) {
			if (prm != null) {
				FSFile file = prm.output;

				if (!file.isDirectory()) {
					G3DIO.writeFile(prm.input, file, prov, handler);
				}
			}
		}
	}

	public static class G3DResourceExportParam {

		public final G3DResource input;
		public final FSFile output;

		public G3DResourceExportParam(G3DResource input, FSFile output) {
			this.input = input;
			this.output = output;
		}
	}
}
