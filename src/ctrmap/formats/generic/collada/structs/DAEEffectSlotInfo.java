package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import xstandard.math.vec.RGBA;
import java.util.List;
import org.w3c.dom.Element;

public class DAEEffectSlotInfo {

	public String mode;

	public String imageId;
	public RGBA color;

	public String uvSetName;

	public DAESampler2D sampler;

	public DAEEffectSlotInfo(Element slot, List<Element> params) {
		mode = slot.getTagName();
		Element texture = XmlFormat.getParamElement(slot, "texture");
		Element col = XmlFormat.getParamElement(slot, "color");

		if (texture != null) {
			String samplerName = texture.getAttribute("texture");
			uvSetName = texture.getAttribute("texcoord");

			Element samplerNewparam = XmlFormat.getByAttribute(params, "sid", samplerName);
			if (samplerNewparam != null) {
				//Blender DAE
				Element sampler2DElem = XmlFormat.getParamElement(samplerNewparam, "sampler2D");
				if (sampler2DElem != null) {
					sampler = new DAESampler2D(sampler2DElem);

					Element surface = XmlFormat.getByAttribute(params, "sid", sampler.surfaceName);

					if (surface != null) {
						imageId = XmlFormat.getParamNodeValue(surface, "surface", "init_from");
					}
				}
			}
			if (imageId == null) {
				//Maya/FBX converter DAE
				imageId = samplerName;
			}
		}
		if (col != null) {
			color = XmlFormat.getFPRGBAValue(col);
		}
	}

	public DAEEffectSlotInfo(String mode) {
		this.mode = mode;
	}
}
