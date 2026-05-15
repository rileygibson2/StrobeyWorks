#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 uModel;
uniform mat4 uProjection;
uniform vec4 uUVRect; // x, y, w, h

out vec2 vScreenPos;
out vec2 vUV;

void main() {
    vec4 world = uModel * vec4(aPos, 1.0);
    vScreenPos = world.xy;

    vec2 baseUV = aPos.xy + vec2(0.5);
    vUV = uUVRect.xy + baseUV * uUVRect.zw;

    gl_Position = uProjection * world;
}