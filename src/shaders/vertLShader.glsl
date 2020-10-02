#version 430

layout (location = 0) in vec3 position;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform int colorFlag;
out vec4 incolor;


void main(void) {
	if (colorFlag == 1){
		incolor = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else{
		incolor = vec4(1.0, 1.0, 0.4, 1.0);    
	}
	
    gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
}