
package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public enum PokemonEvoStage {
	INVALID,
	BASIC,
	STAGE2,
	STAGE3;
	
	private static final PokemonEvoStage[] values = values();
	
	public static PokemonEvoStage read(DataInput dis) throws IOException{
		return values[dis.readUnsignedByte()];
	}
}
