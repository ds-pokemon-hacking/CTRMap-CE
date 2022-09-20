package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import xstandard.math.vec.Vec3f;
import xstandard.util.ParsingUtils;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEMetaDataIO {

	private static final DAEEnumMapper<MetaDataValue.Type> MDV_TYPE = new DAEEnumMapper<>(
		MetaDataValue.Type.FLOAT,
		"FLOAT", MetaDataValue.Type.FLOAT,
		"INT", MetaDataValue.Type.INT,
		"STRING", MetaDataValue.Type.STRING,
		"VECTOR3", MetaDataValue.Type.VEC3,
		"RAW_BUFFER", MetaDataValue.Type.RAW_BYTES
	);

	public static void readMetaData(Element mdTech, MetaData dest) {
		for (Element metavalue : XmlFormat.getElementsByTagName(mdTech, "value")) {
			String name = metavalue.getAttribute("name");
			if (name != null) {
				MetaDataValue.Type type = XmlFormat.getAttributeEnum(metavalue, "type", MDV_TYPE);
				if (type != null) {
					List<Element> valueElems = XmlFormat.getElementsByTagName(metavalue, getValElemTag(type));

					List<Object> values = new ArrayList<>();

					for (Element e : valueElems) {
						String src = e.getTextContent().trim();

						if (!src.isEmpty()) {
							switch (type) {
								case FLOAT:
									values.add(Float.parseFloat(src));
									break;
								case INT:
									values.add(ParsingUtils.parseBasedInt(src));
									break;
								case VEC3:
									values.add(new Vec3f(XmlFormat.getFloatArrayValue(src)));
									break;
								case STRING:
									values.add(src);
									break;
								case RAW_BYTES:
									int len = XmlFormat.getIntAttribute(e, "size");
									if (len != -1) {
										byte[] bytes = new byte[len];
										
										int charCount = Math.min(len * 2, src.length());
										for (int cidx = 0, bidx = 0; cidx < charCount; cidx += 2, bidx++) {
											bytes[bidx] = (byte)((fromHexChar(src.charAt(cidx)) << 4) | fromHexChar(src.charAt(cidx + 1)));
										}
										
										values.add(bytes);
									}
									break;
							}
						}
					}

					if (!values.isEmpty()) {
						dest.putValue(new MetaDataValue(name, values));
					}
				}
			}
		}
	}

	public static Element writeMetaData(Document doc, MetaData md) {
		Element e = doc.createElement("technique");
		e.setAttribute("profile", "CSMetaData");

		for (MetaDataValue metaval : md.getWriteableValues()) {
			Element metavalElem = doc.createElement("value");
			metavalElem.setAttribute("name", metaval.getName());
			MetaDataValue.Type t = metaval.getType();
			metavalElem.setAttribute("type", MDV_TYPE.getName(t));

			for (Object value : metaval.getValues()) {
				Element valElem = doc.createElement(getValElemTag(t));

				String cnt = null;

				switch (t) {
					case FLOAT:
					case INT:
					case STRING:
						cnt = String.valueOf(value);
						break;
					case VEC3:
						cnt = XmlFormat.getVec3((Vec3f) value);
						break;
					case RAW_BYTES:
						StringBuilder sb = new StringBuilder();
						byte[] bytes = (byte[]) value;
						valElem.setAttribute("size", String.valueOf(bytes.length));
						int val;
						for (int i = 0; i < bytes.length; i++) {
							val = bytes[i] & 0xFF;
							sb.append(toHexChar(val >> 4));
							sb.append(toHexChar(val & 15));
						}
						cnt = sb.toString();
						break;
				}

				valElem.setTextContent(cnt);
				metavalElem.appendChild(valElem);
			}

			e.appendChild(metavalElem);
		}

		return e;
	}
	
	private static byte fromHexChar(char value) {
		if (value >= '0' && value <= '9') {
			return (byte)(value - '0');
		}
		if (value >= 'A' && value <= 'F') {
			return (byte)(value - 'A' + 10);
		}
		if (value >= 'a' && value <= 'f') {
			return (byte)(value - 'a' + 10);
		}
		return 0;
	}

	private static char toHexChar(int value) {
		if (value < 10) {
			return (char) ('0' + value);
		}
		return (char) ('A' + value - 10);
	}

	private static String getValElemTag(MetaDataValue.Type t) {
		switch (t) {
			case FLOAT:
				return "float";
			case INT:
				return "int";
			case RAW_BYTES:
			case STRING:
				return "string";
			case VEC3:
				return "float3";
		}
		throw new RuntimeException();
	}
}
