package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import xstandard.math.vec.Vec4f;
import xstandard.math.MatrixUtil;
import xstandard.math.vec.Matrix4;
import xstandard.util.ArraysEx;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEAccessor implements DAESerializable {

	public String sourceId;

	public Map<String, Param> params = new LinkedHashMap<>();

	public DAEAccessor() {

	}

	public DAEAccessor(Element elem, Map<String, Object> sourceArrays) {
		String sourceURL = elem.getAttribute("source");
		sourceId = sourceURL.substring(1);

		Object src = sourceArrays.get(sourceId);

		List<Element> paramElems = XmlFormat.getElementsByTagName(elem, "param");

		int stride = 1;
		if (elem.hasAttribute("stride")) {
			stride = XmlFormat.getIntAttribute(elem, "stride");
		}
		int count = XmlFormat.getIntAttribute(elem, "count");

		int paramIdx = 0;
		for (Element param : paramElems) {
			Param p = new Param();

			String formatName = param.getAttribute("type");

			for (ParamFormat fmt : ParamFormat.values()) {
				if (fmt.accepts(formatName)) {
					p.format = fmt;
					break;
				}
			}

			if (p.format != null) {
				switch (p.format) {
					case FLOAT: {
						float[] data = new float[count];
						float[] srcData = (float[]) src;
						int offs = paramIdx;
						for (int i = 0; i < count; i++) {
							if (offs >= srcData.length) {
								throw new ArrayIndexOutOfBoundsException("Out of bounds index " + offs + " for source " + sourceURL + " count " + count + " stride " + stride + " actlen " + srcData.length + " outlen " + data.length);
							}
							data[i] = srcData[offs];
							offs += stride;
						}
						p.array = data;
						break;
					}
					case FLOAT4x4: {
						Matrix4[] matrices = new Matrix4[count];
						float[] srcData = (float[]) src;
						int offs = paramIdx;
						for (int i = 0; i < count; i++) {
							matrices[i] = Matrix4.createRowMajor(Arrays.copyOfRange(srcData, offs, offs + 4 * 4));
							offs += stride;
						}
						p.array = matrices;
						break;
					}
					case STRING:
					case IDREF: {
						String[] names = new String[count];
						String[] srcData = (String[]) src;
						int offs = paramIdx;
						for (int i = 0; i < count; i++) {
							names[i] = srcData[offs];
							offs += stride;
						}
						p.array = names;
						break;
					}
				}

				String prmName = param.getAttribute("name");

				params.put(prmName == null ? "NullParam" : prmName, p);
			}

			if (paramIdx + 1 < stride) {
				paramIdx++;
			}
		}
	}

	public boolean hasParams(String... params) {
		for (String p : params) {
			if (!this.params.containsKey(p)) {
				return false;
			}
		}
		return true;
	}

	public boolean hasParamTypes(ParamFormat... params) {
		List<ParamFormat> fmts = new ArrayList<>();
		for (Param p : this.params.values()) {
			fmts.add(p.format);
		}
		for (ParamFormat p : params) {
			if (!fmts.contains(p)) {
				return false;
			}
		}
		return true;
	}

	public List<Matrix4> getMatrix4Array() {
		if (hasParamTypes(ParamFormat.FLOAT4x4)) {
			for (Param p : params.values()) {
				if (p.format == ParamFormat.FLOAT4x4) {
					return ArraysEx.asList((Matrix4[]) p.array);
				}
			}
		}
		throw new UnsupportedOperationException("Not a matrix source.");
	}

	private List<Vec4f> getAbstractVecArray() {
		List<Vec4f> l = new ArrayList<>();

		Param paramX = getParam("X", "S", "U", "R");
		Param paramY = getParam("Y", "T", "V", "G");
		Param paramZ = getParam("Z", "B");
		Param paramW = getParam("W", "A");

		float[] xa = paramX != null ? (float[]) paramX.array : null;
		float[] ya = paramY != null ? (float[]) paramY.array : null;
		float[] za = paramZ != null ? (float[]) paramZ.array : null;
		float[] wa = paramW != null ? (float[]) paramW.array : null;

		if (xa == null || ya == null) {
			throw new UnsupportedOperationException("Vectors need at least 2 components.");
		}

		for (int i = 0; i < xa.length; i++) {
			float x = xa[i];
			float y = ya[i];
			float z = za != null ? za[i] : 0f;
			float w = wa != null ? wa[i] : 1f;
			l.add(new Vec4f(x, y, z, w));
		}
		return l;
	}

	public String[] getStringArray() {
		if (hasParamTypes(ParamFormat.STRING) || hasParamTypes(ParamFormat.IDREF)) {
			for (Param p : params.values()) {
				if (p.format == ParamFormat.STRING || p.format == ParamFormat.IDREF) {
					return (String[]) p.array;
				}
			}
		}
		throw new UnsupportedOperationException("Not a name source.");
	}

	public List<String> getStringList() {
		if (hasParamTypes(ParamFormat.STRING) || hasParamTypes(ParamFormat.IDREF)) {
			return ArraysEx.asList(getStringArray());
		}
		throw new UnsupportedOperationException("Not a name source.");
	}

	public float[] getFloatArray() {
		if (hasParamTypes(ParamFormat.FLOAT)) {
			for (Param p : params.values()) {
				if (p.format == ParamFormat.FLOAT) {
					return (float[]) p.array;
				}
			}
		}
		throw new UnsupportedOperationException("Not a float source.");
	}

	public Vec2f[] getVec2fArray() {
		List<Vec4f> src = getAbstractVecArray();
		Vec2f[] dst = new Vec2f[src.size()];
		for (int i = 0; i < src.size(); i++) {
			dst[i] = src.get(i).toVec2();
		}
		return dst;
	}

	public List<Vec2f> getVec2fList() {
		List<Vec4f> src = getAbstractVecArray();
		List<Vec2f> dst = new ArrayList<>(src.size());
		for (Vec4f s : src) {
			dst.add(s.toVec2());
		}
		return dst;
	}

	public List<Vec3f> getVec3fArray() {
		List<Vec4f> src = getAbstractVecArray();
		List<Vec3f> dst = new ArrayList<>();
		for (Vec4f s : src) {
			dst.add(s.toVec3());
		}
		return dst;
	}

	public List<Vec4f> getVec4fArray() {
		return getAbstractVecArray();
	}

	public List<RGBA> getRGBAArray() {
		List<Vec4f> src = getAbstractVecArray();
		List<RGBA> dst = new ArrayList<>();
		for (Vec4f s : src) {
			dst.add(new RGBA(s));
		}
		return dst;
	}

	private Param getParam(String... possibleNames) {
		for (String name : possibleNames) {
			Param key = params.get(name);
			if (key != null) {
				return key;
			}
		}
		return null;
	}

	@Override
	public Element createElement(Document doc) {
		Element e = doc.createElement("accessor");
		e.setAttribute("source", "#" + sourceId);

		int stride = 0;
		int count = 0;
		for (Param p : params.values()) {
			if (p.array != null) {
				stride += p.format.stride;
				count += Array.getLength(p.array);
			}
		}
		count /= stride;

		e.setAttribute("stride", String.valueOf(stride));
		e.setAttribute("count", String.valueOf(count));

		for (Map.Entry<String, Param> p : params.entrySet()) {
			Element param = doc.createElement("param");
			param.setAttribute("name", p.getKey());
			param.setAttribute("type", p.getValue().format.fNames.get(0));
			e.appendChild(param);
		}

		return e;
	}

	public Element createValuesElement(Document doc) {
		String arrayfmt = null;

		int elemCount = Integer.MAX_VALUE;

		if (params.isEmpty()) {
			throw new RuntimeException("Empty params.");
		}

		for (Param p : params.values()) {
			if (p.array != null) {
				String newArrayfmt = null;
				switch (p.format) {
					case FLOAT:
					case FLOAT4x4:
						newArrayfmt = "float_array";
						break;
					case INTEGER:
						newArrayfmt = "int_array";
						break;
					case STRING:
						newArrayfmt = "Name_array";
						break;
					case IDREF:
						newArrayfmt = "IDREF_array";
						break;
				}
				if (newArrayfmt != null) {
					if (arrayfmt != null && !Objects.equals(arrayfmt, newArrayfmt)) {
						throw new RuntimeException("Inconsistent accessor param types - " + arrayfmt + " x " + newArrayfmt);
					}
					arrayfmt = newArrayfmt;
				} else {
					throw new RuntimeException("Invalid content " + p.array);
				}
				elemCount = Math.min(elemCount, Array.getLength(p.array));
			}
		}
		if (arrayfmt == null) {
			throw new RuntimeException("Invalid arrayfmt");
		}

		StringBuilder out = new StringBuilder();

		boolean first = true;

		for (int i = 0; i < elemCount; i++) {
			for (Param p : params.values()) {
				if (!first) {
					out.append(" ");
				} else {
					first = false;
				}
				out.append(String.valueOf(Array.get(p.array, i)));
			}
		}

		Element elem = XmlFormat.createSimpleTextContentElem(doc, arrayfmt, out.toString());
		elem.setAttribute("count", String.valueOf(elemCount * params.size()));
		elem.setAttribute("id", sourceId);

		return elem;
	}

	public static class Param {

		public ParamFormat format;
		public Object array;
	}

	public enum ParamFormat {
		FLOAT(1, "float", "double"),
		INTEGER(1, "int"),
		FLOAT4x4(16, "float4x4"),
		STRING(1, "name", "Name"),
		IDREF(1, "IDREF");

		private final List<String> fNames = new ArrayList<>();
		private final int stride;

		private ParamFormat(int stride, String... friendlyNames) {
			this.stride = stride;
			fNames.addAll(ArraysEx.asList(friendlyNames));
		}

		public boolean accepts(String fName) {
			return fNames.contains(fName);
		}
	}

	public enum DetectedSourceFormat {
		VECTOR4F,
		VECTOR3F,
		VECTOR2F,
		MATRIX4x4F
	}
}
