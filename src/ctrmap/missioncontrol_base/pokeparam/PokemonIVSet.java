package ctrmap.missioncontrol_base.pokeparam;

public class PokemonIVSet {

	private final int[] ivData = new int[PokemonStat.COUNT];
	
	public PokemonIVSet(){
		
	}
	
	public PokemonIVSet(int union){
		for (int i = 0; i < ivData.length; i++){
			ivData[i] = union & 31;
			union >>= 5;
		}
	}

	public void setAll(int value) {
		for (int i = 0; i < ivData.length; i++){
			ivData[i] = value;
		}
	}
	
	public void setIV(PokemonStat stat, int value) {
		ivData[stat.ordinal()] = value;
	}

	public int makeUnion() {
		int union = 0;
		for (int i = 0; i < ivData.length; i++) {
			union |= (ivData[i] << (i * 5));
		}
		return union;
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < ivData.length; i++){
			sb.append(PokemonStat.values()[i]);
			sb.append(": ");
			sb.append(ivData[i]);
			sb.append("\n");
		}
		return sb.toString();
	}
}
