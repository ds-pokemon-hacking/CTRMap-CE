
package ctrmap.formats.pokemon.text.varcodes;

/**
 *
 */
public interface TextVariableCode {
	public String getName();
	public int getBinary();
	public int getOverrideArgCount();
	public boolean getIsImperative();
}
