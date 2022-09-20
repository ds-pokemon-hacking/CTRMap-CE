package ctrmap.renderer.util;

import ctrmap.renderer.scene.Scene;
import ctrmap.renderer.util.texture.TextureProcessor;
import ctrmap.renderer.util.texture.TextureCodec;
import xstandard.math.vec.RGBA;
import ctrmap.renderer.scene.metadata.GFLMetaData;
import ctrmap.renderer.scene.model.Mesh;
import ctrmap.renderer.scene.model.Model;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialColorType;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;
import ctrmap.renderer.scene.texturing.Texture;
import ctrmap.renderer.scene.texturing.TextureMapper;
import ctrmap.renderer.scenegraph.G3DResource;
import ctrmap.renderer.scenegraph.G3DResourceType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MaterialProcessor {

	public static void setAutoAlphaBlendByTexture(G3DResource res, G3DResource... additionalTextureResources) {
		for (Model mdl : res.models) {
			for (Material mat : mdl.materials) {
				if (MaterialProcessor.isAlphaBlendUsed(mat)) {
					continue;
				}
				TexLoop:
				for (TextureMapper mapper : mat.textures) {
					Texture tex = (Texture) res.getNamedResource(mapper.textureName, G3DResourceType.TEXTURE);
					for (G3DResource r : additionalTextureResources) {
						if (r != null) {
							tex = (Texture) r.getNamedResource(mapper.textureName, G3DResourceType.TEXTURE);
							if (tex != null) {
								break;
							}
						}
					}
					if (tex != null) {
						if (tex.format.hasAlpha()) {
							mat.alphaTest.enabled = true;

							byte[] rgba = TextureCodec.getRGBA(tex, tex.format);
							if (TextureProcessor.hasVaryingAlpha(rgba)) {
								setAlphaBlend(mat);
								break;
							}
						}
					}
				}
			}
		}
	}

	public static boolean isAlphaBlendUsed(Material mat) {
		if (mat != null) {
			return mat.blendOperation.enabled
				&& mat.blendOperation.colorEquation == MaterialParams.BlendEquation.ADD
				&& mat.blendOperation.alphaEquation == MaterialParams.BlendEquation.ADD
				&& mat.blendOperation.alphaSrcFunc == MaterialParams.BlendFunction.SRC_ALPHA
				&& mat.blendOperation.alphaDstFunc == MaterialParams.BlendFunction.ONE_MINUS_SRC_ALPHA
				&& mat.blendOperation.colorSrcFunc == MaterialParams.BlendFunction.SRC_ALPHA
				&& mat.blendOperation.colorDstFunc == MaterialParams.BlendFunction.ONE_MINUS_SRC_ALPHA;
		}
		return false;
	}

	public static boolean isAlphaTestUsed(Material mat) {
		if (mat != null && mat.alphaTest.enabled) {
			if (mat.alphaTest.testFunction == MaterialParams.TestFunction.ALWAYS) {
				return false;
			}
			return true;
		}
		return false;
	}

	public static boolean isLightingUsed(Material mat) {
		return hasTEVSource(mat, TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR) || hasTEVSource(mat, TexEnvConfig.PICATextureCombinerSource.FRAG_SECONDARY_COLOR);
	}

	public static boolean isVCoUsed(Material mat) {
		return hasTEVSource(mat, TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR);
	}
	
	public static int getTEVSourceRgbIdx(TexEnvStage stg, TexEnvConfig.PICATextureCombinerSource src) {
		if (!stg.isPassThrough()) {
			int rgbcount = TexEnvConfig.getCombinerModeArgumentCount(stg.rgbCombineOperator);
			for (int i = 0; i < rgbcount; i++) {
				if (stg.rgbSource[i] == src) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public static int getTEVSourceAlphaIdx(TexEnvStage stg, TexEnvConfig.PICATextureCombinerSource src) {
		if (!stg.isPassThrough()) {
			int alphaCount = TexEnvConfig.getCombinerModeArgumentCount(stg.alphaCombineOperator);
			for (int i = 0; i < alphaCount; i++) {
				if (stg.alphaSource[i] == src) {
					return i;
				}
			}
		}
		return -1;
	}

	public static boolean hasTEVSource(TexEnvStage stg, TexEnvConfig.PICATextureCombinerSource src) {
		if (!stg.isPassThrough()) {
			if (getTEVSourceRgbIdx(stg, src) != -1) {
				return true;
			}
			if (getTEVSourceAlphaIdx(stg, src) != -1) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasTEVSource(Material mat, TexEnvConfig.PICATextureCombinerSource src) {
		for (TexEnvStage stg : mat.tevStages.stages) {
			if (hasTEVSource(stg, src)) {
				return true;
			}
		}
		return false;
	}

	public static void limitTextureNames(int maxLen, List<Material> materials, List<Texture> textures) {
		Map<String, String> convNames = new HashMap<>();

		for (Material mat : materials) {
			for (TextureMapper tm : mat.textures) {
				if (tm.textureName != null) {
					String ogName = tm.textureName;
					tm.textureName = tm.textureName.substring(0, Math.min(tm.textureName.length(), maxLen));

					int idx = 1;
					String get;
					while ((get = convNames.get(tm.textureName)) != null && !Objects.equals(get, ogName)) {
						String strIdx = "_" + String.valueOf(idx);
						tm.textureName = tm.textureName.substring(0, Math.min(tm.textureName.length(), maxLen - strIdx.length())) + strIdx;
						idx++;
					}

					convNames.put(tm.textureName, ogName);
				}
			}
		}

		for (Map.Entry<String, String> e : convNames.entrySet()) {
			Texture tex = Scene.getNamedObject(e.getValue(), textures);
			if (tex != null) {
				tex.name = e.getKey();
			}
		}
	}

	public static enum BakeMode {
		SHADOW,
		LIGHT,
		COMB
	}

	public static BakeResult addBakeMap(Material mat, String texName, int uvSetNo, BakeMode mode) {
		TextureMapper t = null;
		for (TextureMapper tm : mat.textures) {
			if (tm.textureName.equals(texName)) {
				t = tm;
				break;
			}
		}
		if (t == null) {
			if (mat.textures.size() < 3) {
				t = new TextureMapper();
				mat.textures.add(t);
			} else {
				return BakeResult.NOT_ENOUGH_TEXTURE_SLOTS;
			}
		}

		TexEnvStage s = null;

		for (TexEnvStage stage : mat.tevStages.stages) {
			if (stage.isPassThrough() || stage.rgbCombineOperator == TexEnvConfig.PICATextureCombinerMode.REPLACE) {
				s = stage;
				break;
			}
		}

		if (s == null) {
			System.out.println("Not enough shading stages for bake");
			return BakeResult.NOT_ENOUGH_SHADING_STAGES;
		}

		t.textureName = texName;
		t.uvSetNo = uvSetNo;

		int texIdx = mat.textures.indexOf(t);
		TexEnvConfig.PICATextureCombinerSource src = TexEnvConfig.PICATextureCombinerSource.getTexSource(texIdx);
		switch (mode) {
			case LIGHT:
			case SHADOW:
				s.rgbSource[1] = src;
				s.rgbOperand[1] = TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR;
				s.rgbCombineOperator = mode == BakeMode.LIGHT ? TexEnvConfig.PICATextureCombinerMode.ADD : TexEnvConfig.PICATextureCombinerMode.MODULATE;
				break;
			case COMB:
				s.rgbSource[1] = src;
				s.rgbSource[2] = src;
				s.rgbOperand[1] = TexEnvConfig.PICATextureCombinerColorOp.RED;
				s.rgbOperand[2] = TexEnvConfig.PICATextureCombinerColorOp.GREEN;
				s.rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.MULT_ADD;
				break;
		}

		return BakeResult.SUCCESS;
	}

	public static enum BakeResult {
		SUCCESS,
		NOT_ENOUGH_TEXTURE_SLOTS,
		NOT_ENOUGH_SHADING_STAGES
	}

	public static void condenseShadingStages(Material mat) {
		TexEnvStage[] stages = mat.tevStages.stages;
		for (int i = 0; i < stages.length; i++) {
			if (stages[i].isPassThrough()) {
				for (int j = i + 1; j < stages.length; j++) {
					stages[j - 1] = stages[j];
				}
			}
		}
	}

	public static void addVcolShadingStage(Material mat) {
		if (isVCoUsed(mat)) {
			return;
		}
		for (TexEnvStage s : mat.tevStages.stages) {
			if (s.rgbCombineOperator == TexEnvConfig.PICATextureCombinerMode.REPLACE && s.alphaCombineOperator == TexEnvConfig.PICATextureCombinerMode.REPLACE) {
				int idx = s.isPassThrough() ? 0 : 1;
				s.alphaSource[idx] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
				s.rgbSource[idx] = TexEnvConfig.PICATextureCombinerSource.PRIMARY_COLOR;
				if (idx == 1) {
					s.rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
					s.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
				}
				break;
			}
		}
	}

	public static void setAlphaBlend(Material mat) {
		if (mat != null) {
			mat.blendOperation.enabled = true;
			mat.blendOperation.colorEquation = MaterialParams.BlendEquation.ADD;
			mat.blendOperation.alphaEquation = MaterialParams.BlendEquation.ADD;
			mat.blendOperation.alphaSrcFunc = MaterialParams.BlendFunction.SRC_ALPHA;
			mat.blendOperation.alphaDstFunc = MaterialParams.BlendFunction.ONE_MINUS_SRC_ALPHA;
			mat.blendOperation.colorSrcFunc = MaterialParams.BlendFunction.SRC_ALPHA;
			mat.blendOperation.colorDstFunc = MaterialParams.BlendFunction.ONE_MINUS_SRC_ALPHA;

			if (mat.parentModel != null) {
				for (Mesh mesh : mat.parentModel.meshes) {
					if (Objects.equals(mesh.materialName, mat.name) && mesh.renderLayer < 1) {
						mesh.renderLayer = 1;
					}
				}
			}
		}
	}

	public static void unsetAlphaBlend(Material mat) {
		if (mat != null) {
			mat.blendOperation.enabled = true;
			mat.blendOperation.colorEquation = MaterialParams.BlendEquation.ADD;
			mat.blendOperation.alphaEquation = MaterialParams.BlendEquation.ADD;
			mat.blendOperation.alphaSrcFunc = MaterialParams.BlendFunction.ONE;
			mat.blendOperation.alphaDstFunc = MaterialParams.BlendFunction.ZERO;
			mat.blendOperation.colorSrcFunc = MaterialParams.BlendFunction.ONE;
			mat.blendOperation.colorDstFunc = MaterialParams.BlendFunction.ZERO;

			if (mat.parentModel != null) {
				for (Mesh mesh : mat.parentModel.meshes) {
					if (Objects.equals(mesh.materialName, mat.name) && mesh.renderLayer > 0) {
						mesh.renderLayer = 0;
					}
				}
			}
		}
	}

	public static void setEdgeMetaData(Material mat, boolean isEdgeEnable, int edgeId) {
		if (mat != null) {
			mat.metaData.putValue(GFLMetaData.GFL_EDGE_TYPE, 2);
			mat.metaData.putValue(GFLMetaData.GFL_EDGE_MAP_ALPHA_MASK, 0);
			mat.metaData.putValue(GFLMetaData.GFL_EDGE_OFFSET_ENABLE, 1);
			mat.metaData.putValue(GFLMetaData.GFL_EDGE_ENABLE, isEdgeEnable ? 1 : 0);
			mat.metaData.putValue(GFLMetaData.GFL_EDGE_ID, edgeId);

			mat.stencilOperation.fail = MaterialParams.StencilOp.KEEP;
			mat.stencilOperation.zFail = MaterialParams.StencilOp.KEEP;
			mat.stencilOperation.zPass = MaterialParams.StencilOp.REPLACE;

			mat.stencilTest.enabled = true;
			mat.stencilTest.testFunction = MaterialParams.TestFunction.ALWAYS;
			mat.stencilTest.funcMask = 255;
			mat.stencilTest.bufferMask = 255;
			mat.stencilTest.reference = edgeId;
		}
	}

	public static void enableFragmentLighting(Material mat) {
		if (mat != null) {
			for (TexEnvStage stage : mat.tevStages.stages) {
				for (TexEnvConfig.PICATextureCombinerSource src : stage.rgbSource) {
					if (src == TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR) {
						return;
					}
				}
				for (TexEnvConfig.PICATextureCombinerSource src : stage.alphaSource) {
					if (src == TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR) {
						return;
					}
				}
			}

			for (int i = 1; i < mat.tevStages.stages.length; i++) {
				//For an unknown reason, enabling fragment lighting in the 1st combiner stage disables it completely
				//This could either be a GFL bug/feature or a hardware thing. Most likely the former.
				TexEnvStage stage = mat.tevStages.stages[i];
				if (stage.isPassThrough()) {
					stage.rgbCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
					stage.alphaCombineOperator = TexEnvConfig.PICATextureCombinerMode.MODULATE;
					stage.rgbSource[1] = TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR;
					stage.alphaSource[1] = TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR;
					stage.rgbOperand[1] = TexEnvConfig.PICATextureCombinerColorOp.SRC_COLOR;
					stage.alphaOperand[1] = TexEnvConfig.PICATextureCombinerAlphaOp.SRC_ALPHA;

					break;
				}
			}
		}
	}

	public static Material createConstantMaterial(String materialName, RGBA constantColor, int separateAlpha) {
		Material mat = createConstantMaterial(materialName, constantColor);
		TexEnvStage stage1 = new TexEnvStage();
		stage1.alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
		mat.constantColors[1] = new RGBA(0, 0, 0, separateAlpha);
		stage1.constantColor = MaterialColorType.CONSTANT1;
		mat.tevStages.stages[1] = stage1;
		mat.faceCulling = MaterialParams.FaceCulling.BACK_FACE;
		if (separateAlpha < 255) {
			MaterialProcessor.setAlphaBlend(mat);
		}
		return mat;
	}

	public static Material createConstantMaterial(String materialName, RGBA constantColor) {
		Material mat = new Material();
		mat.name = materialName;
		TexEnvStage stage0 = new TexEnvStage();
		mat.constantColors[0] = constantColor;
		stage0.constantColor = MaterialColorType.CONSTANT0;
		stage0.alphaSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
		stage0.rgbSource[0] = TexEnvConfig.PICATextureCombinerSource.CONSTANT;
		mat.tevStages.stages[0] = stage0;
		mat.faceCulling = MaterialParams.FaceCulling.BACK_FACE;
		if (constantColor.a == 0) {
			mat.alphaTest.enabled = true;
			mat.alphaTest.reference = 0;
			mat.alphaTest.testFunction = MaterialParams.TestFunction.GREATER;
		} else if (constantColor.a < 255) {
			MaterialProcessor.setAlphaBlend(mat);
		}
		return mat;
	}
}
