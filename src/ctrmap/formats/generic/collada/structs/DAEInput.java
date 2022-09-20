package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEInput implements DAEIDAble, DAESerializable {

	public String semantic;

	public String sourceUrl;
	public int offset = 0;
	public int setNo = 0;

	public DAEInput(String semantic, DAESource source, int offset) {
		this(semantic, source, offset, -1);
	}
	
	public DAEInput(String semantic, String source, int offset) {
		this(semantic, "#" + source, offset, -1);
	}

	public DAEInput(String semantic, DAESource source, int offset, int setNo) {
		this(semantic, source.getURL(), offset, setNo);
	}
	
	public DAEInput(String semantic, String sourceURL, int offset, int setNo) {
		this.semantic = semantic;
		sourceUrl = sourceURL;
		this.offset = offset;
		this.setNo = setNo;
	}

	public DAEInput(Element elem) {
		semantic = elem.getAttribute("semantic");
		sourceUrl = elem.getAttribute("source");
		if (elem.hasAttribute("offset")) {
			offset = Integer.parseInt(elem.getAttribute("offset"));
		}
		if (elem.hasAttribute("set")) {
			setNo = Integer.parseInt(elem.getAttribute("set"));
		}
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("input");
		e.setAttribute("semantic", semantic);
		e.setAttribute("source", sourceUrl);
		XmlFormat.setAttributeNonMinus1(e, "offset", offset);
		XmlFormat.setAttributeNonMinus1(e, "set", setNo);
		return e;
	}

	@Override
	public String getID() {
		return semantic;
	}

	@Override
	public void setID(String id) {
		this.semantic = id;
	}
}
