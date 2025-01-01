$input a_position
$output v_color0

#include <bgfx_shader.sh>

float rand(vec2 co){
    return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);
}

vec2 rand2(vec2 co){
	return vec2(rand(co), rand(co + vec2(1.0)));
}

void main()
{
	// vec2 random_xy = rand2(gl_FragCoord.xy); // this cannot be used in vertex shader
	vec2 random_xy = rand2(a_position.xy);

	// gl_Position = vec4(0.0, 0.0, 0.0, 1.0);
	gl_Position = vec4(random_xy, 0.0, 1.0);
	v_color0 = vec4(1.0, 0.0, 0.0, 1.0);
}