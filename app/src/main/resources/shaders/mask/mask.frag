#version 330 core

in vec2 vUV;

#define MAX_INPUTS 8

uniform sampler2D uInputTextures[MAX_INPUTS];
uniform int uInputCount;
uniform int uInputModes[MAX_INPUTS];

uniform int maskMode;

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

void main() {
    if (uInputCount<=0) {
        FragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec4 inputTex = texture(uInputTextures[0], vUV);

    float maskValue = 1.0;
    if (uInputCount>1) {
        vec4 maskTex = texture(uInputTextures[1], vUV);
        maskValue = sampleInput(maskTex, uInputModes[1]);
    }

    vec4 color;
    if (maskMode==1) { // True alpha mask
        color = vec4(inputTex.rgb, inputTex.a*maskValue);
    }
    else if (maskMode==2) { // Visual matte
        color = vec4(inputTex.rgb*maskValue, 1.0);
    }
    else if (maskMode==3) { // Premult respects input alpha
        float a = inputTex.a*maskValue;
        color = vec4(inputTex.rgb*a, a);
    }
    
    FragColor = color;
}