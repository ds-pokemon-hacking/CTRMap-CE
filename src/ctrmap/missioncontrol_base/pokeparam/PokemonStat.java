
package ctrmap.missioncontrol_base.pokeparam;

public enum PokemonStat {
	HP,
	ATK,
	DEF,
	SPE,
	SPA,
	SPD;
	
	private static final PokemonStat[] values = values();
	
	public static final int COUNT = values.length;
}
