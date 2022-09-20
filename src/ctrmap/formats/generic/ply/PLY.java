package ctrmap.formats.generic.ply;

import xstandard.math.vec.RGBA;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.model.Vertex;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import xstandard.fs.FSFile;
import xstandard.gui.file.ExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class PLY {

	public static final ExtensionFilter EXTENSION_FILTER = new ExtensionFilter("Stanford PLY", "*.ply");
	
	public PLYElementGroup vertices = new PLYElementGroup();
	public PLYElementGroup faces = new PLYElementGroup();

	public PLY(File f) throws FileNotFoundException {
		this(new FileInputStream(f));
	}
	
	public PLY(FSFile fsf) {
		this(fsf.getNativeInputStream());
	}

	public PLY(InputStream in) {
		Scanner s = new Scanner(in);
		String magic = s.nextLine();
		if (!magic.equals("ply")) {
			throw new IllegalArgumentException("Source file is not an ASCII PLY file.");
		}

		boolean headerDone = false;

		List<PLYElementGroup> elemList = new ArrayList<>();

		while (s.hasNextLine() && !headerDone) {
			String l = s.nextLine();
			String[] commands = l.split(" ");
			switch (commands[0]) {
				case "format":
				case "comment":
					break;
				case "end_header":
					headerDone = true;
					break;
				case "element":
					switch (commands[1]) {
						case "vertex":
							elemList.add(vertices);
							vertices.expectedElements = getInt(commands[2]);
							break;
						case "face":
							elemList.add(faces);
							faces.expectedElements = getInt(commands[2]);
							break;
					}
					break;
				case "property":
					elemList.get(elemList.size() - 1).properties.add(new PLYElementGroup.ElementProperty(commands));
					break;
			}
		}

		//Read elements now	
		for (PLYElementGroup grp : elemList) {
			for (int i = 0; i < grp.expectedElements; i++) {
				String[] commands = s.nextLine().split(" ");
				grp.elements.add(new PLYElementGroup.Element(grp, commands));
			}
		}
	}

	public Model toGeneric() {
		Model m = new Model();
		Mesh mesh = new Mesh();
		
		String matName = "Ply_DummyMat_" + new SecureRandom().nextInt();
		
		mesh.materialName = matName;
		Material mat = new Material();
		mat.tevStages.stages[0].rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
		mat.tevStages.stages[0].alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;	//vertex color only
		mat.name = matName;
		m.addMaterial(mat);

		for (PLY.PLYElementGroup.ElementProperty p : vertices.properties) {
			if (p.name.equals("red") || p.name.equals("blue") || p.name.equals("green") || p.name.equals("alpha")) {
				mesh.hasColor = true;
				break;
			}
			if (p.name.equals("s") || p.name.equals("t")){
				mesh.hasUV[0] = true;
				break;
			}
		}

		Vertex[] vertexBuffer = new Vertex[vertices.elements.size()];
		for (int i = 0; i < vertexBuffer.length; i++) {
			Vertex v = new Vertex();

			PLYElementGroup.Element e = vertices.elements.get(i);
			v.position = new Vec3f(e.getProperty("x").getFloatValue(), e.getProperty("y").getFloatValue(), e.getProperty("z").getFloatValue());

			if (mesh.hasColor) {
				v.color = getColor(e.getProperty("red"), e.getProperty("green"), e.getProperty("blue"), e.getProperty("alpha"));
			}
			if (mesh.hasUV[0]){
				v.uv[0] = new Vec2f(e.getProperty("s").getFloatValue(), e.getProperty("t").getFloatValue());
			}

			vertexBuffer[i] = v;
		}

		for (PLYElementGroup.Element e : faces.elements) {
			int[] face = getFace(e);

			if (face.length > 4) {
				throw new UnsupportedOperationException("Only tris and quads are supported.");
			}

			Vertex[] vertices = new Vertex[face.length];
			for (int i = 0; i < face.length; i++) {
				vertices[i] = vertexBuffer[face[i]];
			}

			for (int i = 0; i < 3; i++) {
				mesh.vertices.add(vertices[i]);
			}
			if (vertices.length == 4) {
				mesh.vertices.add(vertices[0]);
				mesh.vertices.add(vertices[3]);
				mesh.vertices.add(vertices[2]);
			}
		}
		m.addMesh(mesh);
		return m;
	}

	public static RGBA getColor(PLY.PLYElementGroup.ElementProperty r, PLY.PLYElementGroup.ElementProperty g, PLY.PLYElementGroup.ElementProperty b, PLY.PLYElementGroup.ElementProperty a) {
		int red = get255Color(r);
		int green = get255Color(g);
		int blue = get255Color(b);
		int alpha = get255Color(a);
		return new RGBA(red, green, blue, alpha);
	}

	private static int get255Color(PLY.PLYElementGroup.ElementProperty col) {
		int c255 = 255;
		if (col != null) {
			if (col.getIsTypeFloatingPoint()) {
				c255 = (int) (col.getFloatValue() * 255);
			} else {
				c255 = col.getIntValue();
			}
		}
		return c255;
	}

	public static int[] getFace(PLY.PLYElementGroup.Element e) {
		PLY.PLYElementGroup.ElementProperty indices = e.getProperty("vertex_indices");
		if (indices.type.equals("list")) {
			int[] ret = new int[indices.getIntValue()];
			List<PLY.PLYElementGroup.ElementProperty> l = indices.getListValues();
			for (int i = 0; i < ret.length; i++) {
				ret[i] = l.get(i).getIntValue();
			}
			return ret;
		} else {
			throw new UnsupportedOperationException("Face elements are only supported as lists.");
		}
	}

	public static int getInt(String s) {
		return Integer.parseInt(s);
	}

	public static float getFloat(String s) {
		return Float.parseFloat(s);
	}

	public static double getDouble(String s) {
		return Double.parseDouble(s);
	}

	public static class PLYElementGroup {

		public int expectedElements;

		public List<ElementProperty> properties = new ArrayList<>();

		public List<Element> elements = new ArrayList<>();

		public static class Element {

			public List<ElementProperty> properties = new ArrayList<>();

			public Element(PLYElementGroup grp, String[] commands) {
				for (int i = 0; i < commands.length;) {
					ElementProperty p = new ElementProperty(grp.properties.get(i), commands, i);
					i += p.getElementCommandSize();
					properties.add(p);
				}
			}

			public ElementProperty getProperty(String id) {
				for (ElementProperty p : properties) {
					if (p.name.equals(id)) {
						return p;
					}
				}
				return null;
			}
		}

		public static class ElementProperty {

			public String name;
			public String type;
			private long value = 0;
			private String subValueType;
			private List<ElementProperty> subValues = new ArrayList<>();

			public ElementProperty(String[] commands) {
				type = commands[1];
				name = commands[commands.length - 1];
				if (type.equals("list")) {
					subValueType = commands[3];
				}
			}

			public ElementProperty(ElementProperty model, String[] commands, int propertyIndex) {
				name = model.name;
				type = model.type;
				switch (type) {
					case "float":
					case "float32":
						value = Float.floatToIntBits(getFloat(commands[propertyIndex]));
						break;
					case "double":
					case "float64":
						value = Double.doubleToLongBits(getDouble(commands[propertyIndex]));
						break;
					case "char":
					case "uchar":
					case "short":
					case "ushort":
					case "int":
					case "uint":
					case "int8":
					case "uint8":
					case "int16":
					case "uint16":
					case "int32":
					case "uint32":
						value = getInt(commands[propertyIndex]);
						break;
					case "list":
						String[] listData = Arrays.copyOfRange(commands, propertyIndex, commands.length);
						value = getInt(listData[0]); //count
						ElementProperty listModel = getListElementSubPropertyModel(model);
						for (int i = 0; i < value; i++) {
							subValues.add(new ElementProperty(listModel, listData, i + 1));
						}
						break;
					default:
						throw new IllegalArgumentException("Unknown property type " + type);
				}
			}

			public boolean getIsTypeFloatingPoint() {
				switch (type) {
					case "float":
					case "float32":
					case "double":
					case "float64":
						return true;
				}
				return false;
			}

			public int getElementCommandSize() {
				if (type.equals("list")) {
					return (int) value + 1;
				} else {
					return 1;
				}
			}

			public ElementProperty getListElementSubPropertyModel(ElementProperty list) {
				return new ElementProperty(new String[]{"dummy", list.subValueType, list.name + "_value"});
			}

			public List<ElementProperty> getListValues() {
				if (type.equals("list")) {
					return subValues;
				} else {
					throw new UnsupportedOperationException("This ElementProperty is not a list.");
				}
			}

			public float getFloatValue() {
				switch (type) {
					case "float":
					case "float32":
						return Float.intBitsToFloat((int) value);
					case "double":
					case "float64":
						return (float) Double.longBitsToDouble(value);
					case "char":
					case "uchar":
					case "short":
					case "ushort":
					case "int":
					case "uint":
					case "int8":
					case "uint8":
					case "int16":
					case "uint16":
					case "int32":
					case "uint32":
						return (int) value;
					default:
						throw new IllegalArgumentException("Invalid type");
				}
			}

			public int getIntValue() {
				switch (type) {
					case "float":
					case "float32":
						return (int) Float.intBitsToFloat((int) value);
					case "double":
					case "float64":
						return (int) Double.longBitsToDouble(value);
					case "char":
					case "uchar":
					case "short":
					case "ushort":
					case "int":
					case "uint":
					case "int8":
					case "uint8":
					case "int16":
					case "uint16":
					case "int32":
					case "uint32":
					case "list":
						return (int) value;
					default:
						throw new IllegalArgumentException("Invalid type " + type);
				}
			}
		}
	}
}
