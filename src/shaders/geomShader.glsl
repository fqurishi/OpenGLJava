#version 430

layout (triangles) in;

in vec3 varyingNormal[];
in vec3 varyingLightDir[];
in vec3 varyingHalfVector[];
in vec2 tc[];
in vec4 shadow_coord[];

out vec3 varyingNormalG;
out vec3 varyingLightDirG;
out vec3 varyingHalfVectorG;
out vec2 tcG;
out vec4 shadow_coordG;

layout (triangle_strip, max_vertices=15) out;

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

void main (void)
{	if (mod(gl_PrimitiveIDIn,3)!=0)
	{	for (int i=0; i<3; i++)
		{	gl_Position = proj_matrix * gl_in[i].gl_Position;
			varyingNormalG = varyingNormal[i];
			varyingLightDirG = varyingLightDir[i];
			varyingHalfVectorG = varyingHalfVector[i];
		tcG = tc[i];
		shadow_coordG = shadow_coord[i];
		EmitVertex();
		}
	}
	EndPrimitive();
}