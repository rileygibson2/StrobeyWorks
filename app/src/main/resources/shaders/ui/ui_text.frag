#version 330 core

in vec2 vUV;

uniform sampler2D uFontAtlas;
uniform vec4 uColor;

uniform int uDebugEnabled;
uniform vec4 uDebugColor;

out vec4 fragColor;

vec4 alphaOver(vec4 top, vec4 bottom) {
    float outA = top.a + bottom.a * (1.0 - top.a);

    if (outA <= 0.0001) {
        return vec4(0.0);
    }

    vec3 outRGB = (
        top.rgb * top.a +
        bottom.rgb * bottom.a * (1.0 - top.a)
    ) / outA;

    return vec4(outRGB, outA);
}

void main() {
    float glyphAlpha = texture(uFontAtlas, vUV).r;
    vec4 textColor = vec4(uColor.rgb, uColor.a * glyphAlpha);

    if (uDebugEnabled == 1) {
        fragColor = alphaOver(textColor, uDebugColor);
        return;
    }

    if (textColor.a <= 0.0) {
        discard;
    }

    fragColor = textColor;
}
