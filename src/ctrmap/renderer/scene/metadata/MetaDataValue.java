package ctrmap.renderer.scene.metadata;

import xstandard.INamed;
import xstandard.io.util.IOUtils;
import xstandard.math.vec.Vec3f;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MetaDataValue implements INamed {

	private boolean isUniform = false;
	private boolean isTransient = false;
	
	private String name;
	private List<Object> values;
	private Type type = null;

	public MetaDataValue(String name, Object value) {
		this(name, value, false);
	}

	public MetaDataValue(String name, Object value, boolean isUniform) {
		this(name, value, isUniform, false);
	}
	
	public MetaDataValue(String name, Object value, boolean isUniform, boolean isTransient) {
		this.name = name;
		this.isUniform = isUniform;
		this.isTransient = isTransient;

		if (value == null) {
			values = new ArrayList<>();
			return;
		}

		List objList = null;

		if (value instanceof List) {
			objList = (List) value;
		} else if (value.getClass().isArray()) {
			objList = new ArrayList();
			if (value instanceof Vec3f[]) {
				for (Vec3f v : (Vec3f[]) value) {
					objList.add(v);
				}
			} else if (value instanceof float[]) {
				for (float f : (float[]) value) {
					objList.add(f);
				}
			} else if (value instanceof int[]) {
				for (int i : (int[]) value) {
					objList.add(i);
				}
			} else if (value instanceof boolean[]) {
				for (boolean bln : (boolean[]) value) {
					objList.add(bln);
				}
			} else if (value instanceof byte[]) {
				objList.add(value);
			} else if (value instanceof Enum[]) {
				for (Enum e : (Enum[]) value) {
					objList.add(e);
				}
			}
		} else {
			objList = new ArrayList<>();
			objList.add(value);
		}

		for (int i = 0; i < objList.size(); i++) {
			Object v = objList.get(i);

			v = getProperlyCastNumber(v);

			MetaDataValue.Type t = getTypeForValue(v);

			if (type == null) {
				type = t;
			} else {
				if (t != type) {
					throw new UnsupportedOperationException("MetaData values can only contain objects of the same types. Expected: " + type + ", got " + t);
				}
			}

			objList.set(i, v);
		}
		this.values = objList;
	}

	public void setAsUniform(boolean v) {
		isUniform = v;
	}
	
	public void setTransient(boolean v) {
		isTransient = v;
	}

	public boolean getIsUniform() {
		return isUniform;
	}
	
	public boolean getIsTransient() {
		return isTransient;
	}

	private static Object getProperlyCastNumber(Object v) {
		if (v instanceof Byte) {
			v = ((Byte) v).intValue();
		} else if (v instanceof Short) {
			v = ((Short) v).intValue();
		} else if (v instanceof Long) {
			v = ((Long) v).intValue();
		} else if (v instanceof Boolean) {
			v = ((boolean) v) ? 1 : 0;
		} else if (v instanceof Enum) {
			v = (int) ((Enum) v).ordinal();
		}
		return v;
	}

	private static Type getTypeForValue(Object v) {
		Type t;
		if (v instanceof Integer || v instanceof Enum) {
			t = Type.INT;
		} else if (v instanceof Float) {
			t = Type.FLOAT;
		} else if (v instanceof String) {
			t = Type.STRING;
		} else if (v instanceof Vec3f) {
			t = Type.VEC3;
		} else if (v instanceof byte[]) {
			t = Type.RAW_BYTES;
		} else {
			throw new IllegalArgumentException("MetaData Object is of unsupported type: " + ((v == null) ? null : v.getClass()));
		}
		return t;
	}

	public void cast(Type t) {
		if (t == type) {
			return;
		}
		for (int v = 0; v < values.size(); v++) {
			Object val = values.get(v);

			String sV = stringValue(v);

			switch (t) {
				case STRING:
					if (type == Type.RAW_BYTES) {
						val = new String((byte[])val);
					} else {
						val = sV;
					}
					break;
				case FLOAT:
					float f;
					try {
						f = Float.parseFloat(sV);
					} catch (NumberFormatException ex) {
						f = 0f;
					}
					val = f;
					break;
				case INT:
					if (val instanceof Float) {
						val = ((Float) val).intValue();
					} else {
						int i;
						try {
							i = Integer.parseInt(sV);
						} catch (NumberFormatException ex) {
							i = 0;
						}
						val = i;
					}
					break;
				case VEC3:
					val = new Vec3f();
					break;
				case RAW_BYTES:
					switch (type) {
						case FLOAT: {
							byte[] b = new byte[Float.BYTES];
							IOUtils.integerToByteArrayLE(Float.floatToIntBits((Float) val), b, 0);
							val = b;
							break;
						}
						case INT: {
							byte[] b = new byte[Integer.BYTES];
							IOUtils.integerToByteArrayLE((Integer) val, b, 0);
							val = b;
							break;
						}
						case STRING: {
							val = ((String) val).getBytes(StandardCharsets.UTF_8);
							break;
						}
						case VEC3: {
							byte[] b = new byte[3 * Float.BYTES];
							IOUtils.floatArrayToByteArray(((Vec3f) val).toFloatUniform(), 0, b, 0, 3);
							val = b;
							break;
						}
					}
					break;
			}

			values.set(v, val);
		}
		type = t;
	}

	public void setValue(Object value) {
		setValue(0, value);
	}

	public void setValue(int index, Object value) {
		value = getProperlyCastNumber(value);
		if (getTypeForValue(value) != type) {
			throw new IllegalArgumentException("The value's class does not belong to this MetaData.");
		}
		this.values.set(index, value);
	}

	public void appendValue(Object value) {
		value = getProperlyCastNumber(value);
		if (getTypeForValue(value) != type) {
			throw new IllegalArgumentException("The value's class does not belong to this MetaData.");
		}
		this.values.add(value);
	}

	public void appendDefaultValue() {
		Object val = null;
		switch (getType()) {
			case FLOAT:
				val = 0f;
				break;
			case INT:
				val = (int) 0;
				break;
			case STRING:
				val = "Blank";
				break;
			case VEC3:
				val = new Vec3f();
				break;
			case RAW_BYTES:
				val = new byte[0];
				break;
		}
		if (val != null) {
			values.add(val);
		}
	}

	public int getValueCount() {
		return values.size();
	}

	public Type getType() {
		return type;
	}

	public List<Object> getValues() {
		return values;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public int intValue() {
		return intValue(0);
	}

	public int[] intValues() {
		int[] vals = new int[this.values.size()];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = intValue(i);
		}
		return vals;
	}

	public float[] floatValues() {
		if (type == Type.VEC3) {
			float[] vals = new float[values.size() * 3];
			for (int i = 0; i < values.size(); i++) {
				Vec3f vec = vec3Value(i);
				vals[i * 3] = vec.x;
				vals[i * 3 + 1] = vec.y;
				vals[i * 3 + 2] = vec.z;
			}
			return vals;
		} else {
			float[] vals = new float[this.values.size()];
			for (int i = 0; i < vals.length; i++) {
				vals[i] = floatValue(i);
			}
			return vals;
		}
	}

	public Vec3f[] vec3Values() {
		Vec3f[] vals = new Vec3f[this.values.size()];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = vec3Value(i);
		}
		return vals;
	}

	public int intValue(int idx) {
		switch (type) {
			case FLOAT:
				return ((Float) (values.get(idx))).intValue();
			case INT:
				return (Integer) (values.get(idx));
			default:
				warnType();
				return 0;
		}
	}

	public float floatValue() {
		return floatValue(0);
	}

	public float floatValue(int idx) {
		switch (type) {
			case FLOAT:
				return (Float) (values.get(idx));
			case INT:
				return ((Integer) (values.get(idx))).floatValue();
			default:
				warnType();
				return 0f;
		}
	}

	public Vec3f vec3Value() {
		return vec3Value(0);
	}

	public Vec3f vec3Value(int idx) {
		if (type == Type.VEC3) {
			return (Vec3f) values.get(idx);
		} else {
			warnType();
			return new Vec3f();
		}
	}

	public String stringValue() {
		return stringValue(0);
	}

	public String stringValue(int idx) {
		if (type == Type.RAW_BYTES){
			return new String((byte[])values.get(idx));
		}
		return String.valueOf((values.get(idx)));
	}
	
	public byte[] byteArrValue(){
		return byteArrValue(0);
	}
	
	public byte[] byteArrValue(int idx){
		Object val = values.get(idx);
		switch (type){
			case RAW_BYTES:
				return (byte[])val;
			case STRING:
				return ((String)val).getBytes(StandardCharsets.UTF_8);
		}
		warnType();
		return new byte[0];
	}

	private static void warnType() {
		System.err.println("WARN: Illegal metadata type requested.");
	}

	public static enum Type {
		FLOAT,
		INT,
		VEC3,
		STRING,
		RAW_BYTES
	}
}
