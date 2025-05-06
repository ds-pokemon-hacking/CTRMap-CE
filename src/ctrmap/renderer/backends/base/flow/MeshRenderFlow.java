package ctrmap.renderer.backends.base.flow;

import xstandard.math.vec.Vec4f;
import xstandard.math.vec.Matrix4;
import xstandard.math.vec.Vec2f;
import xstandard.math.vec.Vec3f;
import ctrmap.renderer.backends.base.*;
import ctrmap.renderer.backends.RenderConstants;
import ctrmap.renderer.backends.base.shaderengine.ShaderProgram;
import ctrmap.renderer.backends.base.shaderengine.ShaderUniform;
import ctrmap.renderer.scene.Light;
import ctrmap.renderer.scene.animation.material.AnimatedColor;
import ctrmap.renderer.scene.animation.material.MatAnimController;
import ctrmap.renderer.scene.animation.material.MaterialAnimationColorFrame;
import ctrmap.renderer.scene.metadata.MetaData;
import ctrmap.renderer.scene.metadata.MetaDataValue;
import ctrmap.renderer.scene.model.*;
import ctrmap.renderer.scene.model.draw.buffers.BufferComponent;
import ctrmap.renderer.scene.model.draw.buffers.mesh.MeshBufferComponent;
import ctrmap.renderer.scene.model.draw.vtxlist.MorphableVertexList;
import ctrmap.renderer.scene.model.draw.vtxlist.VertexListType;
import ctrmap.renderer.scene.texturing.*;
import ctrmap.renderer.scenegraph.G3DResourceInstance;
import ctrmap.renderer.scenegraph.G3DResourceState;
import ctrmap.renderer.util.MaterialProcessor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MeshRenderFlow {

	private static final int[] TEX_UNITS_0123 = new int[]{0, 1, 2, 3};

	private static final Matrix4 SCALING_0 = Matrix4.createScale(0f, 0f, 0f);

	//allocation free + thread safe
	private static final int[] LUTTextureUnitsLUT = new int[MaterialParams.LUTTarget.values().length];
	private static final int[] textureUvSetNos = new int[RenderConstants.TEXTURE_MAX];

	public static void setUpDrawForModelMesh(
		G3DResourceState resourceState,
		Model model,
		Mesh mesh,
		RenderState renderState,
		AbstractBackend backend,
		BufferObjectMemory bufMem,
		IRenderDriver gl
	) {
		IShaderAdapter shaderHandler = backend.getShaderHandler();

		Material mat = mesh.getMaterial(model);

		G3DResourceInstance instance = resourceState.instance;

		ShaderProgram program = gl.getShaderProgram(mat);

		if (program != null) {
			if (!program.isCurrent(renderState)) {
				gl.useProgram(program);

				bufMem.registProgram(program);
			}

			shaderHandler.setUpMatrices(resourceState.modelMatrix, resourceState.viewMatrix, resourceState.getProjectionMatrix(), resourceState.normalMatrix, program, gl);

			passExtraUniforms(backend.getProgramManager().getExtraUniforms(), program, gl);

			passFragShaderUniforms(resourceState, mat, program, shaderHandler, gl);

			boolean isTexturingSuccess = false;

			if (mat != null) {
				boolean isLUTNeedsTangent = false;

				if (!renderState.hasFlag(RenderState.Flag.UNTEXTURED)) {
					isTexturingSuccess = setupMaterialTextures(resourceState, mat, bufMem, gl);
				} else {
					gl.setTexturingEnable(false);
				}

				synchronized (LUTTextureUnitsLUT) {
					int lutUnitIndex = RenderConstants.getTextureMax(mat);
					for (int i = 0; i < mat.LUTs.size(); i++, lutUnitIndex++) {
						LUT lut = mat.LUTs.get(i);
						if (lut.textureName != null) {
							Texture lutTexture = instance.getResTexture(lut.textureName);
							if (lutTexture != null) {
								setUpTexture(gl, lutTexture, lut, lutUnitIndex);
								bufMem.registTexture(lutTexture);
								LUTTextureUnitsLUT[lut.target.ordinal()] = lutUnitIndex;
								isLUTNeedsTangent |= lut.source == MaterialParams.LUTSource.PHI;
							}
						}
					}
					shaderHandler.setUpLUTAssignments(LUTTextureUnitsLUT, gl, program);
				}

				shaderHandler.setLUTNeedsTangentUniform(isLUTNeedsTangent, gl, program);
			} else {
				gl.setTexturingEnable(false);
			}

			shaderHandler.setUpMeshBoolUniforms(mesh, gl, program);
			if (mesh.vertices.getType() == VertexListType.MORPH) {
				float weight = 0f;
				MorphableVertexList mvl = (MorphableVertexList) mesh.vertices;
				if (mvl.currentMorph() != null && mvl.lastMorph() != null) {
					weight = mvl.getMorphWeight();
				}
				for (BufferComponent c : mesh.buffers.vbo.components) {
					if (c instanceof MeshBufferComponent) {
						((MeshBufferComponent) c).processVertexMorph(mvl);
					}
				}
				shaderHandler.setUpMeshBlendWeight(weight, gl, program);
			}

			passMetaDataUniforms(mesh.metaData, program, gl);
			passMetaDataUniforms(model.metaData, program, gl);
			passMetaDataUniforms(resourceState.instance.resource.metaData, program, gl);
			if (mat != null) {
				passMetaDataUniforms(mat.metaData, program, gl);
			}
			passMetaDataUniforms(resourceState.instance.metaData, program, gl);

			int jointIndex = 0;

			if (!(model.skeleton.getJoints().isEmpty() || mesh.skinningType == Mesh.SkinningType.NONE)) {
				Matrix4[] matrices = new Matrix4[Math.min(192, model.skeleton.getJointCount())];

				for (Joint j : model.skeleton) {
					Matrix4 jTransformMatrix;
					if (!resourceState.jointVisibilities.getOrDefault(j.name, true)) {
						jTransformMatrix = SCALING_0;
					} else {
						jTransformMatrix = resourceState.animatedTransforms.get(j);

						if (jTransformMatrix == null) {
							jTransformMatrix = new Matrix4();
						} else {
							jTransformMatrix = jTransformMatrix.clone();
							if (mesh.skinningType != Mesh.SkinningType.RIGID) {
								jTransformMatrix.mul(resourceState.globalBindTransforms.get(j).getInverseMatrix());
							}
						}
					}
					matrices[jointIndex] = jTransformMatrix;

					jointIndex++;
					if (jointIndex >= 192) {
						break;
					}
				}

				shaderHandler.setUpSkeletalTransforms(matrices, gl, program);
			} else {
				shaderHandler.setUpSkeletalTransforms(null, gl, program);
			}

			if (mat != null) {
				if (isTexturingSuccess || (mat.textures.isEmpty() && !MaterialProcessor.hasTEVSource(mat, TexEnvConfig.PICATextureCombinerSource.TEX0))) {
					//Sanity check: some materials might use the combiner source despite not having a texture bound
					shaderHandler.setMaterialShadingStageCount(TexEnvConfig.STAGE_COUNT, gl, program);
				} else {
					shaderHandler.setMaterialShadingStageCount(0, gl, program);
				}

				BackendMaterialOverride overrideMaterial = backend.getOverrideMaterial();

				boolean hasMatOverride = overrideMaterial.enabled();

				int lightSetIndex = hasMatOverride && overrideMaterial.hasOverrideCap(BackendMaterialOverride.OverrideCapability.LIGHT_SET_INDEX) ? overrideMaterial.lightSetIndex : mat.lightSetIndex;

				List<Light> lights = new ArrayList<>();

				for (Light r : resourceState.lights) {
					if (r.setIndex == lightSetIndex) {
						lights.add(r);
					}
				}

				int lightsHash = lights.hashCode();
				if (!(program.isCurrent(renderState) && lightsHash == renderState.lightsHash)) {

					Light[] lightsArr = lights.toArray(new Light[lights.size()]);
					Matrix4[] lightMatrices = new Matrix4[lightsArr.length];
					for (int i = 0; i < lightsArr.length; i++) {
						lightMatrices[i] = resourceState.lightMatrices.getOrDefault(lightsArr[i], new Matrix4());
					}

					shaderHandler.setUpLights(lightsArr, lightMatrices, gl, program);

					renderState.lightsHash = lightsHash;
				}

				shaderHandler.setUpMaterialUniforms(mat, gl, program);
			} else {
				shaderHandler.setUpLights(null, null, gl, program);
				shaderHandler.setMaterialShadingStageCount(0, gl, program);
			}
			renderState.program = program.handle;
		} else {
			gl.useProgram(null);
			renderState.program = null;
		}
	}

	protected static void setUpTexture(IRenderDriver gl, Texture tex, TextureMapper mapper, int textureUnit) {
		if (tex != null) {
			gl.setTexturingEnable(true);
			gl.activeTexture(textureUnit);
			uploadTexture(gl, tex);
			gl.bindTexture(tex.getPointer(gl));
			gl.setUpTextureMapper(mapper);
		}
	}

	protected static boolean setupMaterialTextures(G3DResourceState state, Material mat, BufferObjectMemory bufMem, IRenderDriver gl) {
		G3DResourceInstance instance = state.instance;
		boolean isTexturingSuccess = false;
		if (mat != null) {
			int textureMax = RenderConstants.getTextureMax(mat);
			for (int i = 0; i < textureMax; i++) {
				TextureMapper m = mat.textures.get(i);
				Texture texture = instance.getResTexture(m.textureName);
				for (MatAnimController c : state.materialAnimations) {
					if (c.textureName[i].containsKey(mat.name)) {
						Texture replacement = instance.getResTexture(c.textureName[i].get(mat.name));
						if (replacement != null) {
							texture = replacement;
							break;
						}
					}
				}
				if (texture == null) {
					texture = RenderConstants.EMPTY_TEXTURE;
				} else {
					isTexturingSuccess = true;
				}

				setUpTexture(gl, texture, m, i);
				bufMem.registTexture(texture);
			}
		} else {
			gl.setTexturingEnable(false);
		}
		return isTexturingSuccess;
	}

	protected static Vec2f getRotationByOrigin(Vec2f v, float anglerad, float origin) {
		//compensate translation with origin coordinate shift
		float s = (float) Math.sin(anglerad);
		float c = (float) Math.cos(anglerad);

		float xr = (origin - (origin * c - origin * s)) + v.x;
		float yr = (origin - (origin * c + origin * s)) + v.y;

		//return new Vec2f(x * c - y * s - txy, x * s + y * c - txy);
		return new Vec2f(xr, yr);
	}

	protected static void passExtraUniforms(List<ShaderUniform> extraUniforms, ShaderProgram program, IRenderDriver gl) {
		for (ShaderUniform u : extraUniforms) {
			gl.uniform1i(program.getUniformLocation(u.uniformName, gl), u.intValue);
		}
	}

	protected static void passMetaDataUniforms(MetaData metaData, ShaderProgram program, IRenderDriver gl) {
		for (MetaDataValue mdv : metaData.getValues()) {
			if (mdv.getIsUniform()) {
				int loc = program.getUniformLocation(mdv.getName(), gl);
				if (loc != -1) {
					switch (mdv.getType()) {
						case FLOAT:
							gl.uniform1fv(loc, mdv.floatValues());
							break;
						case INT:
							gl.uniform1iv(loc, mdv.intValues());
							break;
						case VEC3:
							Vec3f[] vectors = mdv.vec3Values();
							if (vectors.length == 0) {
								System.err.println("Uniform " + mdv.getName() + " is empty !! - values " + mdv.getValues());
							}
							gl.uniform3fv(loc, vectors);
							break;
					}
				}
			}
		}
	}

	protected static void passFragShaderUniforms(G3DResourceState state, Material mat, ShaderProgram program, IShaderAdapter adapter, IRenderDriver gl) {
		if (mat != null) {
			//System.out.println(mat.name);

			int textureMax = RenderConstants.getTextureMax(mat);

			if (textureMax > 0) {
				Vec2f[] t = new Vec2f[textureMax];
				float[] r = new float[textureMax];
				Vec2f[] s = new Vec2f[textureMax];

				for (int texUnit = 0; texUnit < textureMax; texUnit++) {
					TextureMapper m = mat.textures.get(texUnit);
					Vec2f traV = m.bindTranslation;
					t[texUnit] = new Vec2f(-traV.x, traV.y);
					r[texUnit] = m.bindRotation;

					Vec2f scaV = m.bindScale;
					s[texUnit] = new Vec2f(scaV);
				}

				List<MatAnimController> mac = state.materialAnimations;

				for (MatAnimController c : mac) {
					for (int texUnit = 0; texUnit < textureMax; texUnit++) {
						if (c.translationX[texUnit].containsKey(mat.name)) {
							t[texUnit].x = c.translationX[texUnit].get(mat.name);
						}
						if (c.translationY[texUnit].containsKey(mat.name)) {
							t[texUnit].y = c.translationY[texUnit].get(mat.name);
						}
						if (c.rotation[texUnit].containsKey(mat.name)) {
							r[texUnit] = c.rotation[texUnit].get(mat.name);
						}
						if (c.scaleX[texUnit].containsKey(mat.name)) {
							s[texUnit].x = c.scaleX[texUnit].get(mat.name);
						}
						if (c.scaleY[texUnit].containsKey(mat.name)) {
							s[texUnit].y = c.scaleY[texUnit].get(mat.name);
						}
					}
				}

				Matrix4[] transformMatrixBuf = new Matrix4[textureMax];

				for (int i = 0; i < textureMax; i++) {
					Matrix4 m = new Matrix4();
					Vec2f t0 = getRotationByOrigin(t[i], r[i], 0.5f);
					m.scale(s[i].x, s[i].y, 0);
					m.translate(t0.x, t0.y, 0);
					m.rotate(r[i], 0, 0, 1f);
					transformMatrixBuf[i] = m;
				}

				adapter.setUpTextureTransforms(transformMatrixBuf, gl, program);

				synchronized (textureUvSetNos) {
					for (int i = 0; i < textureMax; i++) {
						TextureMapper m = mat.textures.get(i);
						textureUvSetNos[i] = m.uvSetNo;
					}
					for (int i = textureMax; i < textureUvSetNos.length; i++) {
						textureUvSetNos[i] = -1;
					}

					adapter.setUpMeshUVAssignments(textureUvSetNos, gl, program);
				}
			}

			Vec4f[] lightingColors = new Vec4f[5];
			MaterialColorType[] lightingColorTypes = new MaterialColorType[5];
			//amb, dif, spc0, spc1, emi
			lightingColorTypes[0] = MaterialColorType.AMBIENT;
			lightingColorTypes[1] = MaterialColorType.DIFFUSE;
			lightingColorTypes[2] = MaterialColorType.SPECULAR0;
			lightingColorTypes[3] = MaterialColorType.SPECULAR1;
			lightingColorTypes[4] = MaterialColorType.EMISSION;
			for (int i = 0; i < lightingColors.length; i++) {
				lightingColors[i] = mat.getMaterialColor(lightingColorTypes[i]).toVector4();
			}
			for (MatAnimController mac : state.materialAnimations) {
				animateColors(mac, mat.name, lightingColors, lightingColorTypes, lightingColorTypes);
			}
			adapter.setUpMaterialLightingColors(lightingColors, gl, program);

			if (mat.tevStages != null) {
				MaterialColorType[] constantColorTypes = new MaterialColorType[TexEnvConfig.STAGE_COUNT];
				MaterialColorType[] constantColorTypesAbsolute = new MaterialColorType[TexEnvConfig.STAGE_COUNT];
				Vec4f[] constantColors = new Vec4f[TexEnvConfig.STAGE_COUNT];
				for (int i = 0; i < mat.tevStages.stages.length; i++) {
					MaterialColorType t = mat.tevStages.stages[i].constantColor;
					constantColorTypes[i] = t;
					constantColors[i] = mat.getMaterialColor(t).toVector4();
					constantColorTypesAbsolute[i] = MaterialColorType.forCColIndex(i);
				}

				for (MatAnimController mac : state.materialAnimations) {
					animateColors(mac, mat.name, constantColors, constantColorTypes, constantColorTypesAbsolute);
				}

				adapter.setUpMaterialConstantColors(constantColors, gl, program);
			}
		}

		adapter.setUpTextureAssignments(TEX_UNITS_0123, gl, program);
	}

	public static void animateColors(MatAnimController mac, String materialName, Vec4f[] colors, MaterialColorType[] colorTypes, MaterialColorType[] colorTypesAbsolute) {
		MaterialAnimationColorFrame frm = mac.colors.get(materialName);
		if (frm != null) {
			for (int i = 0; i < colorTypes.length; i++) {
				AnimatedColor animatedColor = frm.getColorForType(colorTypes[i]);
				if (animatedColor == null) {
					animatedColor = frm.getColorForTypeAbsolute(colorTypesAbsolute[i]);
				}
				if (animatedColor != null) {
					if (animatedColor.r.exists) {
						colors[i].x = animatedColor.r.value;
					}
					if (animatedColor.g.exists) {
						colors[i].y = animatedColor.g.value;
					}
					if (animatedColor.b.exists) {
						colors[i].z = animatedColor.b.value;
					}
					if (animatedColor.a.exists) {
						colors[i].w = animatedColor.a.value;
					}
				}
			}
		}
	}

	public static void uploadTexture(IRenderDriver gl, Texture t) {
		if (t.checkRequiredGeneration(gl)) {
			t.registerRenderer(gl, gl.genTexture());
		}
		if (t.checkRequestedDataReupload(gl)) {
			ByteBuffer textureData = t.getRenderableData();
			if (t.width * t.height * t.format.getNativeBPP() != textureData.remaining()) {
				System.err.println("Malformed texture data!! - texture " + t.name + " with dimensions " + t.width + "x" + t.height + " and format " + t.format.name() + " only has " + textureData.remaining() + " bytes in buffer.");
			}
			gl.bindTexture(t.getPointer(gl));
			gl.texImage2D(t.format, t.width, t.height, textureData);
		}
	}
}
