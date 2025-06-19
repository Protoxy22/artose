#version 430

layout (local_size_x = 1, local_size_y = 1) in;

layout(binding = 0, rgba32f) uniform image2DArray waveBuffersImage;
layout(binding = 1) uniform sampler2DArray waveBuffersSampler;

uniform int u_textureResolution;
uniform float u_texelSizes[8];
uniform int u_lodIndex;

vec4 WorldToUV(vec2 worldPos, int textureResolution, float texelSizes[8], int lodIndex) {
    vec2 uv = worldPos / (texelSizes[lodIndex] * float(textureResolution)) + vec2(0.5);
    return vec4(uv.x, uv.y, float(lodIndex), 0.0);
}

vec2 UVToWorld(vec2 uv, int textureResolution, float texelSizes[8], int lodIndex) {
    return texelSizes[lodIndex] * float(textureResolution) * (uv - vec2(0.5));
}

void main() {
    int x = int(gl_GlobalInvocationID.x);
    int y = int(gl_GlobalInvocationID.y);

    vec2 current_uv = vec2(x, y) / float(u_textureResolution);
    vec2 worldPos = UVToWorld(current_uv, u_textureResolution, u_texelSizes, u_lodIndex);
    vec4 next_coords = WorldToUV(worldPos, u_textureResolution, u_texelSizes, u_lodIndex + 1);

    vec4 current_data = imageLoad(waveBuffersImage, ivec3(x, y, u_lodIndex));
    vec4 next_data = texture(waveBuffersSampler, next_coords.xyz);

    vec4 combined_data = current_data + next_data;
    imageStore(waveBuffersImage, ivec3(x, y, u_lodIndex), combined_data);
}
