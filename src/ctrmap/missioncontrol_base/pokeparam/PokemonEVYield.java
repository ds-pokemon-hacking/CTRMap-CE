
package ctrmap.missioncontrol_base.pokeparam;

public class PokemonEVYield {
	private int data;
	
	public PokemonEVYield(int source){
		data = source;
	}
	
	public int getEVYieldForStat(PokemonStat stat){
		return data >> (stat.ordinal() * 2) & 0b11;
	}
	
	public void setEVYieldForStat(PokemonStat stat, int value){
		int shift = stat.ordinal() * 2;
		data &= ~(0b11 << shift);
		data |= value & 0b11 << shift;
	}
}
