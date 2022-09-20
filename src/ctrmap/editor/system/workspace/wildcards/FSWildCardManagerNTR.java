package ctrmap.editor.system.workspace.wildcards;

import xstandard.fs.FSWildCard;
import xstandard.fs.FSWildCardManager;

public class FSWildCardManagerNTR extends FSWildCardManager {

	public static final FSWildCardManagerNTR INSTANCE = new FSWildCardManagerNTR();
	
	private FSWildCardManagerNTR() {
		super(
				new FSWildCard("y9", "y9.bin", "arm9ovltable.bin")
		);
	}
}
