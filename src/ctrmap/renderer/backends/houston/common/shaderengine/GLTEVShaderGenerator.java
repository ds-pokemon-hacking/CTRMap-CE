package ctrmap.renderer.backends.houston.common.shaderengine;

import ctrmap.renderer.scene.texturing.LUT;
import ctrmap.renderer.scene.texturing.Material;
import ctrmap.renderer.scene.texturing.MaterialParams;
import ctrmap.renderer.scene.texturing.TexEnvConfig;
import ctrmap.renderer.scene.texturing.TexEnvStage;

public class GLTEVShaderGenerator {

	public static String createShader(Material mat) {
		StringBuilder sb = new StringBuilder();
		/*sb.append("#version 110\n\n");

		sb.append("#define TEXTURE_MAX ");
		sb.append(GL2BackendBase.CTR_GL_TEXTURE_COUNT_MAX);
		sb.append("\n");*/

		sb.append("//EXTENSION-frag-define\n\n");

		sb.append("#include FshBase.fsh_chunk\n\n");

		sb.append("uniform sampler2D textures[TEXTURE_MAX];\n\n");
		sb.append("uniform sampler2D LUT[LUT_MAX];\n");

		//sb.append(lightingBase);
		sb.append("uniform vec4 constantColor[SHADING_STAGE_MAX];\n");

		sb.append("uniform int activeStages;\n");

		/*sb.append("varying vec2 uv0;\n");
		sb.append("varying vec2 uv1;\n");
		sb.append("varying vec2 uv2;\n");
		sb.append("varying vec4 color;\n");
		sb.append("varying vec3 f_normal;\n");
		sb.append("varying vec3 f_tangent;\n");
		sb.append("varying vec3 f_view;\n");*/
		boolean needsFragPriColor = false;
		boolean needsFragSecColor = false;

		if (mat != null) {
			Outer:
			for (TexEnvStage s : mat.tevStages.stages) {
				for (int i = 0; i < 3; i++) {
					if (!needsFragPriColor) {
						needsFragPriColor = s.rgbSource[i] == TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR || s.alphaSource[i] == TexEnvConfig.PICATextureCombinerSource.FRAG_PRIMARY_COLOR;
					}
					if (!needsFragSecColor) {
						needsFragSecColor = s.rgbSource[i] == TexEnvConfig.PICATextureCombinerSource.FRAG_SECONDARY_COLOR || s.alphaSource[i] == TexEnvConfig.PICATextureCombinerSource.FRAG_SECONDARY_COLOR;
					}
				}
			}
		}

		//Extensions might require those - generate stubs anyway
		//if (needsFragPriColor) {
			sb.append("vec4 fragmentLightingPrimaryColor = emissionColor;\n");
		//}
		//if (needsFragSecColor) {
			sb.append("vec4 fragmentLightingSecondaryColor = vec4(0.0, 0.0, 0.0, 1.0);\n");
		//}

		if (mat != null && (needsFragPriColor || needsFragSecColor)) {
			sb.append("void genLightingColors(){\n");

			boolean hasPrecalcHalf = false;

			for (LUT lut : mat.LUTs) {
				if (getSourceNeedsPrecalcHalf(lut.source)) {
					hasPrecalcHalf = true;
					break;
				}
			}

			//always precalc view
			sb.append("vec3 v = normalize(f_view);\n");

			//light loop
			sb.append("for (int i = 0; i < lightCount; i++) {\n");
			//sb.append("Light light = lights[i];\n");

			if (hasPrecalcHalf) {
				sb.append("vec3 h = ");
				sb.append(LUTIN_HALF);
				sb.append(";\n");
			}

			LUT fresnelPri = mat.getLUTForTarget(MaterialParams.LUTTarget.FRESNEL_PRI);
			LUT fresnelSec = mat.getLUTForTarget(MaterialParams.LUTTarget.FRESNEL_SEC);

			if (needsFragPriColor) {
				//Diffuse
				sb.append("vec3 lightPos = lights[i].directional ? -lights[i].direction : (lights[i].position + v);\n");
				sb.append("fragmentLightingPrimaryColor += max(0.0, dot(normal, normalize(lightPos))) * lights[i].colors[LIGHT_CMN_COLOR_DIF_IDX] * diffuseColor + lights[i].colors[LIGHT_CMN_COLOR_AMB_IDX] * ambientColor;\n");
			}

			if (needsFragSecColor) {
				sb.append("vec4 specular1 = specular1Color;\n");

				boolean hasReflecR = false;
				boolean hasReflecG = false;
				boolean hasReflecB = false;

				for (LUT lut : mat.LUTs) {
					if (lut.target.ordinal() < 3) {
						String LUTChannel = "a";
						switch (lut.target) {
							case REFLEC_R:
								LUTChannel = "r";
								hasReflecR = true;
								break;
							case REFLEC_G:
								LUTChannel = "g";
								hasReflecG = true;
								break;
							case REFLEC_B:
								LUTChannel = "b";
								hasReflecB = true;
								break;
						}
						sb.append("specular1.");
						sb.append(LUTChannel);
						sb.append(" += ");
						sb.append(getLUTString(lut));
						sb.append(";\n");
					}
				}

				if (!hasReflecR) {
					sb.append("specular1.r = 1.0;\n");
				}

				if (!hasReflecG) {
					sb.append("specular1.g = specular1.r;\n");
				}
				
				if (!hasReflecB) {
					sb.append("specular1.b = specular1.g;\n");
				}

				sb.append("fragmentLightingSecondaryColor += specular1 * lights[i].colors[LIGHT_CMN_COLOR_SPC1_IDX] + specular0Color * lights[i].colors[LIGHT_CMN_COLOR_SPC0_IDX];\n");

			}

			if (fresnelPri != null || fresnelSec != null) {
				sb.append("if (i == lightCount - 1) {\n");

				if (fresnelPri != null) {
					sb.append("fragmentLightingPrimaryColor.a = ");
					sb.append(getLUTString(fresnelPri));
					sb.append(";");
				}
				if (fresnelSec != null) {
					sb.append("fragmentLightingSecondaryColor.a = ");
					sb.append(getLUTString(fresnelSec));
					sb.append(";");
				}

				sb.append("}");
			}

			sb.append("}\n");

			sb.append("if (lightCount == 0) {\n");
			if (needsFragPriColor) {
				sb.append("fragmentLightingPrimaryColor = vec4(1);\n");
			}
			if (needsFragSecColor) {
				sb.append("fragmentLightingSecondaryColor = vec4(0, 0, 0, 1);\n");
			}
			sb.append("}\n");
			sb.append("else {\n");
			if (needsFragPriColor) {
				sb.append("fragmentLightingPrimaryColor = min(vec4(1), fragmentLightingPrimaryColor);\n");
			}
			if (needsFragSecColor) {
				sb.append("fragmentLightingSecondaryColor = min(vec4(1), fragmentLightingSecondaryColor);\n");
			}
			sb.append("}\n");

			sb.append("}\n");
		}

		sb.append("//EXTENSION-frag-init\n");

		sb.append("void main(void)\n{\n");

		sb.append("//EXTENSION-frag-main\n");

		sb.append("vec4 outColor = color;\n");

		if (mat != null && mat.tevStages != null) {
			/*if (needsSphereCoord) {
				//This sphere UV calculation is not the standard reflection map procedure, rather a reimplementation of the one used in 0@BattleChar (Courtesy of SPICA)
				//reg_temp[1].xy = vec4(0.125, 0.00390625, 0.5, 0.25).zz;
				//reg_temp[1].zw = vec4(0, 1, 2, 3).xx;
				//out.xyzw = normal.xyzw * reg_temp[1].xyzw + reg_temp[1].xyzw;
				//out.zw = vec4(0, 1, 2, 3).yy;
				//
				//can be shortened to:
				sb.append("vec2 ");
				sb.append(TEXIN_SPHERE_PRECALC);
				sb.append(" = (normal * 0.5 + 0.5).xy;\n");
			}*/
			if (needsFragPriColor || needsFragSecColor) {
				sb.append("genLightingColors();");
			}

			sb.append("vec4 tevBuffer = vec4");
			sb.append(mat.tevStages.inputBufferColor.toVector4().toString());
			sb.append(";\n");

			sb.append("vec4 tevColor = tevBuffer;\n");

			sb.append("if (activeStages > 0){\n");

			int stageIdx = 0;
			for (TexEnvStage stage : mat.tevStages.stages) {
				if (stage.isPassThrough()) {
					stageIdx++;
					continue;
				}
				if (stage.writeAlphaBuffer) {
					sb.append("tevBuffer.a = tevColor.a;\n");
				}
				if (stage.writeColorBuffer) {
					sb.append("tevBuffer.rgb = tevColor.rgb;\n");
				}

				sb.append("tevColor = vec4(");

				String[] colorArgs = new String[3];
				String[] alphaArgs = new String[3];

				for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(stage.rgbCombineOperator); i++) {
					colorArgs[i] = getColorArg(stage.rgbSource[i], stage.rgbOperand[i], stageIdx, mat);
				}
				for (int i = 0; i < TexEnvConfig.getCombinerModeArgumentCount(stage.alphaCombineOperator); i++) {
					alphaArgs[i] = getAlphaArg(stage.alphaSource[i], stage.alphaOperand[i], stageIdx, mat);
				}

				sb.append("(");
				sb.append(getOperation(stage.rgbCombineOperator, colorArgs));
				sb.append(").rgb");
				float rgbScale = stage.getRgbScaleFloat();
				if (rgbScale != 1f) {
					sb.append(" * ");
					sb.append(rgbScale);
				}
				sb.append(", ");

				sb.append(getOperation(stage.alphaCombineOperator, alphaArgs));
				float aScale = stage.getAlphaScaleFloat();
				if (aScale != 1f) {
					sb.append(" * ");
					sb.append(aScale);
				}

				sb.append(");\n");
				stageIdx++;
			}

			sb.append("outColor = tevColor;\n");
			sb.append("}\n");
		}
		sb.append("\n//EXTENSION-frag-post target=outColor\n\n");

		sb.append("#ifdef GL4\n");
		sb.append("processAlphaTest(outColor.a);\n");
		sb.append("#endif\n");

		sb.append("OUTPUT(RT_SURFACE_MAIN) = outColor;\n");

		sb.append("}");

		return sb.toString();
	}

	private static String getLUTString(LUT lut) {
		StringBuilder sb = new StringBuilder();
		sb.append("texture2D(LUT[");
		sb.append(lut.target.ordinal());
		sb.append("], vec2((");
		sb.append(getLUTSourceString(lut.source));
		sb.append(") * 0.5, 0.0)).r");
		return sb.toString();
	}

	private static boolean getSourceNeedsPrecalcHalf(MaterialParams.LUTSource src) {
		switch (src) {
			case PHI:
				return true;
		}
		return false;
	}

	private static final String LUTIN_NORMAL = "normal";
	private static final String LUTIN_TANGENT = "f_tangent";
	private static final String LUTIN_HALF = "normalize(v + normalize(lights[i].directional ? -lights[i].direction : lights[i].position + v))"; //we don't need to use a precalc light vec because half and light are never used together
	private static final String LUTIN_LIGHT = "normalize(lights[i].directional ? -lights[i].direction : lights[i].position + v)";
	private static final String LUTIN_PRECALC_HALF = "h";
	private static final String LUTIN_PRECALC_VIEW = "v";

	private static String getLUTSourceString(MaterialParams.LUTSource source) {
		StringBuilder sb = new StringBuilder();
		sb.append("dot(");
		String op1 = "vec3(0)";
		String op2 = "vec3(0)";

		switch (source) {
			case LIGHT_NORMAL:
				op1 = LUTIN_LIGHT;
				op2 = LUTIN_NORMAL;
				break;
			case LIGHT_SPOT:
				op1 = LUTIN_LIGHT;
				op2 = "lights[i].direction";
				break;
			case NORMAL_HALF:
				op1 = LUTIN_NORMAL;
				op2 = LUTIN_HALF;
				break;
			case NORMAL_VIEW:
				op1 = LUTIN_NORMAL;
				op2 = LUTIN_PRECALC_VIEW;
				break;
			case VIEW_HALF:
				op1 = LUTIN_PRECALC_VIEW;
				op2 = LUTIN_HALF;
				break;
			case PHI:
				op1 = "h - " + LUTIN_NORMAL + " / dot(" + LUTIN_NORMAL + ", " + LUTIN_NORMAL + ") * dot(" + LUTIN_NORMAL + ", h)";
				op2 = LUTIN_TANGENT;
				break;
		}

		sb.append(op1);
		sb.append(", ");
		sb.append(op2);

		sb.append(")");
		return sb.toString();
	}

	private static String getOperation(TexEnvConfig.PICATextureCombinerMode mode, String[] args) {
		String s = null;
		switch (mode) {
			case ADD:
				s = "min(%s + %s, 1.0)";
				break;
			case ADD_MULT:
				s = "min(%s + %s, 1.0) * %s";
				break;
			case ADD_SIGNED:
				s = "clamp((%s + %s) - 0.5, 0.0, 1.0)";
				break;
			case DOT3_RGB:
				s = "min(dot(vec3(%s), vec3(%s)), 1.0)";
				break;
			case DOT3_RGBA:
				s = "min(dot(vec4(%s), vec4(%s)), 1.0)";
				break;
			case INTERPOLATE:
				//special case - args not in order
				return String.format("mix(%s, %s, %s)", args[1], args[0], args[2]);
			case MODULATE:
				s = "%s * %s";
				break;
			case MULT_ADD:
				s = "min((%s * %s) + %s, 1.0)";
				break;
			case REPLACE:
				s = "%s";
				break;
			case SUBTRACT:
				s = "max(%s - %s, 0.0)";
				break;
		}
		return String.format(s, (Object[]) args);
	}

	private static String getAlphaArg(TexEnvConfig.PICATextureCombinerSource src, TexEnvConfig.PICATextureCombinerAlphaOp op, int stageIndex, Material mat) {
		StringBuilder sb = new StringBuilder();

		boolean oneMinus = false;
		switch (op) {
			case ONE_MINUS_BLUE:
			case ONE_MINUS_GREEN:
			case ONE_MINUS_RED:
			case ONE_MINUS_SRC_ALPHA:
				oneMinus = true;
				break;
		}
		if (oneMinus) {
			sb.append("(1.0 - ");
		}

		sb.append(getSource(src, stageIndex, mat));
		sb.append(".");

		String components = null;
		switch (op) {
			case BLUE:
			case ONE_MINUS_BLUE:
				components = "b";
				break;
			case GREEN:
			case ONE_MINUS_GREEN:
				components = "g";
				break;
			case RED:
			case ONE_MINUS_RED:
				components = "r";
				break;
			case SRC_ALPHA:
			case ONE_MINUS_SRC_ALPHA:
				components = "a";
				break;
		}
		sb.append(components);
		if (oneMinus) {
			sb.append(")");
		}
		return sb.toString();
	}

	private static String getColorArg(TexEnvConfig.PICATextureCombinerSource src, TexEnvConfig.PICATextureCombinerColorOp op, int stageIndex, Material mat) {
		StringBuilder sb = new StringBuilder();

		boolean oneMinus = false;
		switch (op) {
			case ONE_MINUS_BLUE:
			case ONE_MINUS_GREEN:
			case ONE_MINUS_RED:
			case ONE_MINUS_SRC_ALPHA:
			case ONE_MINUS_SRC_COLOR:
				oneMinus = true;
				break;
		}
		if (oneMinus) {
			sb.append("(vec3(1) - ");
		}

		sb.append(getSource(src, stageIndex, mat));
		sb.append(".");

		String components = null;
		switch (op) {
			case BLUE:
			case ONE_MINUS_BLUE:
				components = "bbb";
				break;
			case GREEN:
			case ONE_MINUS_GREEN:
				components = "ggg";
				break;
			case RED:
			case ONE_MINUS_RED:
				components = "rrr";
				break;
			case SRC_ALPHA:
			case ONE_MINUS_SRC_ALPHA:
				components = "aaa";
				break;
			case SRC_COLOR:
			case ONE_MINUS_SRC_COLOR:
				components = "rgb";
				break;
		}
		sb.append(components);
		if (oneMinus) {
			sb.append(")");
		}
		return sb.toString();
	}

	//private static final String TEXIN_SPHERE_PRECALC = "sphereCoord";

	private static String getSource(TexEnvConfig.PICATextureCombinerSource src, int stageIndex, Material mat) {
		switch (src) {
			case CONSTANT:
				return "constantColor[" + stageIndex + "]";
			case FRAG_PRIMARY_COLOR:
				return "fragmentLightingPrimaryColor";
			case FRAG_SECONDARY_COLOR:
				return "fragmentLightingSecondaryColor";
			case PREVIOUS_BUFFER:
				return "tevBuffer";
			case PREVIOUS_STAGE:
				return "tevColor";
			case PRIMARY_COLOR:
				return "color";
			case TEX0:
				return getTextureFunc(0, mat);
			case TEX1:
				return getTextureFunc(1, mat);
			case TEX2:
				return getTextureFunc(2, mat);
			case TEX3:
				return getTextureFunc(3, mat);
		}
		throw new IllegalArgumentException("Invalid combiner source.");
	}

	private static String getTextureFunc(int samplerIndex, Material mat) {
		StringBuilder sb = new StringBuilder();
		sb.append("texture2D(");
		sb.append("textures[");
		sb.append(samplerIndex);
		sb.append("], ");
		/*switch (mat.getMapMode(samplerIndex)) {
			case UV_MAP:
			case CUBE_MAP:
			case PROJECTION_MAP:
				sb.append("uv");
				sb.append(samplerIndex);
				break;
			case SPHERE_MAP:
				sb.append(TEXIN_SPHERE_PRECALC);
				break;
		}*/
		sb.append("uv");
		sb.append(samplerIndex);
		sb.append(")");
		return sb.toString();
	}
}
