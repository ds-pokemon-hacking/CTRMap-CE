
package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public enum PokemonEXPGroup {
	MED_FAST,
	ERRATIC,
	FLUCTUATING,
	MED_SLOW,
	FAST,
	SLOW;
	
	private static final PokemonEXPGroup[] values = values();
	
	public static PokemonEXPGroup read(DataInput dis) throws IOException{
		return values[dis.readUnsignedByte()];
	}
}
