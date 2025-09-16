//EXTENSION-frag-define

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

PASSTHROUGH vec2 uv;

//EXTENSION-frag-init

void main(void){
    vec4 outColor = texture2D(textures[0], uv);
    OUTPUT = outColor;
}