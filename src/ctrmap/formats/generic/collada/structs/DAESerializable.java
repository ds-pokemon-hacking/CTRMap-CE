package ctrmap.formats.generic.collada.structs;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface DAESerializable {
	public Element createElement(Document doc);
}
