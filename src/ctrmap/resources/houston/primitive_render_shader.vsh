//#version 110

//EXTENSION-vert-define

#include VshBase.vsh_chunk

//EXTENSION-vert-init

uniform bool needsTangent;

uniform int uvAssignment[TEXTURE_MAX];
uniform int textureMapModes[TEXTURE_MAX];

const int TEXTURE_MAP_MODE_UV = 0;
const int TEXTURE_MAP_MODE_CUBE = 1;
const int TEXTURE_MAP_MODE_SPHERE = 2;
const int TEXTURE_MAP_MODE_PROJECTION = 3;

//SRT Material Animation support
uniform mat4 mta_transform[TEXTURE_MAX];

//Skeletal animation support
//TODO
uniform bool isAnimated;
uniform int bonesCount;
uniform mat4 boneTransforms[192];

vec2 getTexCoordForUv(int uvId){
    int asgn = uvAssignment[uvId];
    if (asgn == 0) {
        return a_texcoord;
    }
    else if (asgn == 1){
        return a_texcoord1;
    }
    else if (asgn == 2){
        return a_texcoord2;
    }
    return vec2(0.0);
}

vec2 rotate(vec2 v, float anglerad){
    float txy = -0.5;

    float x = v.x + txy;
    float y = v.y + txy;

    float s = sin(anglerad);
    float c = cos(anglerad);

    return vec2(x * c - y * s - txy, x * s + y * c - txy);
}

vec3 uv_normal;
vec3 uv_view;

vec2 transformTexGen(int transformIndex, vec3 texGenSource) {
    if (uvAssignment[transformIndex] == -1) {
        return (mta_transform[transformIndex] * vec4(texGenSource * 0.5, 1.0) + 0.5).xy;
    }
    else {
        return (mta_transform[transformIndex] * vec4(texGenSource, 1.0)).xy + getTexCoordForUv(transformIndex);
    }
}

vec2 transformUV(int transformIndex) {
    int mapMode = textureMapModes[transformIndex];
    if (mapMode == TEXTURE_MAP_MODE_UV) {
        return (mta_transform[transformIndex] * vec4(getTexCoordForUv(transformIndex), 0.0, 1.0)).xy;
    }
    else if (mapMode == TEXTURE_MAP_MODE_SPHERE) {
        return transformTexGen(transformIndex, uv_normal);
    }
    else if (mapMode == TEXTURE_MAP_MODE_PROJECTION) {
        return transformTexGen(transformIndex, uv_view);
    }
    else {
        return vec2(0.0);
    }
}

vec3 calcTangent(vec3 normal) {
    vec3 tangent;

    vec3 c1 = cross(normal, vec3(0.0, 0.0, 1.0));
    vec3 c2 = cross(normal, vec3(0.0, 1.0, 0.0));

    if (length(c1) > length(c2))
    {
        tangent = c1;
    }
    else
    {
        tangent = c2;
    }
    return tangent;
}

vec3 getMorphPosition() {
    if (morphWeight == 0.0) {
        return a_position;
    }
    else {
        return mix(a_position, a_positionB, 1.0 - morphWeight);
    }
}

vec3 getMorphNormal() {
    if (morphWeight == 0.0) {
        return a_normal;
    }
    else {
        return mix(a_normal, a_normalB, 1.0 - morphWeight);
    }
}

vec3 getMorphTangent() {
    if (morphWeight == 0.0) {
        return a_tangent;
    }
    else {
        return mix(a_tangent, a_tangentB, 1.0 - morphWeight);
    }
}

void main(void)
{
    vec4 pos = vec4(getMorphPosition(), 1);
    vec4 normal = vec4(getMorphNormal(), 0);
    vec4 tangent = vec4(getMorphTangent(), 0);
    vec4 p = vec4(0, 0, 0, 1);
    vec4 n = vec4(0);
    vec4 t = vec4(0);

    //EXTENSION-vert-main

    if (isAnimated && hasBonesData){
        if (hasNormal && hasTangent) {
            //better to have this semi-duped code than more branching
            float weightSum = 0.0;
            for (int i = 0; i < 4; i++) {
                int boneIndex = int(a_boneIndices[i]);
                /*if (boneIndex >= bonesCount){
                    break;
                }*/
                //Whatever undefined value is here will get mul'd by 0. I think it's better than branching.
                float weight = a_boneWeights[i];
                weightSum += weight;
                p += boneTransforms[boneIndex] * pos * weight;
                n += boneTransforms[boneIndex] * normal * weight;
                t += boneTransforms[boneIndex] * tangent * weight;
            }
            /*if (weightSum < 1.0) {
                p += pos * (1.0 - weightSum);
                n += normal * (1.0 - weightSum);
            }*/
        }
        else if (hasNormal) {
            float weightSum = 0.0;
            for (int i = 0; i < 4; i++) {
                int boneIndex = int(a_boneIndices[i]);
                float weight = a_boneWeights[i];
                weightSum += weight;
                p += boneTransforms[boneIndex] * pos * weight;
                n += boneTransforms[boneIndex] * normal * weight;
            }
        } else {
            float weightSum = 0.0;
            for (int i = 0; i < 4; i++) {
                int boneIndex = int(a_boneIndices[i]);
                /*if (boneIndex >= bonesCount){
                    break;
                }*/
                float weight = a_boneWeights[i];
                weightSum += weight;
                p += boneTransforms[boneIndex] * pos * weight;
            }
            /*if (weightSum < 1.0) {
                p += pos * (1.0 - weightSum);
            }*/
        }

        //EXTENSION-vert-skinning-post

        p.w = float(1);
        n.w = float(0);
        t.w = 0.0;
    }
    else {
        n = normal;
        p = pos;
        t = tangent;
    }

    vec4 outPosition = viewMatrix * modelMatrix * p;

    f_view = -vec3(outPosition);
    uv_view = outPosition.xyz;

    outPosition = projectionMatrix * outPosition;

    //EXTENSION-vert-post target=outPosition

    uv_normal = normalMatrix * vec3(n);
    f_normal = uv_normal;
    f_relNormal = normal.xyz;

    if (!hasTangent){
        f_tangent = calcTangent(uv_normal);
    }
    else {
        f_tangent = a_tangent;
    }

    gl_Position = outPosition;
    
    if (hasColor){
        color = a_color;
    }
    else {
        color = vec4(1.0);
    }
    uv0 = transformUV(0);
    uv1 = transformUV(1);
    uv2 = transformUV(2);
}