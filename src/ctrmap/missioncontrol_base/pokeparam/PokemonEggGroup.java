
package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public enum PokemonEggGroup {
	INVALID,
	MONSTER,
	WATER1,
	BUG,
	FLYING,
	FIELD,
	FAIRY,
	GRASS,
	HUMANLIKE,
	WATER3,
	MINERAL,
	AMORPHOUS,
	WATER2,
	DITTO,
	DRAGON,
	UNDISCOVERED;
	
	private static final PokemonEggGroup[] values = values();
	
	public static PokemonEggGroup read(DataInput dis) throws IOException{
		return values[dis.readUnsignedByte()];
	}
}
