//EXTENSION-ash-vert-define

#ifdef GL2
	#define VTXATTR attribute
	#define PASSTHROUGH varying
#elif defined(GL4) || defined(GL3)
	#define VTXATTR in
	#define PASSTHROUGH out
#endif

VTXATTR vec3 a_position;
VTXATTR vec3 a_normal;
VTXATTR vec4 a_color;
VTXATTR vec2 a_texcoord;
VTXATTR vec2 a_texcoord1;
VTXATTR vec2 a_texcoord2;
VTXATTR vec4 a_boneIndices;
VTXATTR vec4 a_boneWeights;

uniform float time;

PASSTHROUGH vec2 uv;

//EXTENSION-ash-vert-init

void main(void){
	//EXTENSION-ash-vert-main
	vec4 outPosition = vec4(a_position * 2.0 - vec3(1.0), 1.0);
	uv = a_position.xy;
	//EXTENSION-ash-vert-post target=outPosition
    gl_Position = outPosition;
}