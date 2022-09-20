package ctrmap.editor.system.workspace;

import ctrmap.editor.CTRMap;
import ctrmap.editor.system.workspace.wildcards.FSWildCardManagerCTR;
import xstandard.fs.FSFile;
import ctrmap.formats.common.GameInfo;
import xstandard.io.base.impl.ext.data.DataIOStream;
import xstandard.io.base.impl.ext.data.DataInStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameDetector {

	private static final String HEADER_BIN = "header.bin";

	public static boolean isDSGame(FSFile gameRoot) {
		if (gameRoot != null) {
			FSFile header = gameRoot.getChild(HEADER_BIN);
			System.out.println("Trying DS header.bin file at " + header);
			return FSFile.exists(header);
		}
		return false;
	}

	public static GameInfo createGameInfo(FSFile gameRoot) {
		GameInfo.SubGame sg = detectSubGame(gameRoot);
		GameInfo.Game g = detectGameType(sg);
		return new GameInfo.DefaultGameManager(g, sg);
	}

	public static GameInfo.Game detectGameType(FSFile gameRoot) {
		if (isDSGame(gameRoot)) {
			return detectGameType(detectSubGameDS(gameRoot.getChild(HEADER_BIN)));
		} else {
			return detectGameType(detectSubGame3DS(FSWildCardManagerCTR.INSTANCE.getFileFromRefPath(gameRoot, ":exheader:")));
		}
	}

	public static GameInfo.Game detectGameType(GameInfo.SubGame sg) {
		return sg != null ? sg.game : null;
	}

	public static String getFourCC(FSFile gameRoot) {
		FSFile hbin = gameRoot.getChild(HEADER_BIN);
		if (hbin != null) {
			try (DataIOStream io = hbin.getDataIOStream()) {
				return getFourCC(io);
			} catch (IOException ex) {
				return null;
			}
		}
		return null;
	}

	public static String getFourCC(DataIOStream io) throws IOException {
		io.seek(0xC);
		return io.readPaddedString(4);
	}

	public static GameInfo.SubGame detectSubGameDS(FSFile headerBin) {
		if (headerBin == null || !headerBin.exists()) {
			return null;
		}
		try {
			DataIOStream io = headerBin.getDataIOStream();
			String fourCC = getFourCC(io);
			io.close();

			switch (fourCC) {
				case "IREO":
					return GameInfo.SubGame.B2;
				case "IRDO":
					return GameInfo.SubGame.W2;
				case "IRBO":
					return GameInfo.SubGame.B;
				case "IRAO":
					return GameInfo.SubGame.W;
			}
		} catch (IOException ex) {
			Logger.getLogger(GameDetector.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static GameInfo.SubGame detectSubGame(FSFile root) {
		if (isDSGame(root)) {
			System.out.println("Detecting DS game...");
			return detectSubGameDS(root.getChild(HEADER_BIN));
		} else {
			System.out.println("Detecting 3DS game...");
			return detectSubGame3DS(FSWildCardManagerCTR.INSTANCE.getFileFromRefPath(root, ":exheader:"));
		}
		//return null;
	}

	public static GameInfo.SubGame detectSubGame3DS(FSFile exHeader) {
		if (exHeader == null || !exHeader.exists()) {
			return null;
		}
		try {
			DataInStream in = exHeader.getDataInputStream();
			String exeName = in.readPaddedString(8);
			GameInfo.SubGame g = null;
			switch (exeName) {
				case "kujira-1":
					g = GameInfo.SubGame.X;
					break;
				case "kujira-2":
					g = GameInfo.SubGame.Y;
					break;
				case "sango-1":
					g = GameInfo.SubGame.OMEGA;
					break;
				case "sango-2":
					g = GameInfo.SubGame.ALPHA;
					break;
				case "sango-sp":
					g = GameInfo.SubGame.DEMO;
					break;
			}
			return g;
		} catch (IOException ex) {
			Logger.getLogger(GameDetector.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public GameInfo.DefaultGameManager createGameManager(WSFS wsfs) {
		GameInfo.Game game = detectGameType(wsfs.getBaseFsFile(":romfs:"));
		GameInfo.SubGame subGame = detectSubGame(wsfs.getBaseFsFile(":exheader:"));
		return new GameInfo.DefaultGameManager(game, subGame);
	}
}
