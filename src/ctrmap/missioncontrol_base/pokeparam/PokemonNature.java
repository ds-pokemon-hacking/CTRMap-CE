package ctrmap.missioncontrol_base.pokeparam;

public enum PokemonNature {
	HARDY,
	LONELY,
	BRAVE,
	ADAMANT,
	NAUGHTY,
	BOLD,
	DOCILE,
	RELAXED,
	IMPISH,
	LAX,
	TIMID,
	HASTY,
	SERIOUS,
	JOLLY,
	NAIVE,
	MODEST,
	MILD,
	QUIET,
	BASHFUL,
	RASH,
	CALM,
	GENTLE,
	SASSY,
	CAREFUL,
	QUIRKY;
	
	private static final PokemonNature[] values = values();
	
	public static final int COUNT = values.length;
	
	public static PokemonNature valueOf(int id){
		return values[id];
	}
}
