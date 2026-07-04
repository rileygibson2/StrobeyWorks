#version 330 core

in vec2 vUV;

#define MAX_INPUTS 8

uniform sampler2D uMainFeedTex;
uniform int uMainFeedTexMode;

uniform int uMaskSourceType;

uniform float uMaskValue;
uniform sampler2D uMaskTex;
uniform int uMaskTexMode;

uniform int uMaskModeValue;

out vec4 FragColor;

float sampleInput(vec4 tex, int mode) {
    if (mode == 0) return tex.r;
    if (mode == 1) return tex.g;
    if (mode == 2) return tex.b;
    if (mode == 3) return tex.a;

    if (mode == 4) { // luminance
        return dot(tex.rgb, vec3(0.2126, 0.7152, 0.0722));
    }

    return 1.0; // unnecessary
}

vec4 sampleInputVec4(vec4 tex, int mode) {
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

float getMaskValue() {
    float maskValue = 0.0;

    if (uMaskSourceType == 0) maskValue = uMaskValue;

    if (uMaskSourceType == 1) {
        vec4 maskTex = texture(uMaskTex, vUV);
        maskValue = sampleInput(maskTex, uMaskTexMode);
    }

    return maskValue;
}

void main() {
    vec4 inputTex = sampleInputVec4(texture(uMainFeedTex, vUV), uMainFeedTexMode);

    float maskValue = getMaskValue();
    vec4 color = vec4(inputTex.rgb, inputTex.a);
    
    if (uMaskModeValue==0) { // True alpha mask
        color = vec4(inputTex.rgb, inputTex.a*maskValue);
    }
    else if (uMaskModeValue==1) { // Visual matte
        color = vec4(inputTex.rgb*maskValue, 1.0);
    }
    else if (uMaskModeValue==2) { // Premult respects input alpha
        float a = inputTex.a*maskValue;
        color = vec4(inputTex.rgb*a, a);
    }
    
    FragColor = color;
}