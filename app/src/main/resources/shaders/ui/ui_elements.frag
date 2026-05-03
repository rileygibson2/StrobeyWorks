#version 330 core

in vec2 vLocalPos;
flat in vec2 vSize;

uniform int uPrimType;

uniform vec3 uColor;
uniform vec4 uCornerRadius;

uniform int uHasBorder;
uniform vec3 uBorderColor;
uniform float uBorderThickness;

uniform int uDebugEnabled;
uniform vec3 uDebugColor;

out vec4 FragColor;

float roundedRectSDF(vec2 p, vec2 size, vec4 radii) {
    bool left = p.x < size.x * 0.5;
    bool top = p.y < size.y * 0.5;

    float radius = 0.0;

    if (left && top) radius = radii.x;
    else if (!left && top) radius = radii.y;
    else if (!left && !top) radius = radii.z;
    else radius = radii.w;

    radius = clamp(radius, 0.0, min(size.x, size.y) * 0.5);

    vec2 halfSize = size * 0.5;
    vec2 q = abs(p - halfSize) - (halfSize - vec2(radius));

    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

float ellipseSDF(vec2 p, vec2 size) {
    vec2 safeSize = max(size, vec2(0.001));
    vec2 center = safeSize * 0.5;
    vec2 radius = safeSize * 0.5;

    vec2 d = (p - center) / radius;

    return (length(d) - 1.0) * min(radius.x, radius.y);
}

float circleSDF(vec2 p, vec2 size) {
    vec2 center = size * 0.5;
    float radius = min(size.x, size.y) * 0.5;

    return length(p - center) - radius;
}

void outputShape(float outerD, float innerD) {
    float aa = max(fwidth(outerD), 0.001);
    float outerAlpha = 1.0 - smoothstep(0.0, aa, outerD);

    if (outerAlpha <= 0.0) {
        if (uDebugEnabled == 1) {
            FragColor = vec4(uDebugColor, 1.0);
            return;
        }

        discard;
    }

    vec3 shapeColor = uColor;
    float shapeAlpha = outerAlpha;

    if (uHasBorder == 1) {
        float innerAlpha = 1.0 - smoothstep(0.0, aa, innerD);

        float borderMask = outerAlpha * (1.0 - innerAlpha);
        float fillMask = innerAlpha;

        shapeColor = mix(uColor, uBorderColor, borderMask);
        shapeAlpha = max(fillMask, borderMask);
    }

    if (uDebugEnabled == 1) {
        vec3 finalColor = mix(uDebugColor, shapeColor, shapeAlpha);
        FragColor = vec4(finalColor, 1.0);
        return;
    }

    FragColor = vec4(shapeColor, shapeAlpha);
}

void main() {
    vec2 p = (vLocalPos + 0.5) * vSize;

    float thickness = clamp(
        uBorderThickness,
        0.0,
        min(vSize.x, vSize.y) * 0.5
    );

    if (uPrimType == 2) {
        float outerD = ellipseSDF(p, vSize);

        vec2 innerSize = max(vSize - vec2(thickness * 2.0), vec2(0.0));
        vec2 innerP = p - vec2(thickness);

        float innerD = ellipseSDF(innerP, innerSize);

        outputShape(outerD, innerD);
        return;
    }

    if (uPrimType == 3) {
        float outerD = circleSDF(p, vSize);

        vec2 center = vSize * 0.5;
        float outerRadius = min(vSize.x, vSize.y) * 0.5;
        float innerRadius = max(outerRadius - thickness, 0.0);

        float innerD = length(p - center) - innerRadius;

        outputShape(outerD, innerD);
        return;
    }

    float outerD = roundedRectSDF(p, vSize, uCornerRadius);

    vec2 innerSize = max(vSize - vec2(thickness * 2.0), vec2(0.0));
    vec2 innerP = p - vec2(thickness);
    vec4 innerRadius = max(uCornerRadius - vec4(thickness), vec4(0.0));

    float innerD = roundedRectSDF(innerP, innerSize, innerRadius);

    outputShape(outerD, innerD);
}
