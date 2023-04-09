package ctrmap.formats.generic.collada;

import ctrmap.formats.generic.collada.structs.DAEEnumMapper;
import xstandard.fs.FSFile;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec3f;
import xstandard.text.FormattingUtils;
import xstandard.util.collections.FloatList;
import xstandard.util.collections.IntList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlFormat {

	public static Document createDocument() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			return builder.newDocument();
		} catch (ParserConfigurationException ex) {
			Logger.getLogger(XmlFormat.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static void writeDocumentToFile(Document doc, FSFile dest) {
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);
			OutputStream out = dest.getNativeOutputStream();
			StreamResult result = new StreamResult(out);
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "2");
			transformer.transform(source, result);
			out.close();
		} catch (TransformerException | IOException ex) {
			Logger.getLogger(XmlFormat.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static Element getNormalizedRoot(FSFile f) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			InputStream in = f.getNativeInputStream();
			Document doc = builder.parse(in);
			in.close();

			Element documentRoot = doc.getDocumentElement();
			documentRoot.normalize();
			return documentRoot;
		} catch (SAXException | IOException | ParserConfigurationException ex) {
			Logger.getLogger(XmlFormat.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	public static String makeSafeId(String id) {
		if (id.length() < 64) {
			return id;
		}
		return id.substring(id.length() - 63); //the limit is 63 characters because of URL #s
	}

	public static void setAttributeNonMinus1(Element elem, String attrName, int value) {
		if (value != -1) {
			elem.setAttribute(attrName, String.valueOf(value));
		}
	}

	public static void setAttributeNonNull(Element elem, String attrName, Object value) {
		if (value != null) {
			elem.setAttribute(attrName, String.valueOf(value));
		}
	}

	public static void setParamNode(Document doc, Element parent, String paramName, Object value) {
		if (value != null) {
			Element e = doc.createElement(paramName);
			e.setTextContent(String.valueOf(value));
			parent.appendChild(e);
		}
	}

	public static void setParamNodeWithSID(Document doc, Element parent, String paramName, Object value) {
		if (value != null) {
			Element e = doc.createElement(paramName);
			e.setAttribute("sid", paramName);
			e.setTextContent(String.valueOf(value));
			parent.appendChild(e);
		}
	}

	public static String sanitizeName(String name) {
		if (name == null) {
			return "Null";
		}
		return FormattingUtils.getStrWithoutNonAlphanumeric(name, '-', '/');
	}

	public static String getFPRGBA(RGBA rgba) {
		return rgba.getR() + " " + rgba.getG() + " " + rgba.getB() + " " + rgba.getA();
	}
	
	public static String getFPRGB(RGBA rgba) {
		return rgba.getR() + " " + rgba.getG() + " " + rgba.getB();
	}

	public static String getVec3(Vec3f vec) {
		return vec.x + " " + vec.y + " " + vec.z;
	}

	public static String getMat4(Matrix4 mat) {
		StringBuilder out = new StringBuilder();
		for (int y = 0; y < 4; y++) {
			for (int x = 0; x < 4; x++) {
				if (x != 0 || y != 0) {
					out.append(" ");
				}
				out.append(mat.getRowColumn(y, x));
			}
		}
		return out.toString();
	}

	public static String getList(List l) {
		StringBuilder out = new StringBuilder();
		boolean nf = false;
		for (Object e : l) {
			if (nf) {
				out.append(" ");
			} else {
				nf = true;
			}
			out.append(e.toString());
		}
		return out.toString();
	}
	
	public static String getIntList(IntList l) {
		StringBuilder out = new StringBuilder();
		boolean nf = false;
		for (int i = 0; i < l.size(); i++) {
			if (nf) {
				out.append(" ");
			} else {
				nf = true;
			}
			out.append(l.get(i));
		}
		return out.toString();
	}

	public static Element createSimpleTextContentElem(Document doc, String tag, String content) {
		Element e = doc.createElement(tag);
		e.setTextContent(content);
		return e;
	}

	public static <E extends Enum> void setParamNodeEnum(Document doc, Element parent, String tag, E value, DAEEnumMapper<E> mapper) {
		String n = mapper.getName(value);
		if (n != null) {
			parent.appendChild(createSimpleTextContentElem(doc, tag, n));
		}
	}

	public static String getParamNodeValue(Element parent, String... path) {
		Element target = getElementByPath(parent, path);
		if (target != null) {
			return target.getTextContent().trim();
		}
		return null;
	}

	public static float getParamNodeValueFloat(Element parent, float defaultValue, String... path) {
		String text = getParamNodeValue(parent, path);
		float v = defaultValue;
		if (text != null) {
			try {
				v = Float.parseFloat(text);
			} catch (NumberFormatException ex) {

			}
		}
		return v;
	}

	public static <E extends Enum> E getParamNodeEnum(Element parent, String paramName, DAEEnumMapper<E> mapper) {
		return mapper.getValue(getParamNodeValue(parent, paramName));
	}

	public static <E extends Enum> E getAttributeEnum(Element parent, String attrName, DAEEnumMapper<E> mapper) {
		String val = parent.getAttribute(attrName);
		return mapper.getValue(val);
	}

	public static String getParamNodeValue(Element parent, String paramName) {
		Node node = getParamNode(parent, paramName);
		if (node == null) {
			return null;
		}
		return node.getTextContent();
	}

	public static Node getParamNode(Element parent, String paramName) {
		List<Node> params = getNodesByTagName(parent, paramName);
		if (params.isEmpty()) {
			return null;
		}
		return params.get(0);
	}

	public static Element getParamElement(Element parent, String paramName) {
		return (Element) getParamNode(parent, paramName);
	}

	public static String getNodeAttrib(Node node, String attribName) {
		return node.getAttributes().getNamedItem(attribName).getTextContent();
	}

	public static Node getNodeByPath(Element parent, String... path) {
		Element e = parent;
		for (String p : path) {
			e = getParamElement(e, p);
			if (e == null) {
				break;
			}
		}
		return e;
	}

	public static int[] getIntArrayValue(Element parent) {
		String[] values = parent.getTextContent().split("\\s+");

		List<Integer> l = new ArrayList<>();

		for (int inputIndex = 0; inputIndex < values.length; inputIndex++) {
			if (values[inputIndex].length() == 0) {
				continue;
			}
			l.add(Integer.parseInt(values[inputIndex].trim()));
		}
		int[] arr = new int[l.size()];
		for (int i = 0; i < arr.length; i++) {
			arr[i] = l.get(i);
		}
		return arr;
	}

	public static RGBA getFPRGBAValue(Element elem) {
		return getFPRGBAValue(elem, null);
	}

	public static RGBA getFPRGBAValue(Element elem, RGBA defVal) {
		if (elem == null) {
			return defVal;
		}
		return getFPRGBAValue(elem.getTextContent(), defVal);
	}
	
	public static RGBA getFPRGBAValue(String str, RGBA defVal) {
		if (str == null) {
			return defVal;
		}
		float[] floats = getFloatArrayValue(str);
		return new RGBA(floats, floats.length);
	}

	private static float parseFloat(String str) {
		return Float.parseFloat(str.replace(',', '.'));
	}

	public static float[] getFloatArrayValue(Element parent) {
		return getFloatArrayValue(parent.getTextContent());
	}
	
	public static float[] getFloatArrayValue(String textContent) {
		String[] values = textContent.split("\\s+");

		FloatList l = new FloatList();

		for (int inputIndex = 0; inputIndex < values.length; inputIndex++) {
			if (values[inputIndex].length() == 0) {
				continue;
			}
			l.add(parseFloat(values[inputIndex].trim()));
		}
		return l.toArray();
	}

	public static List<Element> getElementsByPath(Element parent, String... path) {
		return getElementList(getNodesByPath(parent, path));
	}

	public static List<Node> getNodesByPath(Element parent, String... path) {
		Element e = parent;
		for (String p : path) {
			if (p.equals(path[path.length - 1])) {
				return nodeListToListOfNodes(e.getElementsByTagName(p));
			} else {
				e = getParamElement(e, p);
			}
			if (e == null) {
				break;
			}
		}
		return new ArrayList<>();
	}
	
	public static <E extends Enum> E getElementAttribEnumByPath(Element parent, DAEEnumMapper<E> mapper, String attrib, String... path) {
		Element e = getElementByPath(parent, path);
		if (e != null) {
			return getAttributeEnum(e, attrib, mapper);
		}
		return mapper.getValue(null);
	}
	
	public static String getElementAttribByPath(Element parent, String attrib, String... path) {
		Element e = getElementByPath(parent, path);
		if (e != null) {
			return e.getAttribute(attrib);
		}
		return null;
	}

	public static Element getElementByPath(Element parent, String... path) {
		return (Element) getNodeByPath(parent, path);
	}

	public static List<Element> getLibraryContentDataElems(Element libraryParent, String libraryName, String tagName) {
		return getElementList(getLibraryContentData(getElementList(getNodesByTagName(libraryParent, libraryName)), tagName));
	}

	public static List<Node> getLibraryContentData(Element libraryParent, String libraryName, String tagName) {
		return getLibraryContentData(getElementList(getNodesByTagName(libraryParent, libraryName)), tagName);
	}

	public static List<Node> getLibraryContentData(List<Element> libraryHeaders, String tagName) {
		List<Node> r = new ArrayList<>();
		for (Element h : libraryHeaders) {
			for (Node n : nodeListToListOfNodes(h.getElementsByTagName(tagName))) {
				if (n.getParentNode() == h) {
					r.add(n);
				}
			}
		}
		return r;
	}

	public static List<Node> getNodesByTagName(Element parent, String libraryName) {
		NodeList nl = parent.getElementsByTagName(libraryName);
		List<Node> nll = nodeListToListOfNodes(nl);
		List<Node> r = new ArrayList<>();
		for (Node n : nll) {
			if (n.getParentNode().isSameNode(parent)) {
				r.add(n);
			}
		}
		return r;
	}

	public static List<Element> getElementsByTagName(Element parent, String libraryName) {
		return getElementList(getNodesByTagName(parent, libraryName));
	}

	public static boolean getAttributeBool(Element elem, String attrName) {
		if (elem.hasAttribute(attrName)) {
			return elem.getAttribute(attrName).equals("true");
		}
		return false;
	}

	public static String getAttribute(Element elem, String attrName) {
		if (elem.hasAttribute(attrName)) {
			return elem.getAttribute(attrName);
		}
		return null;
	}

	public static int getIntAttribute(Element elem, String attrName) {
		String a = getAttribute(elem, attrName);
		if (a == null) {
			return -1;
		}
		return Integer.parseInt(a);
	}

	public static float getFloatAttribute(Element elem, String attrName) {
		return Float.parseFloat(elem.getAttribute(attrName));
	}

	public static Element getByAttribute(List<Element> elems, String attName, String attValue) {
		for (Element e : elems) {
			if (Objects.equals(attValue, e.getAttribute(attName))) {
				return e;
			}
		}
		return null;
	}

	public static List<Node> nodeListToListOfNodes(NodeList nl) {
		List<Node> r = new ArrayList<>();
		for (int i = 0; i < nl.getLength(); i++) {
			r.add(nl.item(i));
		}
		return r;
	}

	public static List<Element> getElementList(List<Node> nl) {
		List<Element> elems = new ArrayList<>();
		for (Node node : nl) {
			if (node instanceof Element) {
				elems.add((Element) node);
			}
		}
		return elems;
	}
}
