//#version 110

//EXTENSION-frag-define

#include FshBase.fsh_chunk

uniform sampler2D textures[TEXTURE_MAX];

uniform sampler2D LUT[LUT_MAX];
uniform int LUTInputs[LUT_MAX];
uniform bool LUTEnabled[LUT_MAX];

//EXTENSION-frag-init

//Deoxys TexEnv emulator v1.3

const int LUT_REFLEC_R = 0;
const int LUT_REFLEC_G = 1;
const int LUT_REFLEC_B = 2;
const int LUT_DIST_0 = 3;
const int LUT_DIST_1 = 4;
const int LUT_FRESNEL_PRI = 5;
const int LUT_FRESNEL_SEC = 6;

const int LUT_INPUT_NORMAL_HALF = 0;
const int LUT_INPUT_VIEW_HALF = 1;
const int LUT_INPUT_NORMAL_VIEW = 2;
const int LUT_INPUT_LIGHT_NORMAL = 3;
const int LUT_INPUT_LIGHT_SPOT = 4;
const int LUT_INPUT_PHI = 5;

const int CMB_REPLACE = 0;
const int CMB_MODULATE = 1;
const int CMB_ADD = 2;
const int CMB_ADD_SIGNED = 3;
const int CMB_INTERPOLATE = 4;
const int CMB_SUBTRACT = 5;
const int CMB_DOT3_RGB = 6;
const int CMB_DOT3_RGBA = 7;
const int CMB_MULT_ADD = 8;
const int CMB_ADD_MULT = 9;

const int CMB_SRC_PRIMARY_COLOR = 0;
const int CMB_SRC_FRAG_PRIMARY_COLOR = 1;
const int CMB_SRC_FRAG_SECONDARY_COLOR = 2;
const int CMB_SRC_TEX0 = 3;
const int CMB_SRC_TEX1 = 4;
const int CMB_SRC_TEX2 = 5;
const int CMB_SRC_TEX3 = 6; //pretty sure H3D does not support those
const int CMB_SRC_PREV_BUFFER = 13;
const int CMB_SRC_CONSTANT_COLOR = 14;
const int CMB_SRC_PREV_STAGE = 15;

const int CMB_OP_A_ALPHA = 0;
const int CMB_OP_A_ONE_MINUS_ALPHA = 1;
const int CMB_OP_A_RED = 2;
const int CMB_OP_A_ONE_MINUS_RED = 3;
const int CMB_OP_A_GREEN = 4;
const int CMB_OP_A_ONE_MINUS_GREEN = 5;
const int CMB_OP_A_BLUE = 6;
const int CMB_OP_A_ONE_MINUS_BLUE = 7;

const int CMB_OP_RGB_COLOR = 0;
const int CMB_OP_RGB_ONE_MINUS_COLOR = 1;
const int CMB_OP_RGB_ALPHA = 2;
const int CMB_OP_RGB_ONE_MINUS_ALPHA = 3;
const int CMB_OP_RGB_RED = 4;
const int CMB_OP_RGB_ONE_MINUS_RED = 5;
const int CMB_OP_RGB_GREEN = 8;
const int CMB_OP_RGB_ONE_MINUS_GREEN = 9;
const int CMB_OP_RGB_BLUE = 12;
const int CMB_OP_RGB_ONE_MINUS_BLUE = 13;

//TEV params
uniform int activeStages;

uniform vec4 inputBufferColor;

uniform float colorScale[SHADING_STAGE_MAX];
uniform float alphaScale[SHADING_STAGE_MAX];

uniform int colorCombMode[SHADING_STAGE_MAX];
uniform int alphaCombMode[SHADING_STAGE_MAX];

uniform int colorCombSource0[SHADING_STAGE_MAX];
uniform int alphaCombSource0[SHADING_STAGE_MAX];
uniform int colorCombSource1[SHADING_STAGE_MAX];
uniform int alphaCombSource1[SHADING_STAGE_MAX];
uniform int colorCombSource2[SHADING_STAGE_MAX];
uniform int alphaCombSource2[SHADING_STAGE_MAX];

uniform int colorCombOperand0[SHADING_STAGE_MAX];
uniform int alphaCombOperand0[SHADING_STAGE_MAX];
uniform int colorCombOperand1[SHADING_STAGE_MAX];
uniform int alphaCombOperand1[SHADING_STAGE_MAX];
uniform int colorCombOperand2[SHADING_STAGE_MAX];
uniform int alphaCombOperand2[SHADING_STAGE_MAX];

uniform bool writeColorBuf[SHADING_STAGE_MAX];
uniform bool writeAlphaBuf[SHADING_STAGE_MAX];

uniform vec4 constantColor[SHADING_STAGE_MAX];

vec4 tevBuffer = inputBufferColor;

vec4 fragmentLightingPrimaryColor = vec4(0, 0, 0, 1);
vec4 fragmentLightingSecondaryColor = vec4(0, 0, 0, 1);

vec4 getTexColor(sampler2D tex, vec2 uv, int unitIndex){
    return texture2D(tex, uv);
}

vec4 getSource(int stage, int sourceNum, bool sourceIsAlpha, vec4 previous){
    int source;
    if (sourceIsAlpha){
        switch (sourceNum){
            case 0:
                source = alphaCombSource0[stage];
                break;
            case 1:
                source = alphaCombSource1[stage];
                break;
            case 2:
                source = alphaCombSource2[stage];
                break;
        }
    }
    else {
        switch (sourceNum){
            case 0:
                source = colorCombSource0[stage];
                break;
            case 1:
                source = colorCombSource1[stage];
                break;
            case 2:
                source = colorCombSource2[stage];
                break;
        }
    }
    switch (source){
        case CMB_SRC_PRIMARY_COLOR:
            return color;   //vertex color
        case CMB_SRC_FRAG_PRIMARY_COLOR:
            return fragmentLightingPrimaryColor;
        case CMB_SRC_FRAG_SECONDARY_COLOR:
            return fragmentLightingSecondaryColor;
        case CMB_SRC_TEX0:
            return getTexColor(textures[0], uv0, 0);    //the vertex shader has already indexed the UVs correctly
        case CMB_SRC_TEX1:
            return getTexColor(textures[1], uv1, 0);
        case CMB_SRC_TEX2:
            return getTexColor(textures[2], uv2, 0);
        case CMB_SRC_TEX3:
            return getTexColor(textures[3], uv2, 0);
        case CMB_SRC_PREV_BUFFER:
            return tevBuffer;
        case CMB_SRC_CONSTANT_COLOR:
            return constantColor[stage];
        case CMB_SRC_PREV_STAGE:
            return previous;
    }

    return vec4(1);
}

int getOpArgCount(int op){
    switch (op){
        case CMB_REPLACE:
            return 1;
        case CMB_MODULATE:
        case CMB_ADD:
        case CMB_ADD_SIGNED:
        case CMB_SUBTRACT:
        case CMB_DOT3_RGB:
        case CMB_DOT3_RGBA:
            return 2;
        case CMB_ADD_MULT:
        case CMB_MULT_ADD:
        case CMB_INTERPOLATE:
            return 3;
    }
}

float getAlphaArg(int sourceNum, int stage, vec4 previous){
    int operand;
    switch (sourceNum){
        case 0:
            operand = alphaCombOperand0[stage];
            break;
        case 1:
            operand = alphaCombOperand1[stage];
            break;
        case 2:
            operand = alphaCombOperand2[stage];
            break;
    }
    vec4 source = getSource(stage, sourceNum, true, previous);
    switch (operand){
        case CMB_OP_A_ALPHA:
            return source.a;
        case CMB_OP_A_ONE_MINUS_ALPHA:
            return 1.0 - source.a;
        case CMB_OP_A_RED:
            return source.r;
        case CMB_OP_A_ONE_MINUS_RED:
            return 1.0 - source.r;
        case CMB_OP_A_GREEN:
            return source.g;
        case CMB_OP_A_ONE_MINUS_GREEN:
            return 1.0 - source.g;
        case CMB_OP_A_BLUE:
            return source.b;
        case CMB_OP_A_ONE_MINUS_BLUE:
            return 1.0 - source.b;
    }
}

vec4 getColorArg(int sourceNum, int stage, vec4 previous){
    int operand;
    switch (sourceNum){
        case 0:
            operand = colorCombOperand0[stage];
            break;
        case 1:
            operand = colorCombOperand1[stage];
            break;
        case 2:
            operand = colorCombOperand2[stage];
            break;
    }
    vec4 source = getSource(stage, sourceNum, false, previous);
    switch (operand){
        case CMB_OP_RGB_COLOR:
            return source;
        case CMB_OP_RGB_ONE_MINUS_COLOR:
            return vec4(1) - source;
        case CMB_OP_RGB_ALPHA:
            return vec4(source.a);
        case CMB_OP_RGB_ONE_MINUS_ALPHA:
            return vec4(1.0 - source.a);
        case CMB_OP_RGB_RED:
            return vec4(source.r);
        case CMB_OP_RGB_ONE_MINUS_RED:
            return vec4(1.0 - source.r);
        case CMB_OP_RGB_GREEN:
            return vec4(source.g);
        case CMB_OP_RGB_ONE_MINUS_GREEN:
            return vec4(1.0 - source.g);
        case CMB_OP_RGB_BLUE:
            return vec4(source.b);
        case CMB_OP_RGB_ONE_MINUS_BLUE:
            return vec4(1.0 - source.b);
    }
}

float doAlphaCombine(int stage, float args[3]){
    switch (alphaCombMode[stage]){
        case CMB_REPLACE:
            return args[0];
        case CMB_MODULATE:
            return args[0] * args[1];
        case CMB_ADD:
            return min(args[0] + args[1], 1.0); //clamping is redundant as this can't be <0
        case CMB_ADD_SIGNED:
            return clamp((args[0] + args[1]) - 0.5, 0.0, 1.0);  //for some reason, 0,5 is used as the centre of the numeric axis for signed ops
        case CMB_INTERPOLATE:
            return mix(args[1], args[0], args[2]);
        case CMB_SUBTRACT:
            return max(args[0] - args[1], 0.0); //same as with add, in reverse
        case CMB_DOT3_RGB:
            return min(dot(vec3(args[0]), vec3(args[1])), 1.0);
        case CMB_DOT3_RGBA:
            return min(dot(vec4(args[0]), vec4(args[1])), 1.0);
        case CMB_MULT_ADD:
            return min((args[0] * args[1]) + args[2], 1.0);
        case CMB_ADD_MULT:
            return min(args[0] + args[1], 1.0) * args[2];
    }
}

vec4 doColorCombine(int stage, vec4 args[3]){
    switch (colorCombMode[stage]){
        case CMB_REPLACE:
            return args[0];
        case CMB_MODULATE:
            return args[0] * args[1];
        case CMB_ADD:
            return min(args[0] + args[1], 1.0);
        case CMB_ADD_SIGNED:
            return clamp((args[0] + args[1]) - 0.5, 0.0, 1.0);
        case CMB_INTERPOLATE:
            return mix(args[1], args[0], args[2]);
        case CMB_SUBTRACT:
            return max(args[0] - args[1], 0.0);
        case CMB_DOT3_RGB:
            return vec4(min(dot(vec3(args[0]), vec3(args[1])), 1.0));
        case CMB_DOT3_RGBA:
            return vec4(min(dot(args[0], args[1]), 1.0));
        case CMB_MULT_ADD:
            return min((args[0] * args[1]) + args[2], 1.0);
        case CMB_ADD_MULT:
            return min(args[0] + args[1], 1.0) * args[2];
    }
}

vec4 processStage(int stage, vec4 previous){    
    if (writeColorBuf[stage]){
        tevBuffer.rgb = previous.rgb;
    }
    if (writeAlphaBuf[stage]){
        tevBuffer.a = previous.a;
    }
    
    float alphaSource[3];
    vec4 colorSource[3];
    
    //don't waste resources by caching all arguments, we're on a programmable GPU, not PICA
    int alphaArgCount = getOpArgCount(alphaCombMode[stage]);
    int colorArgCount = getOpArgCount(colorCombMode[stage]);
    
    for (int i = 0; i < alphaArgCount; i++){
        alphaSource[i] = getAlphaArg(i, stage, previous);
    }
    for (int i = 0; i < colorArgCount; i++){
        colorSource[i] = getColorArg(i, stage, previous);
    }
    
    vec4 r = vec4(doAlphaCombine(stage, alphaSource)) * alphaScale[stage];
    r.rgb = doColorCombine(stage, colorSource).rgb * colorScale[stage];
    
    return r;
}

vec3 v = normalize(f_view);

float getLUTValue(int LUTIndex, int channel, vec3 v, vec3 n, vec3 h, vec3 l, Light light, float defaultValue){
    if (LUTEnabled[LUTIndex]){
        float val = 0.0;

        switch (LUTInputs[LUTIndex]){
            case LUT_INPUT_NORMAL_HALF:
                val = dot(n, h);
                break;
            case LUT_INPUT_VIEW_HALF:
                val = dot(v, h);
                break;
            case LUT_INPUT_NORMAL_VIEW:
                val = dot(n, v);
                break;
            case LUT_INPUT_LIGHT_NORMAL:
                val = dot(l, n);
                break;
            case LUT_INPUT_LIGHT_SPOT:
                val = dot(l, light.direction);
                break;
            case LUT_INPUT_PHI:
                val = dot(h - n / dot(n, n) * dot(n, h), f_tangent);
                break;
        }

        val = texture2D(LUT[LUTIndex], vec2(val * 0.5, 0.0)).r;
        return val;
    }
    else {
        return defaultValue;
    }
}

void calcLightingColors(){
    vec3 v = normalize(f_view);

    fragmentLightingPrimaryColor = emissionColor;

    for (int i = 0; i < lightCount; i++){
        Light light = lights[i];

        vec3 lightPos = light.directional ? -light.direction : (light.position + v);
        vec3 l = normalize(lightPos);
        fragmentLightingPrimaryColor += max(0.0, dot(normal, l)) * light.colors[LIGHT_CMN_COLOR_DIF_IDX] * diffuseColor + light.colors[LIGHT_CMN_COLOR_AMB_IDX] * ambientColor;

        vec4 specular1 = specular1Color;
        vec3 h = normalize(v + l);
        vec3 n = normal;

        specular1.r = getLUTValue(LUT_REFLEC_R, 0, v, n, h, l, light, 1.0);
        specular1.g = getLUTValue(LUT_REFLEC_G, 1, v, n, h, l, light, specular1.r);
        specular1.b = getLUTValue(LUT_REFLEC_B, 2, v, n, h, l, light, specular1.g);

        fragmentLightingSecondaryColor += specular1 * light.colors[LIGHT_CMN_COLOR_SPC0_IDX] + specular0Color * light.colors[LIGHT_CMN_COLOR_SPC0_IDX];

        if (i == lightCount - 1){
            fragmentLightingPrimaryColor.a = getLUTValue(LUT_FRESNEL_PRI, 2, v, n, h, l, light, 1.0);
            fragmentLightingSecondaryColor.a = getLUTValue(LUT_FRESNEL_SEC, 2, v, n, h, l, light, 1.0);
        }
    }

    if (lightCount == 0) {
        fragmentLightingPrimaryColor = vec4(1);
        fragmentLightingSecondaryColor = vec4(0, 0, 0, 1);
    }
    else {
        fragmentLightingPrimaryColor = min(vec4(1), fragmentLightingPrimaryColor);
        fragmentLightingSecondaryColor = min(vec4(1), fragmentLightingSecondaryColor);
    }
}

void main(void)
{
    //EXTENSION-frag-main
    
    calcLightingColors();

    vec4 tevColor = inputBufferColor; //default texenv color
    for (int stage = 0; stage < activeStages; stage++){
        tevColor = processStage(stage, tevColor);
    }

    vec4 outColor = color;

    if (activeStages != 0){
        outColor = tevColor;
    }

    //EXTENSION-frag-post target=outColor

    #ifdef GL4
    processAlphaTest(outColor.a);
    #endif

    OUTPUT(RT_SURFACE_MAIN) = outColor;
}
