//#version 110

//EXTENSION-vert-define

#include VshBase.vsh_chunk

//EXTENSION-vert-init

uniform bool needsTangent;

uniform int uvAssignment[TEXTURE_MAX];

//SRT Material Animation support
uniform mat4 mta_transform[TEXTURE_MAX];

//Skeletal animation support
//TODO
uniform bool isAnimated;
uniform int bonesCount;
uniform mat4 boneTransforms[192];

vec2 getTexCoordForUv(int uvId){
    int asgn = uvAssignment[uvId];
    if (asgn == 1){
        return a_texcoord1;
    }
    else if (asgn == 2){
        return a_texcoord2;
    }
    return a_texcoord;
}

vec2 rotate(vec2 v, float anglerad){
    float txy = -0.5;

    float x = v.x + txy;
    float y = v.y + txy;

    float s = sin(anglerad);
    float c = cos(anglerad);

    return vec2(x * c - y * s - txy, x * s + y * c - txy);
}

vec2 transformUV(int transformIndex){
    return vec2(mta_transform[transformIndex] * vec4(getTexCoordForUv(transformIndex), 0.0, 1.0));
}

vec3 calcTangent(vec3 normal){
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

void main(void)
{
    vec4 pos = vec4(a_position, 1);
    vec4 normal = vec4(a_normal, 0);
    vec4 tangent = vec4(a_tangent, 0);
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

    outPosition = projectionMatrix * outPosition;

    //EXTENSION-vert-post target=outPosition

    f_normal = normalMatrix * vec3(n);

    if (!hasTangent){
        f_tangent = calcTangent(f_normal);
    }
    else {
        f_tangent = a_tangent;
    }

    gl_Position = outPosition;
    
    if (hasColor){
        color = a_color;
    }
    else {
        color = vec4(1);
    }
    uv0 = transformUV(0);
    uv1 = transformUV(1);
    uv2 = transformUV(2);
}