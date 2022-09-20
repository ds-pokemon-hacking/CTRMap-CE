
package ctrmap.formats.ntr.common.gfx;

import xstandard.formats.yaml.YamlNodeName;
import java.io.DataOutput;
import java.io.IOException;

/**
 *
 */
public class GXColorAlpha extends GXColor {

	@YamlNodeName("Alpha")
	public int alpha;

	public GXColorAlpha(int rgb, int a) {
		super(rgb);
		alpha = bit5to8(a);
	}

	public GXColorAlpha() {
	}

	public void writeAlpha(DataOutput out, boolean is16bit) throws IOException {
		if (is16bit) {
			out.writeShort(bit8to5(alpha));
		} else {
			out.write(bit8to5(alpha));
		}
	}
}
