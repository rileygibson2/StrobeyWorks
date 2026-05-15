#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 uModel;
uniform mat4 uProjection;

out vec2 vScreenPos;
out vec2 vLocalPos;
flat out vec2 vSize;

void main() {
    vLocalPos = aPos.xy;

    vec4 world = uModel * vec4(aPos, 1.0);
    vScreenPos = world.xy;

    vSize = vec2(
        length(uModel[0].xyz),
        length(uModel[1].xyz)
    );

    gl_Position = uProjection * uModel * vec4(aPos, 1.0);
}