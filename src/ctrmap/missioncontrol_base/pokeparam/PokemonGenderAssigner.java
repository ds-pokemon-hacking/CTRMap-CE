
package ctrmap.missioncontrol_base.pokeparam;

public class PokemonGenderAssigner {
	private int config;
	
	public PokemonGenderAssigner(int cfg){
		config = cfg;
	}
	
	public void setSingleGender(PokemonGender g){
		config = g.ordinal() * 254;
	}
	
	public void setChanceGender(float maleToFemaleRatio){
		config = (int)(1 - maleToFemaleRatio) * 254;
	}
}
