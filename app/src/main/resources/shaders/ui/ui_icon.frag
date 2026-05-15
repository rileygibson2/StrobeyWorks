#version 330 core

in vec2 vScreenPos;
in vec2 vUV;

uniform sampler2D uTexture;
uniform vec4 uTint;

uniform int uClipEnabled;
uniform vec4 uClipBounds; // minX, minY, maxX, maxY in UI coordinates

out vec4 FragColor;

void applyClip() {
    if (uClipEnabled == 0) return;

    if (
        vScreenPos.x < uClipBounds.x ||
        vScreenPos.x > uClipBounds.z ||
        vScreenPos.y < uClipBounds.y ||
        vScreenPos.y > uClipBounds.w
    ) {
        discard;
    }
}

void main() {
    applyClip();
    
    vec4 tex = texture(uTexture, vUV);
    vec4 color = tex * uTint;

    if (color.a <= 0.0) discard;
    FragColor = color;
}