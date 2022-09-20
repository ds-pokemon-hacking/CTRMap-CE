
package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public class PokemonHeldItemConfig {
	private final int[] ids;
	
	public PokemonHeldItemConfig(int[] src){
		ids = src;
	}
	
	public PokemonHeldItemConfig(DataInput in) throws IOException {
		ids = new int[Chance.values().length];
		for (int i = 0; i < ids.length; i++){
			ids[i] = in.readShort();
		}
	}
	
	public int getItemByChance(Chance c){
		return ids[c.ordinal()];
	}
	
	public void setItemByChance(Chance c, int id){
		ids[c.ordinal()] = id; 
	}
	
	public static enum Chance {
		_50,
		_5,
		_1
	}
}
