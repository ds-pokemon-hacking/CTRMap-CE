package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.DAEConvMemory;
import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.Scene;
import xstandard.math.vec.RGBA;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.metadata.ReservedMetaData;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.texturing.LUT;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.util.MaterialProcessor;
import xstandard.fs.FSUtil;
import xstandard.gui.file.CommonExtensionFilters;
import xstandard.text.StringEx;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAEEffect implements DAEIDAble, DAESerializable {

	private static final DAEEnumMapper<TexEnvConfig.PICATextureCombinerMode> TEXCMB_MODE = new DAEEnumMapper<>(
		TexEnvConfig.PICATextureCombinerMode.REPLACE,
		"REPLACE", TexEnvConfig.PICATextureCombinerMode.REPLACE,
		"MODULATE", TexEnvConfig.PICATextureCombinerMode.MODULATE,
		"ADD", TexEnvConfig.PICATextureCombinerMode.ADD,
		"ADD_SIGNED", TexEnvConfig.PICATextureCombinerMode.ADD_SIGNED,
		"INTERPOLATE", TexEnvConfig.PICATextureCombinerMode.INTERPOLATE,
		"SUBRACT", TexEnvConfig.PICATextureCombinerMode.SUBTRACT,
		"DOT3", TexEnvConfig.PICATextureCombinerMode.DOT3_RGB,
		"ADD_MULT", TexEnvConfig.PICATextureCombinerMode.ADD_MULT,
		"MULT_ADD", TexEnvConfig.PICATextureCombinerMode.MULT_ADD,
		"DOT3_RGBA", TexEnvConfig.PICATextureCombinerMode.DOT3_RGBA
	);

	private static final DAEEnumMapper<TexEnvConfig.PICATextureCombinerSource> TEXCMB_SOURCE = new DAEEnumMapper<>(
		TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE,
		"PRIMARY", TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR,
		"CONSTANT", TexEnvConfig.PICATextureCombinerSource.CONSTANT,
		"TEXTURE", TexEnvConfig.PICATextureCombinerSource.TEX0,
		"TEXTURE", TexEnvConfig.PICATextureCombinerSource.TEX1,
		"TEXTURE", TexEnvConfig.PICATextureCombinerSource.TEX2,
		"TEXTURE", TexEnvConfig.PICATextureCombinerSource.TEX3,
		"PREVIOUS", TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE,
		"PREVIOUS_BUFFER", TexEnvConfig.PICATextureCombinerSource.PREVIOUS_BUFFER,
		"FRAGMENT_PRIMARY_COLOR", TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR,
		"FRAGMENT_SECONDARY_COLOR", TexEnvConfig.PICATextureCombinerSource.FRAG_SECONDARY_COLOR
	);

	private static final DAEEnumMapper<TexEnvConfig.PICATextureCombinerColorOp> TEXCMB_OPERAND_RGB = new DAEEnumMapper<>(
		TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR,
		"SRC_COLOR", TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR,
		"ONE_MINUS_SRC_COLOR", TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_SRC_COLOR,
		"SRC_ALPHA", TexEnvConfig.PICATextureCombinerColorOp.SRC_ALPHA,
		"ONE_MINUS_SRC_ALPHA", TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_SRC_ALPHA,
		"SRC_RED", TexEnvConfig.PICATextureCombinerColorOp.RED,
		"SRC_GREEN", TexEnvConfig.PICATextureCombinerColorOp.GREEN,
		"SRC_BLUE", TexEnvConfig.PICATextureCombinerColorOp.BLUE,
		"ONE_MINUS_SRC_RED", TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_RED,
		"ONE_MINUS_SRC_GREEN", TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_GREEN,
		"ONE_MINUS_SRC_BLUE", TexEnvConfig.PICATextureCombinerColorOp.ONE_MINUS_BLUE
	);

	private static final DAEEnumMapper<TexEnvConfig.PICATextureCombinerAlphaOp> TEXCMB_OPERAND_A = new DAEEnumMapper<>(
		TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA,
		"SRC_ALPHA", TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA,
		"ONE_MINUS_SRC_ALPHA", TexEnvConfig.PICATextureCombinerAlphaOp.ONE_MINUS_SRC_ALPHA,
		"SRC_RED", TexEnvConfig.PICATextureCombinerAlphaOp.RED,
		"SRC_GREEN", TexEnvConfig.PICATextureCombinerAlphaOp.GREEN,
		"SRC_BLUE", TexEnvConfig.PICATextureCombinerAlphaOp.BLUE,
		"ONE_MINUS_SRC_RED", TexEnvConfig.PICATextureCombinerAlphaOp.ONE_MINUS_RED,
		"ONE_MINUS_SRC_GREEN", TexEnvConfig.PICATextureCombinerAlphaOp.ONE_MINUS_GREEN,
		"ONE_MINUS_SRC_BLUE", TexEnvConfig.PICATextureCombinerAlphaOp.ONE_MINUS_BLUE
	);

	private static final DAEEnumMapper<TexEnvConfig.Scale> TEXCMB_SCALE = new DAEEnumMapper<>(
		TexEnvConfig.Scale.ONE,
		"ONE", TexEnvConfig.Scale.ONE,
		"TWO", TexEnvConfig.Scale.TWO,
		"FOUR", TexEnvConfig.Scale.FOUR
	);

	private static final DAEEnumMapper<MaterialParams.LUTTarget> LUT_TARGET = new DAEEnumMapper<>(
		MaterialParams.LUTTarget.REFLEC_R,
		"REFLEC_R", MaterialParams.LUTTarget.REFLEC_R,
		"REFLEC_G", MaterialParams.LUTTarget.REFLEC_G,
		"REFLEC_B", MaterialParams.LUTTarget.REFLEC_B,
		"DIST_0", MaterialParams.LUTTarget.DIST_0,
		"DIST_1", MaterialParams.LUTTarget.DIST_1,
		"FRESNEL_PRI", MaterialParams.LUTTarget.FRESNEL_PRI,
		"FRESNEL_SEC", MaterialParams.LUTTarget.FRESNEL_SEC
	);

	private static final DAEEnumMapper<MaterialParams.LUTSource> LUT_SOURCE = new DAEEnumMapper<>(
		MaterialParams.LUTSource.LIGHT_NORMAL,
		"COS_LIGHT_NORMAL", MaterialParams.LUTSource.LIGHT_NORMAL,
		"COS_LIGHT_SPOT", MaterialParams.LUTSource.LIGHT_SPOT,
		"COS_NORMAL_HALF", MaterialParams.LUTSource.NORMAL_HALF,
		"COS_NORMAL_VIEW", MaterialParams.LUTSource.NORMAL_VIEW,
		"COS_VIEW_HALF", MaterialParams.LUTSource.VIEW_HALF,
		"COS_PHI", MaterialParams.LUTSource.PHI
	);

	private static final DAEEnumMapper<MaterialParams.TestFunction> RS_TEST_FUNC = new DAEEnumMapper<>(
		MaterialParams.TestFunction.ALWAYS,
		"NEVER", MaterialParams.TestFunction.NEVER,
		"LESS", MaterialParams.TestFunction.LESS,
		"LEQUAL", MaterialParams.TestFunction.LEQ,
		"EQUAL", MaterialParams.TestFunction.EQ,
		"GREATER", MaterialParams.TestFunction.GREATER,
		"NOTEQUAL", MaterialParams.TestFunction.NEQ,
		"GEQUAL", MaterialParams.TestFunction.GEQ,
		"ALWAYS", MaterialParams.TestFunction.ALWAYS
	);

	private static final DAEEnumMapper<MaterialParams.BlendFunction> RS_BLEND_FUNC = new DAEEnumMapper<>(
		MaterialParams.BlendFunction.ZERO,
		"ZERO", MaterialParams.BlendFunction.ZERO,
		"ONE", MaterialParams.BlendFunction.ONE,
		"SRC_COLOR", MaterialParams.BlendFunction.SRC_COLOR,
		"ONE_MINUS_SRC_COLOR", MaterialParams.BlendFunction.ONE_MINUS_SRC_COLOR,
		"DEST_COLOR", MaterialParams.BlendFunction.DEST_COLOR,
		"ONE_MINUS_DEST_COLOR", MaterialParams.BlendFunction.ONE_MINUS_DEST_COLOR,
		"SRC_ALPHA", MaterialParams.BlendFunction.SRC_ALPHA,
		"ONE_MINUS_SRC_ALPHA", MaterialParams.BlendFunction.ONE_MINUS_SRC_ALPHA,
		"DST_ALPHA", MaterialParams.BlendFunction.DEST_ALPHA,
		"ONE_MINUS_DST_ALPHA", MaterialParams.BlendFunction.ONE_MINUS_DEST_ALPHA,
		"CONSTANT_COLOR", MaterialParams.BlendFunction.CONSTANT_COLOR,
		"ONE_MINUS_CONSTANT_COLOR", MaterialParams.BlendFunction.ONE_MINUS_CONSTANT_COLOR,
		"CONSTANT_ALPHA", MaterialParams.BlendFunction.CONSTANT_ALPHA,
		"ONE_MINUS_CONSTANT_ALPHA", MaterialParams.BlendFunction.ONE_MINUS_CONSTANT_ALPHA,
		"SRC_ALPHA_SATURATE", MaterialParams.BlendFunction.SOURCE_ALPHA_SATURATE
	);

	private static final DAEEnumMapper<MaterialParams.BlendEquation> RS_BLEND_EQUATION = new DAEEnumMapper<>(
		MaterialParams.BlendEquation.ADD,
		"FUNC_ADD", MaterialParams.BlendEquation.ADD,
		"FUNC_SUBTRACT", MaterialParams.BlendEquation.SUB,
		"FUNC_REVERSE_SUBTRACT", MaterialParams.BlendEquation.REVERSE_SUB,
		"MIN", MaterialParams.BlendEquation.MIN,
		"MAX", MaterialParams.BlendEquation.MAX
	);

	private static final DAEEnumMapper<MaterialParams.FaceCulling> RS_FACE_CULLING = new DAEEnumMapper<>(
		MaterialParams.FaceCulling.BACK_FACE,
		"FRONT_AND_BACK", MaterialParams.FaceCulling.FRONT_AND_BACK,
		"BACK", MaterialParams.FaceCulling.BACK_FACE,
		"FRONT", MaterialParams.FaceCulling.FRONT_FACE
	);

	private static final DAEEnumMapper<MaterialParams.StencilOp> RS_STENCIL_OP = new DAEEnumMapper<>(
		MaterialParams.StencilOp.KEEP,
		"KEEP", MaterialParams.StencilOp.KEEP,
		"ZERO", MaterialParams.StencilOp.ZERO,
		"REPLACE", MaterialParams.StencilOp.REPLACE,
		"INCR", MaterialParams.StencilOp.INCREMENT,
		"DECR", MaterialParams.StencilOp.DECREMENT,
		"INVERT", MaterialParams.StencilOp.INVERT,
		"INCR_WRAP", MaterialParams.StencilOp.INCREMENT_WRAP,
		"DECR_WRAP", MaterialParams.StencilOp.DECREMENT_WRAP
	);

	private static final DAEEnumMapper<MaterialParams.TextureMapMode> TEX_MAP_MODE_EX = new DAEEnumMapper<>(
		MaterialParams.TextureMapMode.UV_MAP,
		"CUBE_MAP", MaterialParams.TextureMapMode.CUBE_MAP,
		"SPHERE_MAP", MaterialParams.TextureMapMode.SPHERE_MAP,
		"PROJECTION_MAP", MaterialParams.TextureMapMode.PROJECTION_MAP
	);

	private String id;

	public String name;

	public List<DAEEffectSlotInfo> textures = new ArrayList<>();

	public List<DAELUT> LUTs = new ArrayList<>();

	public String transparencyMode;

	public TexEnvConfig texComb;
	public RGBA[] constantColors = new RGBA[MaterialColorType.values().length];

	public RGBA ambientColor;
	public RGBA diffuseColor;
	public RGBA specular0Color;
	public RGBA specular1Color;
	public RGBA emissionColor;

	public MaterialParams.DepthColorMask depthColorMask;
	public MaterialParams.AlphaTest alphaTest;
	public MaterialParams.BlendOperation blendOperation;
	public MaterialParams.StencilOperation stencilOperation;
	public MaterialParams.StencilTest stencilTest;

	public MaterialParams.FaceCulling faceCulling;

	public float lineWidth = -1f;

	public DAEEffect(Element elem) {
		id = elem.getAttribute("id");
		name = elem.getAttribute("name");

		Element profileCommon = XmlFormat.getParamElement(elem, "profile_COMMON");

		List<Element> params = XmlFormat.getElementsByTagName(profileCommon, "newparam");

		List<Element> texUnitElems = new ArrayList<>();
		Map<String, Integer> texUnitIndices = new HashMap<>();

		boolean useTexUnits = false;
		for (Element e : params) {
			Element texunit = XmlFormat.getParamElement(e, "texture_unit");
			if (texunit != null) {
				useTexUnits = true;
				texUnitIndices.put(e.getAttribute("sid"), texUnitElems.size());
				texUnitElems.add(texunit);

				DAEEffectSlotInfo dmyEffSlot = new DAEEffectSlotInfo("diffuse");
				Element tc = XmlFormat.getParamElement(texunit, "texcoord");
				if (tc != null) {
					dmyEffSlot.uvSetName = tc.getAttribute("semantic");
				}
				String samplerName = XmlFormat.getParamNodeValue(texunit, "sampler_state");

				Element samplerNewparam = XmlFormat.getByAttribute(params, "sid", samplerName);
				if (samplerNewparam != null) {
					Element samplerStateElem = XmlFormat.getParamElement(samplerNewparam, "sampler_state");
					if (samplerStateElem != null) {
						dmyEffSlot.sampler = new DAESampler2D(samplerStateElem);
					}
				}

				String surfaceName = XmlFormat.getParamNodeValue(texunit, "surface");
				Element surface = XmlFormat.getByAttribute(params, "sid", surfaceName);

				if (surface != null) {
					dmyEffSlot.imageId = XmlFormat.getParamNodeValue(surface, "surface", "init_from");
				}
				textures.add(dmyEffSlot);
			}
		}

		Element technique = XmlFormat.getParamElement(profileCommon, "technique");
		if (technique != null) {
			List<Element> children = XmlFormat.getElementList(XmlFormat.nodeListToListOfNodes(technique.getChildNodes()));
			for (Element tech : children) {
				diffuseColor = getMatColorParam(technique, "diffuse");
				ambientColor = getMatColorParam(technique, "ambient");
				emissionColor = getMatColorParam(technique, "emission");
				specular0Color = getMatColorParam(technique, "specular", 0);
				specular1Color = getMatColorParam(technique, "specular", 1);

				List<Element> textureNodes = XmlFormat.getElementsByPath(tech, "diffuse");
				textureNodes.addAll(XmlFormat.getElementsByPath(tech, "bump"));
				if (!textureNodes.isEmpty()) {
					if (!useTexUnits) {
						for (Element effSlotElem : textureNodes) {
							textures.add(new DAEEffectSlotInfo(effSlotElem, params));
						}
					}

					Element transparent = XmlFormat.getElementByPath(tech, "transparent");

					if (transparent != null) {
						transparencyMode = transparent.getAttribute("opaque");
					}

					break;
				}
			}

			List<Element> passes = XmlFormat.getElementsByTagName(technique, "pass");

			Element renderStatePass = XmlFormat.getByAttribute(passes, "sid", "CtrRenderState");
			if (renderStatePass != null) {
				RenderStateReader reader = new RenderStateReader(renderStatePass);

				depthColorMask = new MaterialParams.DepthColorMask();
				depthColorMask.depthFunction = reader.getRSEnum(RS_TEST_FUNC, MaterialParams.TestFunction.ALWAYS, "depth_func");
				depthColorMask.enabled = reader.getRSBool("depth_test_enable");
				depthColorMask.depthWrite = reader.getRSBool("depth_mask");
				boolean[] colorMask = reader.getRSBool4("color_mask");
				depthColorMask.redWrite = colorMask[0];
				depthColorMask.greenWrite = colorMask[1];
				depthColorMask.blueWrite = colorMask[2];
				depthColorMask.alphaWrite = colorMask[3];

				if (reader.hasRSAttrib("blend_func")
					|| reader.hasRSAttrib("blend_func_separate")
					|| reader.hasRSAttrib("blend_equation")
					|| reader.hasRSAttrib("blend_equation_separate")) {
					blendOperation = new MaterialParams.BlendOperation();
					blendOperation.enabled = reader.getRSBool("blend_enable");
					blendOperation.blendColor = reader.getRSColor(new RGBA(0, 0, 0, 0), "blend_color");
					if (reader.hasRSAttrib("blend_func_separate")) {
						blendOperation.colorSrcFunc = reader.getRSEnum(RS_BLEND_FUNC, MaterialParams.BlendFunction.ONE, "blend_func_separate", "src_rgb");
						blendOperation.colorDstFunc = reader.getRSEnum(RS_BLEND_FUNC, MaterialParams.BlendFunction.ZERO, "blend_func_separate", "dest_rgb");
						blendOperation.alphaSrcFunc = reader.getRSEnum(RS_BLEND_FUNC, MaterialParams.BlendFunction.ONE, "blend_func_separate", "src_alpha");
						blendOperation.alphaDstFunc = reader.getRSEnum(RS_BLEND_FUNC, MaterialParams.BlendFunction.ZERO, "blend_func_separate", "dest_alpha");
					} else {
						blendOperation.colorSrcFunc = reader.getRSEnum(RS_BLEND_FUNC, MaterialParams.BlendFunction.ONE, "blend_func", "src");
						blendOperation.colorDstFunc = reader.getRSEnum(RS_BLEND_FUNC, MaterialParams.BlendFunction.ZERO, "blend_func", "dest");
						blendOperation.alphaSrcFunc = blendOperation.colorSrcFunc;
						blendOperation.alphaDstFunc = blendOperation.colorDstFunc;
					}
					if (reader.hasRSAttrib("blend_equation_separate")) {
						blendOperation.colorEquation = reader.getRSEnum(RS_BLEND_EQUATION, MaterialParams.BlendEquation.ADD, "blend_equation_separate", "rgb");
						blendOperation.alphaEquation = reader.getRSEnum(RS_BLEND_EQUATION, MaterialParams.BlendEquation.ADD, "blend_equation_separate", "alpha");
					} else {
						blendOperation.colorEquation = reader.getRSEnum(RS_BLEND_EQUATION, MaterialParams.BlendEquation.ADD, "blend_equation");
						blendOperation.alphaEquation = blendOperation.colorEquation;
					}
				}

				alphaTest = new MaterialParams.AlphaTest();
				alphaTest.enabled = reader.getRSBool("alpha_test_enable");
				alphaTest.testFunction = reader.getRSEnum(RS_TEST_FUNC, MaterialParams.TestFunction.ALWAYS, "alpha_func", "func");
				alphaTest.reference = (int) (255f * reader.getRSFloat(0f, "alpha_func", "value"));

				boolean cullFace = reader.getRSBool("cull_face_enable");
				if (cullFace) {
					faceCulling = reader.getRSEnum(RS_FACE_CULLING, MaterialParams.FaceCulling.BACK_FACE, "cull_face");
				} else {
					faceCulling = MaterialParams.FaceCulling.NEVER;
				}

				stencilTest = new MaterialParams.StencilTest();
				stencilTest.testFunction = reader.getRSEnum(RS_TEST_FUNC, MaterialParams.TestFunction.ALWAYS, "stencil_func", "func");
				stencilTest.reference = reader.getRSInt(0, "stencil_func", "ref");
				stencilTest.funcMask = reader.getRSInt(255, "stencil_func", "mask");
				stencilTest.bufferMask = reader.getRSInt(0xFF, "stencil_mask");
				stencilTest.enabled = reader.getRSBool("stencil_test_enable");

				stencilOperation = new MaterialParams.StencilOperation();
				stencilOperation.fail = reader.getRSEnum(RS_STENCIL_OP, MaterialParams.StencilOp.KEEP, "stencil_op", "fail");
				stencilOperation.zFail = reader.getRSEnum(RS_STENCIL_OP, MaterialParams.StencilOp.KEEP, "stencil_op", "zfail");
				stencilOperation.zPass = reader.getRSEnum(RS_STENCIL_OP, MaterialParams.StencilOp.KEEP, "stencil_op", "zpass");

				lineWidth = reader.getRSFloat(-1f, "line_width");
			}

			Element lutPass = XmlFormat.getByAttribute(passes, "sid", "CtrLUTFragLight");
			if (lutPass != null) {
				List<Element> luts = XmlFormat.getElementsByPath(lutPass, "extra", "technique");
				for (Element l : luts) {
					DAELUT dl = new DAELUT();
					Element src = XmlFormat.getParamElement(l, "LUTSource");
					dl.source = LUT_SOURCE.getValue(src.getAttribute("input"));
					dl.target = LUT_TARGET.getValue(l.getAttribute("profile"));
					dl.imageId = src.getTextContent().trim();
					LUTs.add(dl);
				}
			}

			Element texcombPass = XmlFormat.getByAttribute(passes, "sid", "CtrTextureCombiner");
			if (texcombPass != null) {
				List<Element> texCombiners = XmlFormat.getElementsByPath(texcombPass, "texture_pipeline", "texcombiner");
				texComb = new TexEnvConfig();
				Element inBufColor = XmlFormat.getElementByPath(texcombPass, "texture_pipeline", "input_buffer_color");
				if (inBufColor != null) {
					texComb.inputBufferColor = XmlFormat.getFPRGBAValue(inBufColor);
				}

				for (int i = 0; i < Math.min(texComb.stages.length, texCombiners.size()); i++) {
					Element tcElem = texCombiners.get(i);

					TexEnvStage stage = texComb.stages[i];
					stage.constantColor = MaterialColorType.values()[MaterialColorType.CONSTANT0.ordinal() + i];
					Element constColor = XmlFormat.getParamElement(tcElem, "constant");
					constantColors[i] = XmlFormat.getFPRGBAValue(constColor, new RGBA());

					Element rgb = XmlFormat.getParamElement(tcElem, "RGB");
					if (rgb != null) {
						stage.rgbCombineOperator = XmlFormat.getAttributeEnum(rgb, "operator", TEXCMB_MODE);
						String attrScale = rgb.getAttribute("scale");
						if (attrScale != null) {
							float scale = Float.parseFloat(attrScale);
							stage.rgbScale = TexEnvConfig.Scale.forFloat(scale);
						}
						stage.writeColorBuffer = XmlFormat.getAttributeBool(rgb, "buffer");

						readTexcmbArgs(rgb, stage.rgbSource, texUnitIndices, ((index, value) -> {
							stage.rgbOperand[index] = TEXCMB_OPERAND_RGB.getValue(value);
						}));
					}

					Element alpha = XmlFormat.getParamElement(tcElem, "alpha");
					if (alpha != null) {
						stage.alphaCombineOperator = XmlFormat.getAttributeEnum(alpha, "operator", TEXCMB_MODE);
						String attrScale = alpha.getAttribute("scale");
						if (attrScale != null) {
							float scale = Float.parseFloat(attrScale);
							stage.alphaScale = TexEnvConfig.Scale.forFloat(scale);
						}
						stage.writeAlphaBuffer = XmlFormat.getAttributeBool(alpha, "buffer");

						readTexcmbArgs(alpha, stage.alphaSource, texUnitIndices, ((index, value) -> {
							stage.alphaOperand[index] = TEXCMB_OPERAND_A.getValue(value);
						}));
					}
				}
			}
		}
	}

	private static class RenderStateReader {

		private Element pass;

		public RenderStateReader(Element pass) {
			this.pass = pass;
		}

		public boolean hasRSAttrib(String... path) {
			return getRSStr(path) != null;
		}

		public String getRSStr(String... path) {
			return XmlFormat.getElementAttribByPath(pass, "value", path);
		}

		public RGBA getRSColor(RGBA defaultValue, String... path) {
			return XmlFormat.getFPRGBAValue(getRSStr(path), defaultValue);
		}

		public boolean getRSBool(String... path) {
			String val = getRSStr(path);
			return Objects.equals(val, "true");
		}

		public boolean[] getRSBool4(String... path) {
			String val = getRSStr(path);
			boolean[] ret = new boolean[4];
			if (val != null) {
				String[] split = StringEx.splitOnecharFastNoBlank(val, ' ');
				for (int i = 0; i < split.length; i++) {
					ret[i] = split[i].equals("true");
				}
			}
			return ret;
		}

		public int getRSInt(int defaultValue, String... path) {
			String str = getRSStr(path);
			return str == null ? defaultValue : Integer.parseInt(str);
		}

		public float getRSFloat(float defaultValue, String... path) {
			String str = getRSStr(path);
			return str == null ? defaultValue : Float.parseFloat(str);
		}

		public <E extends Enum> E getRSEnum(DAEEnumMapper<E> mapper, E defaultValue, String... path) {
			return mapper.getValue(getRSStr(path), defaultValue);
		}
	}

	private static class RenderStateWriter {

		private Document doc;
		private Element pass;

		public RenderStateWriter(Document doc, Element pass) {
			this.doc = doc;
			this.pass = pass;
		}

		public Element createParentElement(String name) {
			return createParentElement(pass, name);
		}

		public Element createParentElement(Element parent, String name) {
			Element elem = doc.createElement(name);
			parent.appendChild(elem);
			return elem;
		}

		public <E extends Enum> void setSimpleParentElemValueEnum(String name, DAEEnumMapper<E> mapper, E value) {
			setSimpleParentElemValueEnum(pass, name, mapper, value);
		}

		public <E extends Enum> void setSimpleParentElemValueEnum(Element parent, String name, DAEEnumMapper<E> mapper, E value) {
			setSimpleParentElemValue(parent, name, mapper.getName(value));
		}

		public <E extends Enum> void setSimpleParentElemValueRGBA(String name, RGBA value) {
			setSimpleParentElemValue(name, XmlFormat.getFPRGBA(value));
		}

		public void setSimpleParentElemValueBool4(String name, boolean b1, boolean b2, boolean b3, boolean b4) {
			setSimpleParentElemValue(name, b1 + " " + b2 + " " + b3 + " " + b4);
		}

		public void setSimpleParentElemValue(String name, Object value) {
			setSimpleParentElemValue(pass, name, value);
		}

		public void setSimpleParentElemValue(Element parent, String name, Object value) {
			Element pe = createParentElement(parent, name);
			pe.setAttribute("value", String.valueOf(value));
		}
	}

	private static interface ReadTexcmbOperandCallback {

		public void set(int index, String value);
	}

	private void readTexcmbArgs(Element main, TexEnvConfig.PICATextureCombinerSource[] outSources, Map<String, Integer> texUnitIndices, ReadTexcmbOperandCallback opCb) {
		int argidx = 0;
		for (Element arg : XmlFormat.getElementsByTagName(main, "argument")) {
			outSources[argidx] = XmlFormat.getAttributeEnum(arg, "source", TEXCMB_SOURCE);
			opCb.set(argidx, arg.getAttribute("operand"));

			if (TexEnvConfig.isTexSource(outSources[argidx])) {
				String unit = arg.getAttribute("unit");
				if (texUnitIndices.containsKey(unit)) {
					int unitIndex = texUnitIndices.get(unit);
					if (unitIndex <= 3) {
						outSources[argidx] = TexEnvConfig.PICATextureCombinerSource.getTexSource(unitIndex);
					}
				}
			}

			if (argidx++ > 2) {
				break;
			}
		}
	}

	private RGBA getMatColorParam(Element technique, String name) {
		Element e = XmlFormat.getElementByPath(technique, name, "color");
		if (e == null) {
			return null;
		}
		return XmlFormat.getFPRGBAValue(e);
	}

	private RGBA getMatColorParam(Element technique, String name, int index) {
		List<Element> el = XmlFormat.getElementsByPath(technique, name, "color");
		if (el == null || el.size() <= index || index < 0) {
			return null;
		}
		return XmlFormat.getFPRGBAValue(el.get(index));
	}

	public DAEEffect(Material mat, List<Texture> textures, DAEDict<DAEImage> outImages, DAEConvMemory<Texture, DAEImage> images) {
		name = XmlFormat.sanitizeName(mat.name) + "-effect";
		transparencyMode = MaterialProcessor.isAlphaBlendUsed(mat) || MaterialProcessor.isAlphaTestUsed(mat) ? "A_ONE" : null;
		texComb = mat.tevStages;

		ambientColor = mat.ambientColor;
		diffuseColor = mat.diffuseColor;
		emissionColor = mat.emissionColor;
		specular0Color = mat.specular0Color;
		specular1Color = mat.specular1Color;

		for (LUT lut : mat.LUTs) {
			DAELUT dl = new DAELUT();
			dl.target = lut.target;
			dl.source = lut.source;
			DAEImage img = createEnsureImage(lut.textureName, textures, outImages, images);
			dl.imageId = img.getID();
			LUTs.add(dl);
		}

		constantColors = mat.constantColors;

		if (!mat.textures.isEmpty()) {
			for (TextureMapper map : mat.textures) {
				if (map.textureName != null) {
					DAEEffectSlotInfo si = new DAEEffectSlotInfo(mat.textures.indexOf(map) == mat.bumpTextureIndex ? "bump" : "diffuse");
					si.color = mat.diffuseColor;
					if (map.mapMode == MaterialParams.TextureMapMode.UV_MAP) {
						si.uvSetName = "UVSet" + map.uvSetNo;
					} else {
						si.uvSetName = TEX_MAP_MODE_EX.getName(map.mapMode);
					}
					DAESampler2D samp = new DAESampler2D();
					samp.wrapU = map.mapU;
					samp.wrapV = map.mapV;
					samp.magFilter = map.textureMagFilter;
					samp.minFilter = map.textureMinFilter;
					si.sampler = samp;

					DAEImage img = createEnsureImage(map.textureName, textures, outImages, images);

					si.imageId = img.getID();
					this.textures.add(si);
				}
			}
		}

		depthColorMask = mat.depthColorMask;
		alphaTest = mat.alphaTest;
		stencilOperation = mat.stencilOperation;
		stencilTest = mat.stencilTest;
		faceCulling = mat.faceCulling;
		blendOperation = mat.blendOperation;
	}

	private DAEImage createEnsureImage(String imageName, List<Texture> textures, DAEDict<DAEImage> outImages, DAEConvMemory<Texture, DAEImage> images) {
		DAEImage img = images.findByInput(Scene.getNamedObject(imageName, textures));
		if (img == null) {
			img = images.findByInputName(imageName);
			if (img == null) {
				img = new DAEImage((Texture) null);
				img.initFrom = FSUtil.getFileNameWithoutExtension(imageName, CommonExtensionFilters.PNG.getPrimaryExtension()) + CommonExtensionFilters.PNG.getPrimaryExtension();
				img.name = imageName;
				Texture dmyTex = new Texture(0, 0);
				dmyTex.name = img.name;
				images.put(dmyTex, img);
				outImages.putNode(img);
			}
		}
		return img;
	}

	@Override

	public Element createElement(Document doc) {
		Element elem = doc.createElement("effect");
		elem.setAttribute("id", id);
		if (name != null) {
			elem.setAttribute("name", name);
		}
		Element profileCommon = doc.createElement("profile_COMMON");

		Element technique = doc.createElement("technique");
		technique.setAttribute("sid", "CtrShadeModel");

		Element techPhong = doc.createElement("phong");

		boolean addedDiffuse = false;

		String transparencySampler = null;
		String transparencyTexcoord = null;

		for (DAEEffectSlotInfo si : textures) {
			Element effectSlot = doc.createElement(si.mode);
			if (si.color != null) {
				Element colElem = doc.createElement("color");
				colElem.setAttribute("sid", "diffuse");
				colElem.setTextContent(XmlFormat.getFPRGBA(diffuseColor));
				effectSlot.appendChild(colElem);
				addedDiffuse = true;
			}
			if (si.imageId != null) {
				Element existingSurface = XmlFormat.getByAttribute(XmlFormat.getElementsByTagName(profileCommon, "newparam"), "sid", si.imageId + "-surface");
				if (existingSurface == null) {
					Element surface = doc.createElement("surface");
					surface.setAttribute("type", "2D");
					surface.appendChild(XmlFormat.createSimpleTextContentElem(doc, "init_from", si.imageId));

					appendAsNewparam(doc, profileCommon, surface, si.imageId + "-surface");

					si.sampler.surfaceName = si.imageId + "-surface";
					si.sampler.tagName = "sampler2D";

					transparencySampler = si.imageId + "-sampler";
					appendAsNewparam(doc, profileCommon, si.sampler.createElement(doc), transparencySampler);

					si.sampler.tagName = "sampler_state";

					appendAsNewparam(doc, profileCommon, si.sampler.createElement(doc), si.imageId + "-sampler-state");
				}

				Element texElem = doc.createElement("texture");
				texElem.setAttribute("texture", si.imageId + "-sampler");
				effectSlot.appendChild(texElem);
				if (si.uvSetName != null) {
					texElem.setAttribute("texcoord", si.uvSetName);
					transparencyTexcoord = si.uvSetName;
				}
			}
			techPhong.appendChild(effectSlot);
		}

		appendMatColElem(doc, techPhong, "ambient", ambientColor);

		if (!addedDiffuse) {
			appendMatColElem(doc, techPhong, "diffuse", diffuseColor);
		}

		appendMatColElem(doc, techPhong, "specular", specular0Color, specular1Color);
		appendMatColElem(doc, techPhong, "emission", emissionColor);

		if (transparencyMode != null) {
			Element transparent = doc.createElement("transparent");
			transparent.setAttribute("opaque", transparencyMode);
			if (transparencySampler != null && transparencyTexcoord != null) {
				Element tex = doc.createElement("texture");
				tex.setAttribute("texcoord", transparencyTexcoord);
				tex.setAttribute("texture", transparencySampler);
				transparent.appendChild(tex);
			}
			techPhong.appendChild(transparent);
		}

		technique.appendChild(techPhong);

		for (DAEEffectSlotInfo si : textures) {
			if (si.imageId != null) {
				Element texunit = doc.createElement("texture_unit");
				texunit.appendChild(XmlFormat.createSimpleTextContentElem(doc, "surface", si.imageId + "-surface"));
				texunit.appendChild(XmlFormat.createSimpleTextContentElem(doc, "sampler_state", si.imageId + "-sampler-state"));
				Element texcoord = doc.createElement("texcoord");
				texcoord.setAttribute("semantic", si.uvSetName);
				texunit.appendChild(texcoord);
				appendAsNewparam(doc, profileCommon, texunit, si.imageId + "-texunit");
			}
		}

		if (blendOperation != null || alphaTest != null || depthColorMask != null || stencilOperation != null || lineWidth != -1 || faceCulling != null || blendOperation != null) {
			Element pass = doc.createElement("pass");
			pass.setAttribute("sid", "CtrRenderState");

			RenderStateWriter writer = new RenderStateWriter(doc, pass);

			if (faceCulling != null) {
				boolean cullFace = faceCulling != MaterialParams.FaceCulling.NEVER;
				writer.setSimpleParentElemValue("cull_face_enable", cullFace);
				if (cullFace) {
					writer.setSimpleParentElemValueEnum("cull_face", RS_FACE_CULLING, faceCulling);
				}
			}

			if (lineWidth != -1f) {
				writer.setSimpleParentElemValue("line_width", lineWidth);
			}

			if (depthColorMask != null) {
				writer.setSimpleParentElemValue("depth_test_enable", depthColorMask.enabled);
				writer.setSimpleParentElemValueEnum("depth_func", RS_TEST_FUNC, depthColorMask.depthFunction);
				writer.setSimpleParentElemValue("depth_mask", depthColorMask.depthWrite);
				writer.setSimpleParentElemValueBool4("color_mask", depthColorMask.redWrite, depthColorMask.greenWrite, depthColorMask.blueWrite, depthColorMask.alphaWrite);
			}

			if (alphaTest != null) {
				writer.setSimpleParentElemValue("alpha_test_enable", alphaTest.enabled);

				Element alphaFunc = writer.createParentElement("alpha_func");

				writer.setSimpleParentElemValueEnum(alphaFunc, "func", RS_TEST_FUNC, alphaTest.testFunction);
				writer.setSimpleParentElemValue(alphaFunc, "value", alphaTest.reference);
			}

			if (blendOperation != null) {
				writer.setSimpleParentElemValue("blend_enable", blendOperation.enabled);
				writer.setSimpleParentElemValueRGBA("blend_color", blendOperation.blendColor);

				if (blendOperation.alphaEquation != blendOperation.colorEquation) {
					Element beSeparate = writer.createParentElement("blend_equation_separate");

					writer.setSimpleParentElemValueEnum(beSeparate, "rgb", RS_BLEND_EQUATION, blendOperation.colorEquation);
					writer.setSimpleParentElemValueEnum(beSeparate, "alpha", RS_BLEND_EQUATION, blendOperation.alphaEquation);
				} else {
					writer.setSimpleParentElemValueEnum("blend_equation", RS_BLEND_EQUATION, blendOperation.colorEquation);
				}
				if (blendOperation.colorSrcFunc != blendOperation.alphaSrcFunc || blendOperation.colorDstFunc != blendOperation.alphaDstFunc) {
					Element bfSep = writer.createParentElement("blend_func_separate");

					writer.setSimpleParentElemValueEnum(bfSep, "src_rgb", RS_BLEND_FUNC, blendOperation.colorSrcFunc);
					writer.setSimpleParentElemValueEnum(bfSep, "dest_rgb", RS_BLEND_FUNC, blendOperation.colorDstFunc);
					writer.setSimpleParentElemValueEnum(bfSep, "src_alpha", RS_BLEND_FUNC, blendOperation.alphaSrcFunc);
					writer.setSimpleParentElemValueEnum(bfSep, "dest_alpha", RS_BLEND_FUNC, blendOperation.alphaDstFunc);
				} else {
					Element bf = writer.createParentElement("blend_func");

					writer.setSimpleParentElemValueEnum(bf, "src", RS_BLEND_FUNC, blendOperation.colorSrcFunc);
					writer.setSimpleParentElemValueEnum(bf, "dest", RS_BLEND_FUNC, blendOperation.colorDstFunc);
				}
			}

			if (stencilTest != null) {
				writer.setSimpleParentElemValue("stencil_test_enable", stencilTest.enabled);

				Element stencilFunc = writer.createParentElement("stencil_func");

				writer.setSimpleParentElemValueEnum(stencilFunc, "func", RS_TEST_FUNC, stencilTest.testFunction);
				writer.setSimpleParentElemValue(stencilFunc, "ref", stencilTest.reference);
				writer.setSimpleParentElemValue(stencilFunc, "mask", stencilTest.funcMask);

				writer.setSimpleParentElemValue(stencilFunc, "stencil_mask", stencilTest.bufferMask);
			}

			if (stencilOperation != null) {
				Element stencilOp = writer.createParentElement("stencil_op");

				writer.setSimpleParentElemValueEnum(stencilOp, "fail", RS_STENCIL_OP, stencilOperation.fail);
				writer.setSimpleParentElemValueEnum(stencilOp, "zfail", RS_STENCIL_OP, stencilOperation.zFail);
				writer.setSimpleParentElemValueEnum(stencilOp, "zpass", RS_STENCIL_OP, stencilOperation.zPass);
			}

			technique.appendChild(pass);
		}

		if (LUTs != null) {
			Element pass = doc.createElement("pass");
			pass.setAttribute("sid", "CtrLUTFragLight");

			Element extra = doc.createElement("extra");

			for (DAELUT lut : LUTs) {
				Element lutTech = doc.createElement("technique");
				lutTech.setAttribute("profile", LUT_TARGET.getName(lut.target));
				Element lutSrc = doc.createElement("LUTSource");
				lutSrc.setAttribute("input", LUT_SOURCE.getName(lut.source));
				lutSrc.setTextContent(lut.imageId);
				lutTech.appendChild(lutSrc);
				extra.appendChild(lutTech);
			}

			pass.appendChild(extra);
			technique.appendChild(pass);
		}

		if (texComb != null) {
			Element pass = doc.createElement("pass");
			pass.setAttribute("sid", "CtrTextureCombiner");

			Element texPipelineEnable = doc.createElement("texture_pipeline_enable");
			texPipelineEnable.setAttribute("value", "true");
			pass.appendChild(texPipelineEnable);

			Element texPipeline = doc.createElement("texture_pipeline");

			texPipeline.appendChild(XmlFormat.createSimpleTextContentElem(doc, "input_buffer_color", XmlFormat.getFPRGBA(texComb.inputBufferColor)));

			for (int i = 0; i < texComb.getActiveStageCount(); i++) {
				TexEnvStage stage = texComb.stages[i];

				Element texc = doc.createElement("texcombiner");

				texc.appendChild(XmlFormat.createSimpleTextContentElem(doc, "constant", XmlFormat.getFPRGBA(constantColors[stage.constantColor.ordinal()])));

				texc.appendChild(createCombinerStageElement(doc, "RGB", stage.rgbCombineOperator, stage.rgbSource, TEXCMB_OPERAND_RGB, stage.rgbOperand, stage.rgbScale, stage.writeColorBuffer));
				texc.appendChild(createCombinerStageElement(doc, "alpha", stage.alphaCombineOperator, stage.alphaSource, TEXCMB_OPERAND_A, stage.alphaOperand, stage.alphaScale, stage.writeAlphaBuffer));

				texPipeline.appendChild(texc);
			}

			pass.appendChild(texPipeline);

			technique.appendChild(pass);
		}

		profileCommon.appendChild(technique);

		elem.appendChild(profileCommon);
		return elem;
	}

	private void appendMatColElem(Document doc, Element tech, String name, RGBA... matcol) {
		if (matcol != null) {
			for (RGBA rgba : matcol) {
				if (rgba != null) {
					Element e = doc.createElement(name);
					Element col = doc.createElement("color");
					col.setTextContent(XmlFormat.getFPRGBA(rgba));
					e.appendChild(col);
					tech.appendChild(e);
				}
			}
		}
	}

	private Element createCombinerStageElement(
		Document doc,
		String elemTag,
		TexEnvConfig.PICATextureCombinerMode operator,
		TexEnvConfig.PICATextureCombinerSource[] sources,
		DAEEnumMapper operandMapper,
		Enum[] operands,
		TexEnvConfig.Scale scale,
		boolean updateBuffer
	) {
		Element elem = doc.createElement(elemTag);
		elem.setAttribute("operator", TEXCMB_MODE.getName(operator));
		elem.setAttribute("scale", String.valueOf(scale.floatValue));
		elem.setAttribute("buffer", String.valueOf(updateBuffer));
		for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(operator); i++) {
			Element arg = doc.createElement("argument");
			arg.setAttribute("source", TEXCMB_SOURCE.getName(sources[i]));
			arg.setAttribute("operand", operandMapper.getName(operands[i]));
			if (TexEnvConfig.isTexSource(sources[i])) {
				int texIdx = sources[i].ordinal() - TexEnvConfig.PICATextureCombinerSource.TEX0.ordinal();
				if (texIdx < textures.size()) {
					arg.setAttribute("unit", textures.get(texIdx).imageId + "-texunit");
				}
			}
			elem.appendChild(arg);
		}
		return elem;
	}

	private void appendAsNewparam(Document doc, Element parent, Element param, String sid) {
		Element newparam = doc.createElement("newparam");
		newparam.setAttribute("sid", sid);
		newparam.appendChild(param);
		parent.appendChild(newparam);
	}

	public void applyToMaterial(Material mat, DAEDict<DAEImage> images, Mesh mesh) {
		if (blendOperation == null) {
			if (transparencyMode != null) {
				switch (transparencyMode) {
					case "A_ONE":
						MaterialProcessor.setAlphaBlend(mat);
						break;
				}
			}
		} else {
			mat.blendOperation = blendOperation;
		}
		if (depthColorMask != null) {
			mat.depthColorMask = depthColorMask;
		}
		if (alphaTest != null) {
			mat.alphaTest = alphaTest;
		}
		if (stencilOperation != null) {
			mat.stencilOperation = stencilOperation;
		}
		if (stencilTest != null) {
			mat.stencilTest = stencilTest;
		}
		if (faceCulling != null) {
			mat.faceCulling = faceCulling;
		}
		if (lineWidth != -1) {
			mat.metaData.putValue(ReservedMetaData.LINE_WIDTH, lineWidth);
		}

		boolean generateTexComb = texComb == null;

		if (generateTexComb) {
			for (int i = 0; i < mat.tevStages.stages.length; i++) {
				mat.tevStages.stages[i] = new TexEnvStage();
			}
		} else {
			mat.tevStages = texComb;
		}

		for (DAELUT daelut : LUTs) {
			if (daelut.imageId != null) {
				LUT lut = new LUT();
				lut.source = daelut.source;
				lut.target = daelut.target;
				setupTextureMapperImage(lut, mesh, null, daelut.imageId, images);
				mat.LUTs.add(lut);
			}
		}

		for (int i = 0; i < constantColors.length; i++) {
			if (constantColors[i] != null) {
				mat.constantColors[i] = constantColors[i];
			}
		}

		copyRGBAIfNonnull(mat.ambientColor, ambientColor);
		copyRGBAIfNonnull(mat.diffuseColor, diffuseColor);
		copyRGBAIfNonnull(mat.specular0Color, specular0Color);
		copyRGBAIfNonnull(mat.specular1Color, specular1Color);
		copyRGBAIfNonnull(mat.emissionColor, emissionColor);

		for (int i = 0; i < textures.size(); i++) {
			boolean bump = false;
			DAEEffectSlotInfo info = textures.get(i);
			if (info.imageId != null) {
				bump = info.mode.equals("bump");
				if (bump) {
					mat.bumpMode = MaterialParams.BumpMode.NORMAL;
					mat.bumpTextureIndex = mat.textures.size();
				}
				TextureMapper m = new TextureMapper();
				if (info.sampler != null) {
					m.mapU = info.sampler.wrapU;
					m.mapV = info.sampler.wrapV;
					m.textureMagFilter = info.sampler.magFilter;
					m.textureMinFilter = info.sampler.minFilter;
				}

				setupTextureMapperImage(m, mesh, info.uvSetName, info.imageId, images);

				mat.textures.add(m);
			}
			if (generateTexComb && !bump) {
				addDiffuseShadingStage(mat, i, info);
			}
		}

		if (generateTexComb) {
			MaterialProcessor.condenseShadingStages(mat);
			if (mesh.hasColor) {
				MaterialProcessor.addVcolShadingStage(mat);
			}
			if (mesh.hasNormal) {
				MaterialProcessor.enableFragmentLighting(mat);
			}
		}
	}

	private void copyRGBAIfNonnull(RGBA dest, RGBA src) {
		if (src != null) {
			dest.set(src);
		}
	}

	private void setupTextureMapperImage(TextureMapper m, Mesh mesh, String uvSetName, String imageId, DAEDict<DAEImage> images) {
		DAEImage img = images.get(imageId);
		if (img == null) {
			return;
		}
		m.textureName = img.name;

		MaterialParams.TextureMapMode mapMode = TEX_MAP_MODE_EX.getValue(uvSetName, null);
		if (mapMode == null) {
			for (MetaDataValue val : mesh.metaData.getValues()) {
				if (Objects.equals(uvSetName, val.stringValue())) {
					if (val.getName().startsWith("UvSetName")) {
						int uvSetNameTgt = Integer.parseInt(val.getName().substring("UvSetName".length()));
						m.uvSetNo = uvSetNameTgt;
						break;
					}
				}
			}
			m.mapMode = MaterialParams.TextureMapMode.UV_MAP;
		} else {
			m.mapMode = mapMode;
		}
	}

	private void addDiffuseShadingStage(Material mat, int textureIndex, DAEEffectSlotInfo info) {
		TexEnvStage textureStage = null;
		TexEnvStage colorStage = null;
		if (info.imageId != null) {
			textureStage = new TexEnvStage();
			TexEnvConfig.PICATextureCombinerSource src = getSrcForTexNum(textureIndex);
			textureStage.alphaSource[0] = src;
			textureStage.rgbSource[0] = src;
			setModOrReplace(textureIndex != 0, textureStage);
		}
		if (info.color != null && !info.color.equals(RGBA.WHITE)) {
			if (textureIndex == 0 && textureStage != null) {
				colorStage = textureStage;//don't need the previous buffer color
			} else {
				colorStage = new TexEnvStage();
			}
			int idx = colorStage == textureStage ? 1 : 0;
			colorStage.alphaSource[idx] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
			colorStage.rgbSource[idx] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
			mat.constantColors[idx] = info.color;
			setModOrReplace(textureIndex != 0 || (textureStage != null && textureIndex == 0), colorStage);
		}
		if (textureStage != null) {
			mat.tevStages.stages[textureIndex * 2] = textureStage;
		}
		if (colorStage != null) {
			mat.tevStages.stages[textureIndex * 2 + 1] = colorStage;
		}
	}

	private static void setModOrReplace(boolean isMod, TexEnvStage s) {
		if (isMod) {
			s.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
			s.rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
		} else {
			s.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
			s.rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.REPLACE;
		}
	}

	private static TexEnvConfig.PICATextureCombinerSource getSrcForTexNum(int index) {
		switch (index) {
			case 0:
				return TexEnvConfig.PICATextureCombinerSource.TEX0;
			case 1:
				return TexEnvConfig.PICATextureCombinerSource.TEX1;
			case 2:
				return TexEnvConfig.PICATextureCombinerSource.TEX2;
			case 3:
				return TexEnvConfig.PICATextureCombinerSource.TEX3;
		}
		return TexEnvConfig.PICATextureCombinerSource.PREVIOUS_STAGE;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	public static class DAELUT {

		public String imageId;
		public MaterialParams.LUTTarget target;
		public MaterialParams.LUTSource source;
	}
}
