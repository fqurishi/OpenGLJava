#version 430

in vec2 tcG;
in vec3 varyingNormalG;
in vec3 varyingLightDirG;
in vec3 varyingHalfVectorG;
in vec4 shadow_coordG;
 
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
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;
layout (binding=1) uniform sampler2D s;

float lookup(float x, float y)
{  	float t = textureProj(shadowTex, shadow_coordG + vec4(x * 0.001 * shadow_coordG.w,
                                                         y * 0.001 * shadow_coordG.w,
                                                         -0.01, 0.0));
	return t;
}

void main(void)
{
	float shadowFactor=0.0;
	// normalized the light, normal, and eye direction vectors
	vec3 L = normalize(varyingLightDirG);
	vec3 N = normalize(varyingNormalG);
	vec4 textureColor = texture(s, tcG);
	float swidth = 2.5;
	vec2 o = mod(floor(gl_FragCoord.xy), 2.0) * swidth;
	shadowFactor += lookup(-1.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup(-1.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x,  1.5*swidth - o.y);
	shadowFactor += lookup( 0.5*swidth + o.x, -0.5*swidth - o.y);
	shadowFactor = shadowFactor / 4.0;
	
	// get the angle between the light and surface normal
	float cosTheta = dot(L,N);
	
	// halfway vector was computed in vertex shader, and interpolated
	vec3 H = normalize(varyingHalfVectorG);
	
	vec4 shadowColor = globalAmbient * material.ambient
				+ light.ambient * material.ambient;
				
	vec4 lightedColor = light.diffuse * material.diffuse * max(cosTheta,0.0)
				+ light.specular * material.specular
				* pow(max(dot(H,N),0.0),material.shininess*3.0) * 0.6 + (textureColor) * 0.4;

	fragColor = vec4((shadowColor.xyz + shadowFactor*(lightedColor.xyz)),1.0);
}
