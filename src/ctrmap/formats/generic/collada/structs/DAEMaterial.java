package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.texturing.Material;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEMaterial implements DAEIDAble, DAESerializable {

	private String id;

	public String materialName;

	public DAEInstance effInstance;
	
	public MetaData metaData;

	public DAEMaterial(Element elem) {
		id = elem.getAttribute("id");
		if (elem.hasAttribute("name")) {
			materialName = elem.getAttribute("name");
		} else {
			materialName = id;
		}

		effInstance = new DAEInstance(XmlFormat.getParamElement(elem, "instance_effect"));
		
		Element extra = XmlFormat.getParamElement(elem, "extra");
		if (extra != null) {
			Element mdElem = XmlFormat.getByAttribute(XmlFormat.getElementsByTagName(extra, "technique"), "profile", "CSMetaData");
			if (mdElem != null) {
				metaData = new MetaData();
				DAEMetaDataIO.readMetaData(mdElem, metaData);
			}
		}
	}

	public DAEMaterial(Material mat, DAEEffect effect) {
		materialName = XmlFormat.sanitizeName(mat.name);
		effInstance = new DAEInstance(DAEInstance.InstanceType.EFFECT, effect);
		metaData = mat.metaData;
	}

	public void applyToMaterial(Material mat) {
		mat.name = materialName;
		if (metaData != null) {
			mat.metaData = metaData;
		}
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("material");
		e.setAttribute("id", id);
		e.setAttribute("name", materialName);
		e.appendChild(effInstance.createElement(doc));
		
		if (!metaData.isEmpty()) {
			Element extra = doc.createElement("extra");
			
			extra.appendChild(DAEMetaDataIO.writeMetaData(doc, metaData));
			
			e.appendChild(extra);
		}
		
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
}
