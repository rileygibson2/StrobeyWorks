#version 330 core

in vec2 vUV;

uniform sampler2D uTexture;
uniform vec4 uTint;

out vec4 FragColor;

void main() {
    vec4 tex = texture(uTexture, vUV);
    vec4 color = tex * uTint;

    if (color.a <= 0.0) discard;
    FragColor = color;
}