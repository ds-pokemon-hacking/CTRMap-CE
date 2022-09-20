package ctrmap.formats.pokemon.containers;

import ctrmap.formats.pokemon.containers.util.ContentType;
import xstandard.fs.FSFile;

public class DefaultGamefreakContainer extends GFContainer {

	private String magic;

	private boolean isPaddingExplicit = false;
	private boolean padding = true;

	public DefaultGamefreakContainer(FSFile f, String magic) {
		super(f);
		this.magic = magic;
	}

	public DefaultGamefreakContainer(FSFile f, String magic, int fileCount) {
		super(f);
		this.magic = magic;
		initializeContainer(fileCount);
	}

	public DefaultGamefreakContainer(FSFile f, String magic, int fileCount, boolean padding) {
		super(f);
		this.magic = magic;
		this.isPaddingExplicit = true;
		this.padding = padding;
		initializeContainer(fileCount);
	}

	@Override
	public String getSignature() {
		return magic;
	}

	@Override
	public ContentType getDefaultContentType(int index) {
		return ContentType.UNKNOWN;
	}

	public void setPaddingExplicit(boolean value) {
		isPaddingExplicit = true;
		padding = value;
	}

	@Override
	public boolean getIsPadded() {
		if (isPaddingExplicit) {
			return padding;
		}
		int[] offsets = getContHeader().fileOffsets;
		for (int offs : offsets) {
			if (offs % 0x80 != 0) {
				return false;
			}
		}
		return true;
	}
}
