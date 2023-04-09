package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import xstandard.math.vec.AbstractVector;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAESource implements DAEIDAble, DAESerializable {

	public String id;

	public DAEAccessor accessor;

	public DAESource(Element elem) {
		id = elem.getAttribute("id");

		Map<String, Object> srcArrays = new HashMap<>();

		List<Element> floatArrayElems = XmlFormat.getElementsByTagName(elem, "float_array");
		List<Element> nameArrayElems = XmlFormat.getElementsByTagName(elem, "Name_array");
		nameArrayElems.addAll(XmlFormat.getElementsByTagName(elem, "IDREF_array"));

		for (Element floatArray : floatArrayElems) {
			float[] array = Arrays.copyOf(XmlFormat.getFloatArrayValue(elem), XmlFormat.getIntAttribute(floatArray, "count"));

			srcArrays.put(floatArray.getAttribute("id"), array);
		}

		for (Element nameArray : nameArrayElems) {
			String[] array = new String[XmlFormat.getIntAttribute(nameArray, "count")];

			String[] values = nameArray.getTextContent().split("\\s+");

			int actualCount = Math.min(array.length, values.length);

			int inputIndex = 0;
			for (int i = 0; i < actualCount && inputIndex < values.length; i++, inputIndex++) {
				while (values[inputIndex].length() == 0) {
					inputIndex++;
				}
				array[i] = values[inputIndex].trim();
			}

			srcArrays.put(nameArray.getAttribute("id"), array);
		}

		Element accessorElem = XmlFormat.getElementByPath(elem, "technique_common", "accessor");
		if (accessorElem != null) {
			accessor = new DAEAccessor(accessorElem, srcArrays);
		}
	}

	public DAESource(Object contents, String... labels) {
		this(contents, null, labels);
	}

	public DAESource(Object contents, DAEAccessor.ParamFormat format, String... labels) {
		accessor = new DAEAccessor();
		int extraLabelStartIndex = 0;
		if (contents instanceof AbstractVector[]) {
			AbstractVector[] vecA = (AbstractVector[]) contents;

			int len = vecA.length;
			for (int i = 0; i < labels.length; i++) {
				DAEAccessor.Param p = new DAEAccessor.Param();

				p.format = format == null ? DAEAccessor.ParamFormat.FLOAT : format;
				float[] values = new float[len];
				for (int j = 0; j < values.length; j++) {
					values[j] = vecA[j].get(i);
				}
				p.array = values;

				accessor.params.put(labels[i], p);
			}
			extraLabelStartIndex = labels.length;
		} else if (contents instanceof String[]) {
			String[] strA = (String[]) contents;

			for (int i = 0; i < labels.length; i++) {
				DAEAccessor.Param p = new DAEAccessor.Param();

				p.format = format == null ? DAEAccessor.ParamFormat.STRING : format;
				String[] values = new String[strA.length / labels.length];
				for (int j = 0, k = 0; j < values.length; j++, k += labels.length) {
					values[j] = strA[k];
				}
				p.array = values;

				accessor.params.put(labels[i], p);
			}
			extraLabelStartIndex = labels.length;
		} else if (contents instanceof float[]) {
			float[] fltA = (float[]) contents;

			DAEAccessor.Param p = new DAEAccessor.Param();
			p.array = fltA;
			p.format = format == null ? DAEAccessor.ParamFormat.FLOAT : format;

			accessor.params.put(labels[0], p);
			extraLabelStartIndex = 1;
		} else if (contents instanceof int[]) {
			int[] intA = (int[]) contents;

			DAEAccessor.Param p = new DAEAccessor.Param();
			p.array = intA;
			p.format = DAEAccessor.ParamFormat.INTEGER;

			accessor.params.put(labels[0], p);
			extraLabelStartIndex = 1;
		} else if (contents instanceof Matrix4[]) {
			Matrix4[] matrices = (Matrix4[]) contents;
			float[] floats = new float[matrices.length * 16];

			for (int i = 0, j = 0; i < matrices.length; i++, j += 16) {
				matrices[i].getRowMajor(floats, j);
			}

			DAEAccessor.Param p = new DAEAccessor.Param();
			p.array = floats;
			p.format = DAEAccessor.ParamFormat.FLOAT4x4;

			accessor.params.put(labels[0], p);
			extraLabelStartIndex = 1;
		} else {
			throw new RuntimeException("Incompatible content type " + contents);
		}

		if (!accessor.params.isEmpty()) {
			for (int el = extraLabelStartIndex; el > labels.length; el++) {
				DAEAccessor.Param p = new DAEAccessor.Param();
				p.array = null;
				p.format = new ArrayList<>(accessor.params.values()).get(0).format;
			}
		}
	}

	@Override
	public Element createElement(Document doc) {
		Element elem = doc.createElement("source");
		elem.setAttribute("id", id);
		accessor.sourceId = XmlFormat.makeSafeId(id + "-array");
		elem.appendChild(accessor.createValuesElement(doc));
		Element tech = doc.createElement("technique_common");
		tech.appendChild(accessor.createElement(doc));
		elem.appendChild(tech);
		return elem;
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
