//EXTENSION-ash-frag-define

#ifdef GL2
	#define PASSTHROUGH varying
	#define OUTPUT gl_FragColor
#elif defined(GL4) || defined(GL3)
	#define PASSTHROUGH in
	#define OUTPUT color
#endif

#ifdef GL4
layout(location = 0) out vec4 color;
#endif
#ifdef GL3
out vec4 color;
#endif

uniform sampler2D textures[TEXTURE_MAX];

uniform float time;

uniform vec2 screenDim;

PASSTHROUGH vec2 uv;

//EXTENSION-frag-init

void main(void){
	//EXTENSION-ash-frag-main
    vec4 outColor = texture2D(textures[0], uv);
    //EXTENSION-ash-frag-post target=outColor
    OUTPUT = outColor;
}