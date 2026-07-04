#version 330 core

in vec2 vUV;
out vec4 FragColor;

uniform sampler2D uMainFeedTex;
uniform int uMainFeedTexMode;

uniform float uGridSizeValue;
uniform float uDotScaleValue;   // 0.0-1.0, optional
uniform int uInvertValue;       // optional bool-as-int

float sampleScalar(vec4 tex, int mode) {
    if (mode == 0) return tex.r;
    if (mode == 1) return tex.g;
    if (mode == 2) return tex.b;
    if (mode == 3) return tex.a;

    if (mode == 4) {
        return dot(tex.rgb, vec3(0.2126, 0.7152, 0.0722));
    }

    return dot(tex.rgb, vec3(0.2126, 0.7152, 0.0722));
}

void main() {
    float gridSize = max(1.0, uGridSizeValue);
    float dotScale = clamp(uDotScaleValue, 0.0, 1.0);

    vec2 grid = vec2(gridSize);

    // Cell identity and local position.
    vec2 cell = floor(vUV * grid);
    vec2 cellUV = fract(vUV * grid);

    // Center of this cell in UV space.
    vec2 centerUV = (cell + 0.5) / grid;

    // Read source once per cell.
    vec4 source = texture(uMainFeedTex, centerUV);
    float lum = sampleScalar(source, uMainFeedTexMode);

    if (uInvertValue == 1) {
        lum = 1.0 - lum;
    }

    // Distance inside the cell, normalized so cell center is vec2(0).
    vec2 p = cellUV - 0.5;

    // Radius in cell-local units. Max useful radius is about 0.5.
    float radius = lum * 0.5 * dotScale;

    float dist = length(p);

    // Anti-aliased circle edge.
    float aa = fwidth(dist);
    float dotMask = 1.0 - smoothstep(radius - aa, radius + aa, dist);

    // White background, grayscale dot.
    vec3 bg = vec3(0.0);
    vec3 dotColor = vec3(lum);

    vec3 color = mix(bg, dotColor, dotMask);

    FragColor = vec4(color, 1.0);
}