package ctrmap.renderer.backends.houston.common;

public class HoustonUniforms {	
	public static final String TEV_ACTIVE_STAGE_COUNT_OVERRIDE = "activeStages";
	
	public static final String TEV_FRAG_COLOR_PRI = "fragmentLightingPrimaryColor";
	public static final String TEV_FRAG_COLOR_SEC = "fragmentLightingSecondaryColor";
	
	public static final String TEV_CONST_COLOR = "constantColor";
	
	public static final String MTX_MODEL = "modelMatrix";
	public static final String MTX_VIEW = "viewMatrix";
	public static final String MTX_NORMAL = "normalMatrix";
	public static final String MTX_PROJECTION = "projectionMatrix";
	
	public static final String MESH_BOOLUNIFORMS = "meshBoolUniforms";
	public static final int MESH_BOOLUNIFORMS_COLOR_IDX = 0;
	public static final int MESH_BOOLUNIFORMS_NORMAL_IDX = 1;
	public static final int MESH_BOOLUNIFORMS_TANGENT_IDX = 2;
	public static final int MESH_BOOLUNIFORMS_SKINNING_IDX = 3;
	public static final int MESH_BOOLUNIFORMS_COUNT = 4;
	public static final String MESH_UV_ASSIGNMENT = "uvAssignment";
	
	public static final String SHA_NEEDS_TANGENT = "needsTangent";
	
	public static final String TEX_SAMPLERS = "textures";
	
	public static final String TEX_SAMPLER_LUT = "LUT";
	
	public static final String SKA_TRANSFORM_ENABLE = "isAnimated";
	public static final String SKA_TRANSFORM_COUNT = "bonesCount";
	public static final String SKA_TRANSFORMS = "boneTransforms";
	
	public static final String MTA_TRANSFORM = "mta_transform";
	
	public static final int LIGHTING_COLORS_COUNT = 5;
	public static final String LIGHTING_COLORS = "lightingColors";
	public static final String LIGHTING_COL_AMB = "ambientColor";
	public static final String LIGHTING_COL_DIF = "diffuseColor";
	public static final String LIGHTING_COL_EMI = "emissionColor";
	public static final String LIGHTING_COL_SPC0 = "specular0Color";
	public static final String LIGHTING_COL_SPC1 = "specular1Color";
	
	public static final String LIGHT_COUNT = "lightCount";
	public static final String LIGHT_DIRECTIONAL = "directional";
	public static final String LIGHT_POSITION = "position";
	public static final String LIGHT_DIRECTION = "direction";
	public static final String LIGHT_COLORS = "colors";
	public static final int LIGHT_COLORS_COUNT = 4;
	
	public static String getLightAttribName(int lightIndex, String attrib) {
		return "lights[" + lightIndex + "]." + attrib;
	}
	
	public static final String ASH_TIME = "time";
	public static final String ASH_SCREEN_DIM = "screenDim";
}
