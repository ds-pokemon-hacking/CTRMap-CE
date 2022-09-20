package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEInstance implements DAESerializable, DAEIDAble {

	public InstanceType type;

	public String name;
	public String url;

	public String targetSymbol;
	public String symbolReplacement;

	public String sklRootURL;

	public List<DAEBind> binds = new ArrayList<>();

	public DAEInstance(Element elem) {
		String fn = elem.getTagName();
		for (InstanceType t : InstanceType.values()) {
			if (t.hasFN(fn)) {
				type = t;
				break;
			}
		}

		name = elem.getAttribute("name");
		targetSymbol = elem.getAttribute("symbol");
		symbolReplacement = elem.getAttribute("target");
		url = elem.getAttribute("url");

		for (Element bindElem : XmlFormat.getElementList(XmlFormat.nodeListToListOfNodes(elem.getChildNodes()))) {
			if (bindElem.getTagName().startsWith("bind_")) {
				binds.add(new DAEBind(bindElem));
			}
		}
	}

	public DAEInstance(InstanceType type, DAEIDAble instanceElem) {
		this.type = type;
		url = instanceElem.getURL();
	}

	public DAEInstance(InstanceType type, DAEIDAble instanceElem, String sklRootURL) {
		this(type, instanceElem);
		this.sklRootURL = sklRootURL;
	}

	@Override
	public Element createElement(Document doc) {
		Element elem = doc.createElement(type.fn);
		elem.setAttribute("url", url);
		XmlFormat.setAttributeNonNull(elem, "name", name);
		XmlFormat.setAttributeNonNull(elem, "symbol", targetSymbol);
		XmlFormat.setAttributeNonNull(elem, "target", symbolReplacement);
		if (sklRootURL != null) {
			elem.appendChild(XmlFormat.createSimpleTextContentElem(doc, "skeleton", sklRootURL));
		}

		for (DAEBind b : binds) {
			elem.appendChild(b.createElement(doc));
		}

		return elem;
	}

	@Override
	public String getID() {
		return targetSymbol;
	}

	@Override
	public void setID(String id) {
		this.targetSymbol = id;
	}

	public static enum InstanceType {
		GEOMETRY("instance_geometry"),
		CONTROLLER("instance_controller"),
		MATERIAL("instance_material"),
		EFFECT("instance_effect"),
		CAMERA("instance_camera"),
		LIGHT("instance_light"),
		VISUAL_SCENE("instance_visual_scene");

		private String fn;

		private InstanceType(String friendlyName) {
			fn = friendlyName;
		}

		public boolean hasFN(String fn) {
			return fn.equals(this.fn);
		}
	}
}
