
package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public class PokemonStatSet {
	public int hp;
	public int atk;
	public int def;
	public int speed;
	public int spAtk;
	public int spDef;
	
	public PokemonStatSet(){
		
	}
	
	public PokemonStatSet(DataInput in) throws IOException{
		hp = in.readUnsignedByte();
		atk = in.readUnsignedByte();
		def = in.readUnsignedByte();
		speed = in.readUnsignedByte();
		spAtk = in.readUnsignedByte();
		spDef = in.readUnsignedByte();
	}
}
