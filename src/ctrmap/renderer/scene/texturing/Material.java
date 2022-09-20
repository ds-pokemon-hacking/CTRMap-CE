package ctrmap.renderer.scene.texturing;

import ctrmap.renderer.scenegraph.NamedResource;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.model.Model;
import xstandard.math.vec.RGBA;
import xstandard.util.ArraysEx;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Material implements NamedResource {

	public Model parentModel;

	public String name;

	public String vertexShaderName = "DefaultShader";
	public String fragmentShaderName = null;
	public MaterialParams.FragmentShaderType fshType = MaterialParams.FragmentShaderType.CTR_COMBINER;
	public List<String> shaderExtensions = new ArrayList<>();

	public List<TextureMapper> textures = new ArrayList<>();

	public TexEnvConfig tevStages = new TexEnvConfig();

	public MaterialParams.BlendOperation blendOperation = new MaterialParams.BlendOperation();
	public MaterialParams.AlphaTest alphaTest = new MaterialParams.AlphaTest();

	public MaterialParams.DepthColorMask depthColorMask = new MaterialParams.DepthColorMask();
	public MaterialParams.StencilTest stencilTest = new MaterialParams.StencilTest();
	public MaterialParams.StencilOperation stencilOperation = new MaterialParams.StencilOperation();

	public MaterialParams.FaceCulling faceCulling = MaterialParams.FaceCulling.NEVER;

	public int lightingLayer = 0;
	public int lightSetIndex = 0;

	public RGBA ambientColor = new RGBA(RGBA.WHITE);
	public RGBA diffuseColor = new RGBA(RGBA.WHITE);
	public RGBA specular0Color = new RGBA(RGBA.BLACK);
	public RGBA specular1Color = new RGBA(RGBA.BLACK);
	public RGBA emissionColor = new RGBA(RGBA.BLACK);

	public final RGBA[] constantColors = new RGBA[TexEnvConfig.STAGE_COUNT];

	public boolean fogEnabled;
	public int fogIndex;

	public MaterialParams.BumpMode bumpMode = MaterialParams.BumpMode.NONE;
	public int bumpTextureIndex = -1;

	public List<LUT> LUTs = new ArrayList<>();

	public MetaData metaData = new MetaData();

	public Material() {
		for (int i = 0; i < constantColors.length; i++) {
			constantColors[i] = new RGBA(RGBA.WHITE);
		}
	}

	public Material(Material src) {
		this();
		name = src.name;
		for (TextureMapper mapper : src.textures) {
			textures.add(new TextureMapper(mapper));
		}
		for (LUT l : LUTs) {
			this.LUTs.add(new LUT(l));
		}
		metaData = src.metaData;
		tevStages = src.tevStages;
		bumpMode = src.bumpMode;
		bumpTextureIndex = src.bumpTextureIndex;
		parentModel = src.parentModel;
		vertexShaderName = src.vertexShaderName;
		fragmentShaderName = src.fragmentShaderName;
		shaderExtensions.addAll(src.shaderExtensions);
		depthColorMask = src.depthColorMask;
		alphaTest = src.alphaTest;
		blendOperation = src.blendOperation;
		stencilOperation = src.stencilOperation;
		stencilTest = src.stencilTest;
		faceCulling = src.faceCulling;
		lightSetIndex = src.lightSetIndex;
		lightingLayer = src.lightingLayer;
		ambientColor = new RGBA(src.ambientColor);
		diffuseColor = new RGBA(src.diffuseColor);
		specular0Color = new RGBA(src.specular0Color);
		specular1Color = new RGBA(src.specular1Color);
		emissionColor = new RGBA(src.emissionColor);
		for (int i = 0; i < constantColors.length; i++) {
			constantColors[i] = new RGBA(src.constantColors[i]);
		}
		fogEnabled = src.fogEnabled;
		fogIndex = src.fogIndex;
		metaData = new MetaData(src.metaData);
	}

	@Override
	public String getName() {
		return name;
	}

	public void setMaterialColor(MaterialColorType type, RGBA color) {
		int ord = type.ordinal();
		if (ord >= MaterialColorType.CONSTANT0.ordinal() && ord <= MaterialColorType.CONSTANT5.ordinal()) {
			constantColors[ord - MaterialColorType.CONSTANT0.ordinal()] = color;
		} else {
			switch (type) {
				case AMBIENT:
					ambientColor = color;
					break;
				case DIFFUSE:
					diffuseColor = color;
					break;
				case EMISSION:
					emissionColor = color;
					break;
				case SPECULAR0:
					specular0Color = color;
					break;
				case SPECULAR1:
					specular1Color = color;
					break;
			}
		}
	}

	public RGBA getMaterialColor(MaterialColorType type) {
		int ord = type.ordinal();
		if (ord >= MaterialColorType.CONSTANT0.ordinal() && ord <= MaterialColorType.CONSTANT5.ordinal()) {
			return constantColors[ord - MaterialColorType.CONSTANT0.ordinal()];
		}

		switch (type) {
			case AMBIENT:
				return ambientColor;
			case DIFFUSE:
				return diffuseColor;
			case EMISSION:
				return emissionColor;
			case SPECULAR0:
				return specular0Color;
			case SPECULAR1:
				return specular1Color;
		}

		return new RGBA();
	}

	public void addShaderExtension(String name) {
		ArraysEx.addIfNotNullOrContains(shaderExtensions, name);
	}

	public void removeShaderExtension(String name) {
		shaderExtensions.remove(name);
	}

	public String[] getShaderExtensions() {
		return shaderExtensions.toArray(new String[shaderExtensions.size()]);
	}

	public LUT getLUTForTarget(MaterialParams.LUTTarget tgt) {
		for (LUT n : LUTs) {
			if (n.target == tgt) {
				return n;
			}
		}
		return null;
	}

	public MaterialParams.TextureMapMode getMapMode(int textureIndex) {
		if (textureIndex < textures.size()) {
			return textures.get(textureIndex).mapMode;
		}
		return MaterialParams.TextureMapMode.UV_MAP;
	}

	public int getShadingHash() {
		int hash = 7;
		hash = 37 * hash + vertexShaderName.hashCode();
		hash = 37 * hash + getFragmentShaderHash();
		return hash;
	}

	public int getFragmentShaderHash() {
		int hash = 7;
		hash = 37 * hash + shaderExtensions.hashCode();
		if (fshType == MaterialParams.FragmentShaderType.CTR_COMBINER) {
			hash = 37 * hash + Objects.hashCode(this.tevStages);
			for (MaterialParams.LUTTarget tgt : MaterialParams.LUTTarget.values()) {
				hash = 37 * hash + Objects.hashCode(getLUTForTarget(tgt));
			}
			int textureIndex = 0;
			for (TextureMapper m : textures) {
				if (m.mapMode != MaterialParams.TextureMapMode.UV_MAP) {
					hash = 37 * hash + Objects.hashCode(m.mapMode);
					hash = 37 * hash + ((Integer) textureIndex).hashCode();
				} else {
					hash *= 37; //do not discern between uvsets 0 to 2
				}
				textureIndex++;
			}
		} else {
			hash = 37 * hash + fragmentShaderName.hashCode();
		}

		return hash;
	}

	public int getFullHash() {
		int hash = getTexturingHash();
		hash = 37 * hash + Objects.hashCode(name);
		return hash;
	}

	public int getTexturingHash() {
		int hash = getShadingHash();
		for (TextureMapper m : textures) {
			hash = 37 * hash + Objects.hashCode(m.textureName);
			hash = 37 * hash + Objects.hashCode(m.uvSetNo);
		}
		hash = 37 * hash + Objects.hashCode(alphaTest);
		hash = 37 * hash + Objects.hashCode(depthColorMask);
		hash = 37 * hash + Objects.hashCode(blendOperation);
		return hash;
	}

	public boolean hasTexture(int index) {
		return textures.size() > index;
	}

	public boolean hasTextureName(String name) {
		for (TextureMapper m : textures) {
			if (Objects.equals(m.textureName, name)) {
				return true;
			}
		}
		return false;
	}

	public int getTextureMapperIndex(String name) {
		for (int i = 0; i < textures.size(); i++) {
			if (Objects.equals(textures.get(i).textureName, name)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
