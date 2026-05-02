#version 330 core

in vec2 vLocalPos;
flat in vec2 vSize;

uniform vec3 uColor;
uniform vec4 uCornerRadius;

uniform int uHasBorder;
uniform vec3 uBorderColor;
uniform float uBorderThickness;

out vec4 FragColor;

float roundedRectSDF(vec2 p, vec2 size, vec4 radii) {
    bool left = p.x < size.x * 0.5;
    bool top = p.y < size.y * 0.5;

    float radius = 0.0;

    if (left && top) {
        radius = radii.x;
    } else if (!left && top) {
        radius = radii.y;
    } else if (!left && !top) {
        radius = radii.z;
    } else {
        radius = radii.w;
    }

    radius = clamp(radius, 0.0, min(size.x, size.y) * 0.5);

    vec2 halfSize = size * 0.5;
    vec2 q = abs(p - halfSize) - (halfSize - vec2(radius));

    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 p = (vLocalPos + 0.5) * vSize;

    float outerD = roundedRectSDF(p, vSize, uCornerRadius);
    float aa = max(fwidth(outerD), 0.001);

    float outerAlpha = 1.0 - smoothstep(0.0, aa, outerD);

    if (outerAlpha <= 0.0) {
        discard;
    }

    if (uHasBorder == 0) {
        FragColor = vec4(uColor, outerAlpha);
        return;
    }

    float thickness = clamp(
        uBorderThickness,
        0.0,
        min(vSize.x, vSize.y) * 0.5
    );

    vec2 innerSize = max(vSize - vec2(thickness * 2.0), vec2(0.0));
    vec2 innerP = p - vec2(thickness);
    vec4 innerRadius = max(uCornerRadius - vec4(thickness), vec4(0.0));

    float innerD = roundedRectSDF(innerP, innerSize, innerRadius);
    float innerAlpha = 1.0 - smoothstep(0.0, aa, innerD);

    float borderMask = outerAlpha * (1.0 - innerAlpha);
    float fillMask = innerAlpha;

    vec3 finalColor = mix(uColor, uBorderColor, borderMask);
    float finalAlpha = max(fillMask, borderMask);

    FragColor = vec4(finalColor, finalAlpha);
}
