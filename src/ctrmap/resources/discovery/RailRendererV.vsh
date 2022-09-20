//#version 110

#define TEXTURE_MAX 4

#define RR_CURVE_LERP 0
#define RR_CURVE_SLERP_XZ 1
#define RR_CURVE_SLERP_XYZ 2

#define WB_TILE_SIZE 16.0

#include VshBase.vsh_chunk

//EXTENSION-vert-init

uniform sampler2D textures[TEXTURE_MAX];
//End HOUSTON attributes

//RailRenderV vertex shader system
uniform int railID;

uniform vec3 point1;
uniform vec3 point2;
uniform vec3 centre;

uniform float lineWidthP1;
uniform float lineWidthP2;

uniform float railLength;
uniform float railWidth;
uniform float tileWidth;

uniform int curveType;

uniform vec3 inputRailPosOffset;
//End RailRender

vec2 inPosition = a_position.xy + inputRailPosOffset.xy;

float lerpLineWidth(float yWeight){
	return mix(lineWidthP1, lineWidthP2, yWeight) + 8.0;
}

float makeYWeight(){
	return inPosition.y / railLength;
}

float makeXWeight(float yWeight){
	return (inPosition.x / (railWidth * 0.5)) * tileWidth * lerpLineWidth(yWeight);
}

vec3 makeCurveLerp(){
	float yWeight = makeYWeight();
	float xAdd = makeXWeight(yWeight);

	vec3 diff = point2 - point1;

	vec3 result = mix(point1, point2, yWeight);

	vec3 perp = normalize(cross(diff, vec3(0.0, 1.0, 0.0)));

	result = perp * xAdd + result;	

	return result;
}

vec3 makeSlerp(vec3 dp1, vec3 dp2, float yWeight){
	float len1 = length(dp1);
	float len2 = length(dp2);

	dp1 = normalize(dp1);
	dp2 = normalize(dp2);

	float angle = acos(dot(dp1, dp2));

	float sinAngle = sin(angle);

	vec3 slerp;

	if (sinAngle == 0.0){
		slerp = dp2;
	}
	else {
		float sinAngleWeightFront = sin(angle * (1.0 - yWeight));
		float sinAngleWeightBack = sin(angle * yWeight);

		slerp = normalize((dp1 * sinAngleWeightFront + dp2 * sinAngleWeightBack) / sinAngle);
	}

	return slerp * mix(len1, len2, yWeight);
}

vec3 makeCurveSlerpXZ(){
	float yWeight = makeYWeight();
	float xAdd = makeXWeight(yWeight);

	vec3 c = centre;
	c.y = mix(point1.y, point2.y, yWeight);

	vec3 dp1 = point1 - c;
	vec3 dp2 = point2 - c;

	dp1.y = 0.0;
	dp2.y = 0.0;

	vec3 slerp = makeSlerp(dp1, dp2, yWeight);

	vec3 result = slerp + c;

	slerp = normalize(slerp);

	vec3 crossProduct = cross(slerp, normalize(dp1 - dp2));

	if (crossProduct.y >= 0.0){
		result = slerp * -xAdd + result;
	}
	else {
		result = slerp * xAdd + result;	
	}

	return result;
}

vec3 makeCurveSlerpXYZ(){
	float yWeight = makeYWeight();
	float xAdd = makeXWeight(yWeight);

	vec3 c = centre;

	vec3 dp1 = point1 - c;
	vec3 dp2 = point2 - c;

	vec3 slerp = makeSlerp(dp1, dp2, yWeight);

	vec3 result = slerp + c;

	if (slerp.x == 0.0 || slerp.z == 0.0){
		//Slerp result is one of the points
		//Sets the slerp to the vector perpendicular to the rail line for posH calculation
		slerp = cross(point2 - point1, vec3(0.0, 1.0, 0.0));
	}
	else {
		slerp.y = 0.0;
	}

	slerp = normalize(slerp);

	vec3 crossProduct = cross(slerp, normalize(dp1 - dp2));

	if (crossProduct.y >= 0.0){
		result = slerp * -xAdd + result;
	}
	else {
		result = slerp * xAdd + result;	
	}

	return result;
}

void main(void){
	//EXTENSION-vert-main
	vec3 result = vec3(0.0);
	switch (curveType){
		case RR_CURVE_LERP:
			result = makeCurveLerp();
			break;
		case RR_CURVE_SLERP_XZ:
			result = makeCurveSlerpXZ();
			break;
		case RR_CURVE_SLERP_XYZ:
			result = makeCurveSlerpXYZ();
			break;
	}

	uv0 = a_texcoord;
	color = a_color;
	//EXTENSION-vert-post

	gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(result, 1.0);
}