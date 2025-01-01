$input v_color0

#include <bgfx_shader.sh>

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec2 rand2(vec2 co){
	return vec2(rand(co), rand(co + vec2(1.0)));
}

void main()
{
	vec2 rg = rand2(gl_FragCoord.xy);
	float b = rand(gl_FragCoord.xy);
	vec3 rgb = vec3(rg, b);
	// gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0);
	gl_FragColor = vec4(rgb, 1.0);
}