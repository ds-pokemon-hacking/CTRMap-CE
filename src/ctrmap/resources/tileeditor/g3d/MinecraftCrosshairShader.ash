#version HOUSTON_EX

//Aftershader for rendering the color inversion crosshair in STE Minecraft Mode

//EXTENSION-CODE region=ash-frag-post
/*
vec2 uvPixels = abs((uv * screenDim) - (screenDim * 0.5)); //relative to center of screen
if ((uvPixels.x < 15.0 && uvPixels.y < 2.0) || (uvPixels.x < 2.0 && uvPixels.y < 15.0)){
	target.rgb = vec3(1.0) - target.rgb;
}
*/