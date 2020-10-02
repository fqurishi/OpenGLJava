#version 430

in vec4 incolor;
out vec4 outcolor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

void main(void) {
    outcolor = incolor;
	
}