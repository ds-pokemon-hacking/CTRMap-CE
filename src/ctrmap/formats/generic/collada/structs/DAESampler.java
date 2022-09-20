package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAESampler implements DAEIDAble, DAESerializable {

	public String id;

	public DAEDict<DAEInput> inputs = new DAEDict<>();
	
	public DAESampler() {
		
	}

	public DAESampler(Element elem) {
		id = elem.getAttribute("id");
		List<Element> inList = XmlFormat.getElementsByTagName(elem, "input");
		for (Element in : inList) {
			inputs.putNode(new DAEInput(in));
		}
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("sampler");
		e.setAttribute("id", id);
		for (DAEInput in : inputs) {
			e.appendChild(in.createElement(doc));
		}
		return e;
	}
}
