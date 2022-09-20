
package ctrmap.formats.pokemon.text;

/**
 *
 */
public class MsgStr {
	public String value;
	public boolean encode9Bit = false;
	
	public MsgStr(String str){
		this(str, false);
	}
	
	public MsgStr(String str, boolean encode9Bit){
		this.value = str;
		this.encode9Bit = encode9Bit;
	}
	
	@Override
	public String toString(){
		return value;
	}
}
