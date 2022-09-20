package ctrmap.formats.generic.collada.structs;

import ctrmap.formats.generic.collada.XmlFormat;
import ctrmap.renderer.scene.texturing.MaterialParams;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DAESampler2D implements DAESerializable {

	public static final DAEEnumMapper<MaterialParams.TextureWrap> TEX_WRAP_ENUM = new DAEEnumMapper<>(
		MaterialParams.TextureWrap.REPEAT,
		"WRAP", MaterialParams.TextureWrap.REPEAT,
		"MIRROR", MaterialParams.TextureWrap.MIRRORED_REPEAT,
		"CLAMP", MaterialParams.TextureWrap.CLAMP_TO_EDGE,
		"BORDER", MaterialParams.TextureWrap.CLAMP_TO_BORDER,
		"NONE", MaterialParams.TextureWrap.CLAMP_TO_BORDER
	);

	public static final DAEEnumMapper<MaterialParams.TextureMinFilter> TEX_FILTER_MIN = new DAEEnumMapper<>(
		MaterialParams.TextureMinFilter.LINEAR,
		"NEAREST", MaterialParams.TextureMinFilter.NEAREST_NEIGHBOR,
		"LINEAR", MaterialParams.TextureMinFilter.LINEAR,
		"NEAREST_MIPMAP_NEAREST", MaterialParams.TextureMinFilter.NEAREST_MIPMAP_NEAREST,
		"LINEAR_MIPMAP_NEAREST", MaterialParams.TextureMinFilter.LINEAR_MIPMAP_NEAREST,
		"NEAREST_MIPMAP_LINEAR", MaterialParams.TextureMinFilter.NEAREST_MIPMAP_LINEAR,
		"LINEAR_MIPMAP_LINEAR", MaterialParams.TextureMinFilter.LINEAR_MIPMAP_LINEAR,
		"NONE", MaterialParams.TextureMinFilter.LINEAR
	);

	public static final DAEEnumMapper<MaterialParams.TextureMagFilter> TEX_FILTER_MAG = new DAEEnumMapper<>(
		MaterialParams.TextureMagFilter.LINEAR,
		"NEAREST", MaterialParams.TextureMagFilter.NEAREST_NEIGHBOR,
		"LINEAR", MaterialParams.TextureMagFilter.LINEAR,
		"NONE", MaterialParams.TextureMagFilter.LINEAR
	);

	public String tagName;

	public String surfaceName;

	public MaterialParams.TextureWrap wrapU;
	public MaterialParams.TextureWrap wrapV;

	public MaterialParams.TextureMinFilter minFilter;
	public MaterialParams.TextureMagFilter magFilter;

	public DAESampler2D() {
		
	}
	
	public DAESampler2D(Element sampler2D) {
		tagName = sampler2D.getTagName();
		surfaceName = XmlFormat.getParamNodeValue(sampler2D, "source");

		wrapU = XmlFormat.getParamNodeEnum(sampler2D, "wrap_s", TEX_WRAP_ENUM);
		wrapV = XmlFormat.getParamNodeEnum(sampler2D, "wrap_t", TEX_WRAP_ENUM);
		minFilter = XmlFormat.getParamNodeEnum(sampler2D, "minfilter", TEX_FILTER_MIN);
		magFilter = XmlFormat.getParamNodeEnum(sampler2D, "magfilter", TEX_FILTER_MAG);
	}

	@Override
	public Element createElement(Document doc) {
		Element elem = doc.createElement(tagName);

		if (surfaceName != null) {
			elem.appendChild(XmlFormat.createSimpleTextContentElem(doc, "source", surfaceName));
		}

		XmlFormat.setParamNodeEnum(doc, elem, "wrap_s", wrapU, TEX_WRAP_ENUM);
		XmlFormat.setParamNodeEnum(doc, elem, "wrap_s", wrapV, TEX_WRAP_ENUM);
		XmlFormat.setParamNodeEnum(doc, elem, "minfilter", minFilter, TEX_FILTER_MIN);
		XmlFormat.setParamNodeEnum(doc, elem, "magfilter", magFilter, TEX_FILTER_MAG);

		return elem;
	}

}
