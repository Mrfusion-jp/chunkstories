#version 330
//uniform vec2 screenSize;

layout(std140) uniform MatrixBlock {
	mat4 firstMatrix;
	mat4 secondMatrix;
} howdy;

in vec3 vertexIn;
out vec2 texCoord;

void main()
{
	gl_Position = vec4(vertexIn.xy, 0.0, 1.0);
	texCoord = vertexIn.xy*0.5;
}