//#version 110

#define TEXTURE_MAX 4

#define RAIL_SAMPLER_LUT_Y 0
#define RAIL_SAMPLER_LUT_X1 1
#define RAIL_SAMPLER_LUT_X2 2

#define POINT_VTX_TANGENT_C_L 0
#define POINT_VTX_TANGENT_O_L 1
#define POINT_VTX_TANGENT_C_R 2
#define POINT_VTX_TANGENT_O_R 3
#define POINT_VTX_OFFSET_L 4
#define POINT_VTX_OFFSET_R 5

//HOUSTON attributes
#include VshBase.vsh_chunk

uniform sampler2D textures[TEXTURE_MAX];
uniform sampler2D LUT[7];
//End HOUSTON attributes

//RailRender vertex shader system
uniform bool railHermite_Enable;

uniform bool railSampler_CenterX;
uniform bool railSampler_MirrorX;

uniform bool railIsTrack;

uniform vec3 point1;
uniform vec3 point2;
//Normals are only taken into consideration on lerp tracks
uniform vec3 normal1;
uniform vec3 normal2;
uniform vec3 tangent1;
uniform vec3 tangent2;

uniform vec3 point1Vertices[6];
uniform vec3 point2Vertices[6];

uniform float railWidthMaxAbs;
uniform float railHeightMaxAbs;
//End RailRender

float hermite(float p0, float m0, float p1, float m1, float t) {
	float t3 = t * t * t;
	float t2 = t * t;
	return 
		(2.0 * t3 - 3.0 * t2 + 1.0) * p0 + 
		(t3 - 2.0 * t2 + t) * m0 + 
		(-2.0 * t3 + 3.0 * t2) * p1 + 
		(t3 - t2) * m1;
}

vec3 vectorHermite(vec3 p0, vec3 m0, vec3 p1, vec3 m1, float t) {
	vec3 dest;
	dest.x = hermite(p0.x, m0.x, p1.x, m1.x, t);
	dest.y = hermite(p0.y, m0.y, p1.y, m1.y, t);
	dest.z = hermite(p0.z, m0.z, p1.z, m1.z, t);
	return dest;
}

void main(void){
	float yWeight = a_position.y / railHeightMaxAbs;
	float xWeight = a_position.x / railWidthMaxAbs; //range -0.5 to +0.5

	if (railIsTrack) {
		yWeight = 1.0 - yWeight;
	}

	bool isRight = xWeight >= 0.0;

	if (railHermite_Enable){
		vec2 yLookup  = vec2(yWeight,  0.0);
		yWeight  = texture2D(LUT[RAIL_SAMPLER_LUT_Y ], yLookup ).r;

		vec2 xLookup = vec2(xWeight + 0.5, 0.0); //lookup table starts at 0.0, shift input accordingly

		xWeight = mix(texture2D(LUT[RAIL_SAMPLER_LUT_X1], xLookup).r, texture2D(LUT[RAIL_SAMPLER_LUT_X2], xLookup).r, yWeight);
		if (!isRight) {
			xWeight = -xWeight;
		}
		isRight = xWeight >= 0.0;
	}
	else {
		xWeight *= 2.0; //expand weight to -1 to +1
	}

	vec3 curve;
	if (railHermite_Enable) {
	 	curve = vectorHermite(point1, tangent1, point2, tangent2, yWeight);
	}
	else {
		curve = mix(point1, point2, yWeight);
	}

	vec3 hOffsetTop;
	vec3 hOffsetBottom;

	vec3 hCenterTangentTop;
	vec3 hCenterTangentBottom;

	vec3 hOffsetTangentTop;
	vec3 hOffsetTangentBottom;

	if (isRight){
		hOffsetTop = point1Vertices[POINT_VTX_OFFSET_R];
		hOffsetBottom = point2Vertices[POINT_VTX_OFFSET_R];
		hCenterTangentTop = point1Vertices[POINT_VTX_TANGENT_C_R];
		hCenterTangentBottom = point2Vertices[POINT_VTX_TANGENT_C_R];
		hOffsetTangentTop = point1Vertices[POINT_VTX_TANGENT_O_R];
		hOffsetTangentBottom = point2Vertices[POINT_VTX_TANGENT_O_R];
	}
	else {
		hOffsetTop = point1Vertices[POINT_VTX_OFFSET_L];
		hOffsetBottom = point2Vertices[POINT_VTX_OFFSET_L];
		hCenterTangentTop = point1Vertices[POINT_VTX_TANGENT_C_L];
		hCenterTangentBottom = point2Vertices[POINT_VTX_TANGENT_C_L];
		hOffsetTangentTop = point1Vertices[POINT_VTX_TANGENT_O_L];
		hOffsetTangentBottom = point2Vertices[POINT_VTX_TANGENT_O_L];
	}

	vec3 result;

	vec3 hOffset = mix(hOffsetTop, hOffsetBottom, yWeight);

	if (railHermite_Enable) {
		vec3 hCenterTangent = 
		mix(		
			hCenterTangentTop,
			hCenterTangentBottom,
			yWeight
		);
		vec3 hOffsetTangent = 
		mix(		
			hOffsetTangentTop,
			hOffsetTangentBottom,
			yWeight
		);

		result = vectorHermite(curve, hCenterTangent, curve + hOffset, hOffsetTangent, abs(xWeight));
	}
	else {
		float hOffMul = length(hOffset) * xWeight;

		vec3 normal = mix(normal1, normal2, yWeight);
		vec3 posDiff = point2 - point1;

		result = curve + normalize(cross(normalize(posDiff), normal)) * hOffMul;
	}

	uv0 = a_texcoord;

	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(result, 1.0);
}