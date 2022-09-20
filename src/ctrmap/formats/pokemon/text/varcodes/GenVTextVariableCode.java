package ctrmap.formats.pokemon.text.varcodes;

public enum GenVTextVariableCode implements TextVariableCode {
	SCROLL(0xBE00, true),
	CLEAR(0xBE01, true),
	WAIT(0xBE02, true),
	SPEED(0xBE09, true),
	BLANK(0xBDFF, true),
	COLOR(0xFF00, true),
	
	COLOREX(0xBD00, true),
	COLORRESET(0xBD01, true),
	CENTER(0xBD02, true),
	RIGHT(0xBD03, true),
	SKIPPIXELS(0xBD04, true),
	SETXPOS(0xBD05, true),
	
	TRNAME(0x0100),
	PKNAME(0x0101),
	PKNICK(0x0102),
	LOCATION(0x105),
	TYPE(0x0103),
	ABILITY(0x0106),
	MOVE(0x0107),
	ITEM1(0x0108),
	ITEM2(0x0109),
	DRESSUPPROP(0x010A),
	BOX(0x010B),
	BATTLEPK(0x010C),
	STAT(0x010D),
	TRCLASS(0x010E),
	HOBBY(0x010F),
	PASSPOWER(0x0110),
	BAGPOCKET(0x0112),
	SURVEYRESULT(0x0113),

	GENERIC(0x011C),
	
	DRESSUPSHOWNAME(0x0122),
	DRESSUPSHOWFEELING(0x0123),

	COUNTRY(0x0124),
	PROVINCE(0x0125),

	DRESSUPBODYPART(0x0131),
	DECORNAME(0x0132),
	DRESSUPAUDIENCE(0x0133),
	MEDAL(0x135),
	MEDALISTRANK(0x136),
	JOINAVINPUT(0x137),
	
	TOURNAMENT(0x013B),
	BATTLEMODE(0x013C),
	INSTTITLE(0x013D),
	WEATHER(0x013E),
	MOVIENAME(0x013F),
	FUNFESTMISSION(0x0140),
	JOINAVRANK(0x0142),
	ENTRALINKLVL(0x0143),
	
	NUM1(0x0200),
	NUM2(0x0201),
	NUM3(0x0202),
	NUM4(0x0203),
	NUM5(0x0204),
	NUM6(0x0205),
	NUM7(0x0206),
	NUM8(0x0207),
	NUM9(0x0208);
	
	public static final GenVTextVariableCode[] CODES = values();
	
	public final int binaryId;
	public final boolean isImperative;
	
	private GenVTextVariableCode(int binaryId){
		this(binaryId, false);
	}
	
	private GenVTextVariableCode(int binaryId, boolean isImperative){
		this.binaryId = binaryId;
		this.isImperative = isImperative;
	}
	
	public static GenVTextVariableCode getCode(int binaryId){
		for (GenVTextVariableCode c : values()){
			if (c.binaryId == binaryId){
				return c;
			}
		}
		return null;
	}
	
	public static GenVTextVariableCode getCode(String friendlyName){
		String lc = friendlyName.toLowerCase();
		for (GenVTextVariableCode c : values()){
			if (c.toString().toLowerCase().equals(lc)){
				return c;
			}
		}
		return null;
	}

	@Override
	public int getBinary() {
		return binaryId;
	}

	@Override
	public boolean getIsImperative() {
		return isImperative;
	}

	@Override
	public String getName() {
		return toString();
	}

	@Override
	public int getOverrideArgCount() {
		return -1;
	}
}
