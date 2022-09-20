package ctrmap.editor.system.workspace.wildcards;

import xstandard.fs.FSWildCard;
import xstandard.fs.FSWildCardManager;

public class FSWildCardManagerCTR extends FSWildCardManager {

	public static final FSWildCardManagerCTR INSTANCE = new FSWildCardManagerCTR();
	
	private FSWildCardManagerCTR() {
		super(
				new FSWildCard("romfs", "RomFS", "romfs"),
				new FSWildCard("exefs", "ExeFS", "exefs"),
				new FSWildCard("codebin", "code.bin", ".code.bin", "mario.exe"),
				new FSWildCard("exheader", "ExHeader.bin", "exheader.bin", "exheader.exh", "ExHeader.exh", "exh.bin")
		);
	}
}
