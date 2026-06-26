#version 330 core

in vec2 vUV;

#define MAX_INPUTS 8

uniform sampler2D uInputTextures[MAX_INPUTS];
uniform int uInputCount;
uniform int uInputModes[MAX_INPUTS];

uniform float uMixWeights[MAX_INPUTS];

out vec4 FragColor;

vec4 sampleInput(vec4 tex, int mode) {
    if (mode == 0) return vec4(tex.r, tex.r, tex.r, tex.a);
    if (mode == 1) return vec4(tex.g, tex.g, tex.g, tex.a);
    if (mode == 2) return vec4(tex.b, tex.b, tex.b, tex.a);
    if (mode == 3) return vec4(tex.a, tex.a, tex.a, 1.0);

    if (mode == 4) { // luminance
        float l = dot(tex.rgb, vec3(0.2126, 0.7152, 0.0722));
        return vec4(l, l, l, tex.a);
    }

    return tex; // unnecessary
}

void main() {
    if (uInputCount <= 0) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec4 sum = vec4(0.0);

    for (int i = 0; i < MAX_INPUTS; i++) {
        if (i >= uInputCount) break;
        
        vec4 tex = texture(uInputTextures[i], vUV);
        sum += sampleInput(tex, uInputModes[i])*uMixWeights[i];
    }

    FragColor = sum;
}