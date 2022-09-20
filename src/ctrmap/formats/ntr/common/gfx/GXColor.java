
package ctrmap.formats.ntr.common.gfx;

import xstandard.formats.yaml.YamlNodeName;
import xstandard.math.vec.RGBA;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class GXColor {
	
	public static final GXColor WHITE = new GXColor(0x7FFF);

	@YamlNodeName("Red")
	public int red;
	@YamlNodeName("Green")
	public int green;
	@YamlNodeName("Blue")
	public int blue;

	public GXColor(int rgb) {
		red = bit5to8(rgb & 31);
		green = bit5to8((rgb >> 5) & 31);
		blue = bit5to8((rgb >> 10) & 31);
	}
	
	public GXColor(RGBA color) {
		red = color.r;
		green = color.g;
		blue = color.b;
	}
	
	public GXColor(DataInput in) throws IOException {
		this(in.readUnsignedShort());
	}

	public GXColor() {
	}
	
	public GXColor(GXColor left, GXColor right, float weight){
		red = (int)(left.red + (right.red - left.red) * weight);
		green = (int)(left.green + (right.green - left.green) * weight);
		blue = (int)(left.blue + (right.blue - left.blue) * weight);
	}
	
	public RGBA toRGBA(){
		return new RGBA(red, green, blue, 255);
	}
	
	public int getBits() {
		return bit8to5(blue) << 10 | bit8to5(green) << 5 | bit8to5(red);
	}

	public void write(DataOutput out) throws IOException {
		out.writeShort(getBits());
	}

	public static int bit5to8(int value) {
		return value << 3 | value >>> 2;
	}

	public static int bit8to5(int value) {
		return (value >> 3) & 31;
	}

}
