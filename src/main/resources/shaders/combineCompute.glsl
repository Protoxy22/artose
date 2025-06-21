#version 430

layout (local_size_x = 1, local_size_y = 1) in;

layout(binding = 0, rgba32f) uniform image2DArray waveBuffersImage;
layout(binding = 1) uniform sampler2DArray waveBuffersSampler;

uniform int u_textureResolution;
uniform float u_texelSizes[8];
uniform int u_lodIndex;

vec2 WorldToUV(vec2 worldPos, int textureResolution, float texelSizes[8], int lodIndex) {
    vec2 uv = worldPos / (texelSizes[lodIndex] * float(textureResolution)) + vec2(0.5);
    return uv;
}

vec2 UVToWorld(vec2 uv, int textureResolution, float texelSizes[8], int lodIndex) {
    return texelSizes[lodIndex] * float(textureResolution) * (uv - vec2(0.5));
}

void main() {
    int x = int(gl_GlobalInvocationID.x);
    int y = int(gl_GlobalInvocationID.y);

    ivec3 currentCoords = ivec3(x, y, u_lodIndex);
    vec2 currentUV = vec2(x, y) / float(u_textureResolution);
    vec2 worldPos = UVToWorld(currentUV, u_textureResolution, u_texelSizes, u_lodIndex);
    vec2 nextUV = WorldToUV(worldPos, u_textureResolution, u_texelSizes, u_lodIndex + 1);

    vec4 currentData = imageLoad(waveBuffersImage, currentCoords);
    vec4 nextData = textureLod(waveBuffersSampler, vec3(nextUV, float(u_lodIndex + 1)), 0.0);

    imageStore(waveBuffersImage, currentCoords, currentData + nextData);
}
