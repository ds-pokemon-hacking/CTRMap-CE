package ctrmap.formats.pokemon.text.varcodes;

public enum GenVITextVariableCode implements TextVariableCode {
	SCROLL(0xBE00, true),
	CLEAR(0xBE01, true),
	WAIT(0xBE02, true, 1),
	BLANK(0xBDFF, true, 1),
	COLOR(0xFF00, true),
	
	TRNAME(0x0100),
	PKNAME(0x0101),
	PKNICK(0x0102),
	TYPE(0x0103),
	LOCATION(0x0105),
	ABILITY(0x0106),
	MOVE(0x0107),
	ITEM1(0x0108),
	ITEM2(0x0109),
	sTRBAG(0x010A),
	BOX(0x010B),
	EVSTAT(0x010D),
	OPOWER(0x0110),
	RIBBON(0x0127),
	MIINAME(0x0134),
	WEATHER(0x013E),
	TRNICK(0x0189),
	TRNICKSHORT(0x018A),
	SHOUTOUT(0x018B),
	BERRY(0x018E),
	REMFEEL(0x018F),
	REMQUAL(0x0190),
	WEBSITE(0x0191),
	CHOICECOS(0x019C),
	GSYNCID(0x01A1),
	PRVIDSAY(0x0192),
	BTLTEST(0x0193),
	GENLOC(0x0195),
	CHOICEFOOD(0x0199),
	HOTELITEM(0x019A),
	TAXISTOP(0x019B),
	MAISTITLE(0x019F),
	ITEMPLUR0(0x1000),
	ITEMPLUR1(0x1001),
	GENDERBRNCH(0x1100),
	NUMBRNCH(0x1101),
	iCOLOR2(0x1302),
	iCOLOR3(0x1303),
	NUM1(0x0200),
	NUM2(0x0201),
	NUM3(0x0202),
	NUM4(0x0203),
	NUM5(0x0204),
	NUM6(0x0205),
	NUM7(0x0206),
	NUM8(0x0207),
	NUM9(0x0208);
	
	public static final GenVITextVariableCode[] CODES = values();
	
	public final int binaryId;
	public final boolean isImperative;
	public final int overrideArgCount;
	
	private GenVITextVariableCode(int binaryId){
		this(binaryId, false);
	}
	
	private GenVITextVariableCode(int binaryId, boolean isImperative){
		this(binaryId, isImperative, -1);
	}
	
	private GenVITextVariableCode(int binaryId, boolean isImperative, int overrideArgCount){
		this.binaryId = binaryId;
		this.isImperative = isImperative;
		this.overrideArgCount = overrideArgCount;
	}
	
	public static GenVITextVariableCode getCode(int binaryId){
		for (GenVITextVariableCode c : values()){
			if (c.binaryId == binaryId){
				return c;
			}
		}
		return null;
	}
	
	public static GenVITextVariableCode getCode(String friendlyName){
		String lc = friendlyName.toLowerCase();
		for (GenVITextVariableCode c : values()){
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
		return overrideArgCount;
	}
}
