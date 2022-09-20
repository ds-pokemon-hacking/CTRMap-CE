package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.Light;
import xstandard.math.vec.RGBA;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAELight implements DAEIDAble, DAESerializable {

	public String id;
	public String name;

	public DAELightType type;
	public RGBA color;

	public DAELight(Light light, DAELightType type) {
		this.type = type;
		this.name = light.name;
		switch (type) {
			case AMBIENT:
				color = light.ambientColor;
				break;
			case DIRECTIONAL:
				color = light.diffuseColor;
				break;
			case POINT:
				color = light.specular1Color;
				break;
		}
	}

	public DAELight(Element e) {
		id = e.getAttribute("id");
		name = e.getAttribute("name");

		Element tech = XmlFormat.getParamElement(e, "technique_common");
		if (tech != null) {
			Element lightElem = null;

			for (DAELightType t : DAELightType.values()) {
				lightElem = XmlFormat.getParamElement(tech, t.tagName);
				if (lightElem != null) {
					type = t;
					break;
				}
			}

			if (lightElem != null) {
				Element colElem = XmlFormat.getParamElement(lightElem, "color");
				if (colElem != null) {
					color = XmlFormat.getFPRGBAValue(colElem);
					color.a = 255;
				}
			}
		}
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("light");
		XmlFormat.setAttributeNonNull(e, "id", id);
		XmlFormat.setAttributeNonNull(e, "name", name);

		Element tech = doc.createElement("technique_common");

		if (type != null) {
			Element lightElem = doc.createElement(type.tagName);
			if (color != null) {
				XmlFormat.setParamNode(doc, lightElem, "color", XmlFormat.getFPRGB(color));
			}
			tech.appendChild(lightElem);
		}

		e.appendChild(tech);

		return e;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	public static enum DAELightType {
		AMBIENT("ambient"),
		DIRECTIONAL("directional"),
		POINT("point"),
		SPOT("spot");

		public final String tagName;

		private DAELightType(String tagName) {
			this.tagName = tagName;
		}
	}
}
