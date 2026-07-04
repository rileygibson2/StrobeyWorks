#version 330 core

in vec2 vUV;
out vec4 FragColor;

uniform sampler2D uMainFeedTex;
uniform int uMainFeedTexMode;

uniform float uGridSizeValue;

vec4 sampleInputColor(vec4 tex, int mode) {
    if (mode == 0) return vec4(tex.r, tex.r, tex.r, tex.a);
    if (mode == 1) return vec4(tex.g, tex.g, tex.g, tex.a);
    if (mode == 2) return vec4(tex.b, tex.b, tex.b, tex.a);
    if (mode == 3) return vec4(tex.a, tex.a, tex.a, tex.a);

    if (mode == 4) {
        float l = dot(tex.rgb, vec3(0.2126, 0.7152, 0.0722));
        return vec4(l, l, l, tex.a);
    }

    return tex;
}

void main() {
    float gridSize = max(1.0, uGridSizeValue);

    vec2 cellCount = vec2(gridSize);
    vec2 cell = floor(vUV * cellCount);
    vec2 sampleUV = (cell + 0.5) / cellCount;

    vec4 tex = texture(uMainFeedTex, sampleUV);
    FragColor = sampleInputColor(tex, uMainFeedTexMode);
}