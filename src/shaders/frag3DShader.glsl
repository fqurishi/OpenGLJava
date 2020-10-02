#version 430

in vec3 varyingNormal;
in vec3 originalPosition;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec4 shadow_coord;

out vec4 fragColor;

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
uniform mat4 texRot_matrix;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=7) uniform sampler3D s;

float lookup(float x, float y)
{  	float t = textureProj(shadowTex, shadow_coord + vec4(x * 0.001 * shadow_coord.w,
                                                         y * 0.001 * shadow_coord.w,
                                                         -0.01, 0.0));
	return t;
}

void main(void)
{	float shadowFactor=0.0;
	// normalize the light, normal, and view vectors:
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	float swidth = 2.5;
	vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor = shadowFactor / 4.0;
	
	// compute light reflection vector, with respect N:
	vec3 R = normalize(reflect(-L, N));
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// angle between the view vector and reflected light:
	float cosPhi = dot(V,R);
	
	vec4 t = texture(s,originalPosition/3.0 + 0.5);

	vec4 shadowColor = globalAmbient * material.ambient
				+ light.ambient * material.ambient;
	
	vec4 lightedColor = light.diffuse * material.diffuse * max(cosTheta,0.0)
				+ 0.5 * light.specular * material.specular
				* pow(max(cosPhi,0.0),material.shininess);

	// compute ADS contributions (per pixel):
	fragColor = 0.7 * t * vec4((shadowColor.xyz + shadowFactor*(lightedColor.xyz)),1.0);
}