package ctrmap.formats.common;

public abstract class GameInfo {

	public abstract Game getGame();

	public abstract SubGame getSubGame();

	public boolean isGenVI() {
		return !isGenV();
	}

	public boolean isXY() {
		return getGame() == Game.XY;
	}

	public boolean isOA() {
		return getGame() == Game.ORAS || getGame() == Game.ORAS_DEMO;
	}

	public boolean isOADemo() {
		return getGame() == Game.ORAS_DEMO;
	}

	public boolean isGenV() {
		return getGame() == Game.BW || getGame() == Game.BW2;
	}

	public boolean isBW() {
		return getGame() == Game.BW;
	}

	public boolean isBW2() {
		return getGame() == Game.BW2;
	}
	
	public boolean isPrimary() {
		SubGame sg = getSubGame();
		return sg == SubGame.X || sg == SubGame.OMEGA || sg == SubGame.W || sg == SubGame.W2;
	}
	
	public boolean isSecondary() {
		return !isPrimary();
	}

	public enum Game {
		XY(true),
		ORAS(true),
		ORAS_DEMO(false),
		BW(true),
		BW2(true);

		public final boolean isStandalone;

		private Game(boolean isStandalone) {
			this.isStandalone = isStandalone;
		}
	}

	public enum SubGame {
		X(Game.XY, "Pokémon X"),
		Y(Game.XY, "Pokémon Y"),
		ALPHA(Game.ORAS, "Pokémon Alpha Sapphire"),
		OMEGA(Game.ORAS, "Pokémon Omega Ruby"),
		DEMO(Game.ORAS_DEMO, "Pokémon ΩR&αS Special Demo"),
		B(Game.BW, "Pokémon Black"),
		W(Game.BW, "Pokémon White"),
		B2(Game.BW2, "Pokémon Black 2"),
		W2(Game.BW2, "Pokémon White 2");

		public final Game game;
		public final String friendlyName;

		private SubGame(Game game, String friendlyName) {
			this.game = game;
			this.friendlyName = friendlyName;
		}
	}

	public static class DefaultGameManager extends GameInfo {

		private Game game;
		private SubGame subGame;

		public DefaultGameManager(Game g, SubGame sg) {
			game = g;
			subGame = sg;
		}

		@Override
		public GameInfo.Game getGame() {
			return game;
		}

		@Override
		public SubGame getSubGame() {
			return subGame;
		}
	}
}
