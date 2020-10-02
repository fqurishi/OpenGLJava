#version 430

layout (location=0) in vec4 vertPos;
layout (location=1) in vec4 vertNormal;
layout (location=2) in vec2 tCoord;

out vec2 tc;
out vec3 varyingNormal; 
out vec3 varyingLightDir;
out vec3 varyingHalfVector;
out vec4 shadow_coord;

struct PositionalLight
{	vec4 ambient;
	vec4 diffuse;
	vec4 specular;
	vec3 position;
};
struct Material
{	vec4 ambient;  
	vec4 diffuse;  
	vec4 specular;  
	float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D s;

void main(void)
{
	varyingNormal = (norm_matrix * vertNormal).xyz;
	varyingLightDir = light.position - (mv_matrix*vertPos).xyz;	
	varyingHalfVector = normalize(varyingLightDir) + normalize(varyingNormal);
	shadow_coord = shadowMVP * vertPos;
	gl_Position = mv_matrix * vertPos;
	tc = tCoord;
}
