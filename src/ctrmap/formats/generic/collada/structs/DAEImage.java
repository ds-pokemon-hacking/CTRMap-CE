package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.util.texture.TextureConverter;
import xstandard.fs.FSFile;
import xstandard.fs.accessors.DiskFile;
import xstandard.gui.file.CommonExtensionFilters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEImage implements DAEIDAble, DAESerializable {

	private String id;
	public String name;
	public String initFrom;
	private boolean isLUT;

	public Texture texture;

	public DAEImage(Element elem) {
		id = elem.getAttribute("id");
		if (elem.hasAttribute("name")) {
			name = elem.getAttribute("name");
		} else {
			name = id;
		}

		Element init_from = XmlFormat.getParamElement(elem, "init_from");
		if (init_from != null) {
			Element ref = XmlFormat.getParamElement(init_from, "ref");
			if (ref != null) {
				initFrom = ref.getTextContent();
			} else {
				initFrom = init_from.getTextContent();
			}
		}
		
		Element extra = XmlFormat.getParamElement(elem, "extra");
		if (extra != null) {
			Element lutTech = XmlFormat.getByAttribute(XmlFormat.getElementsByTagName(extra, "technique"), "profile", "CtrLUTFragLight");
			if (lutTech != null) {
				String isLUTValue = XmlFormat.getParamNodeValue(lutTech, "IsLUTTexture");
				isLUT = isLUTValue != null && isLUTValue.equals(Boolean.TRUE.toString());
			}
		}
	}

	public DAEImage(Texture tex) {
		if (tex != null) {
			this.texture = tex;
			name = XmlFormat.sanitizeName(tex.name);
			if (!name.endsWith(CommonExtensionFilters.PNG.getPrimaryExtension())) {
				initFrom = name + CommonExtensionFilters.PNG.getPrimaryExtension();
			} else {
				initFrom = name;
			}
			isLUT = ReservedMetaData.isLUT(tex);
		}
	}

	@Override
	public Element createElement(Document doc) {
		Element elem = doc.createElement("image");
		elem.setAttribute("id", id);
		elem.setAttribute("name", name);
		elem.appendChild(XmlFormat.createSimpleTextContentElem(doc, "init_from", initFrom));
		if (isLUT) {
			Element extra = doc.createElement("extra");
			Element extraTech = doc.createElement("technique");
			extraTech.setAttribute("profile", "CtrLUTFragLight");
			XmlFormat.setParamNode(doc, extraTech, "IsLUTTexture", "true");
			extra.appendChild(extraTech);
			elem.appendChild(extra);
		}
		return elem;
	}

	public Texture toTexture(FSFile basePath) {
		FSFile file = basePath.getChild(initFrom);
		if (!file.exists()) {
			file = new DiskFile(initFrom);
		}
		if (file.exists()) {
			try {
				texture = TextureConverter.readTextureFromFile(file);
				texture.name = name;
				if (isLUT) {
					texture.metaData.putValue(ReservedMetaData.TEX_AS_LUT, true);
				}
				return texture;
			} catch (Exception e) {
				System.err.println("Exception while importing texture " + initFrom);
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}
}
