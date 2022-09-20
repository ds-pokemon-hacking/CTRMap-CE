package ctrmap.missioncontrol_base.pokeparam;

import java.io.DataInput;
import java.io.IOException;

public enum PokemonType {
	NORMAL, //0
	FIGHT,  //1
	FLYING, //2
	POISON, //3
	GROUND, //4
	ROCK,   //5
	BUG,    //6
	GHOST,  //7
	STEEL,  //8
	FIRE,   //9
	WATER,  //10
	GRASS,  //11
	ELEC,   //12
	PSYCHIC,//13
	ICE,    //14
	DRAGON, //15
	DARK,   //16
	FAIRY;  //17

	private static final PokemonType[] values = values();

	public static PokemonType read(DataInput dis) throws IOException {
		return values[dis.readUnsignedByte()];
	}
}
