
package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public class PokemonAbilityConfig {
	private final int[] ids;
	
	public PokemonAbilityConfig(int[] src){
		ids = src;
	}
	
	public PokemonAbilityConfig(DataInput in) throws IOException {
		ids = new int[Slot.values().length];
		for (int i = 0; i < ids.length; i++){
			ids[i] = in.readUnsignedByte();
		}
	}
	
	public int getAbilityBySlot(Slot c){
		return ids[c.ordinal()];
	}
	
	public void setAbilityBySlot(Slot c, int id){
		ids[c.ordinal()] = id; 
	}
	
	public static enum Slot {
		SLOT_1,
		SLOT_2,
		SLOT_HIDDEN
	}
}
