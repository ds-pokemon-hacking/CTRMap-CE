package ctrmap.util.tools.cont;

import ctrmap.formats.pokemon.containers.DefaultGamefreakContainer;
import ctrmap.formats.pokemon.containers.GFContainer;
import xstandard.fs.FSFile;

public interface AGFCContentIdentifier {

	public AGFCIdentifyResult identify(byte[] data, GFContainer cont, int fileIndex);

	public default GFContainer identifyContainer(FSFile fsf) {
		return new DefaultGamefreakContainer(fsf);
	}
}
