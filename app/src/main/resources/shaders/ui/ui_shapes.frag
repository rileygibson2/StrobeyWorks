#version 330 core

in vec2 vScreenPos;
in vec2 vLocalPos;
flat in vec2 vSize;

uniform int uPrimType;

uniform float uOpacity;
uniform vec4 uColor;
uniform vec4 uCornerRadius;

uniform int uHasBorder;
uniform vec4 uBorderColor;
uniform float uBorderThickness;
uniform vec4 uBorderSides; // top, right, bottom, left

uniform int uDebugEnabled;
uniform vec4 uDebugColor;

uniform int uClipEnabled;
uniform vec4 uClipBounds; // minX, minY, maxX, maxY in UI coordinates

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

float borderSideMask(vec2 p, vec2 size, vec4 sides, float aa) {
    float dTop = p.y;
    float dRight = size.x - p.x;
    float dBottom = size.y - p.y;
    float dLeft = p.x;

    float horizontalSide = dLeft < dRight ? sides.w : sides.y;
    float verticalSide = dTop < dBottom ? sides.x : sides.z;

    float horizontalDist = min(dLeft, dRight);
    float verticalDist = min(dTop, dBottom);

    float t = smoothstep(-aa, aa, horizontalDist - verticalDist);

    return mix(horizontalSide, verticalSide, t);
}

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

void outputShape(float outerD, float innerD, float sideMask) {
    float outerAa = max(fwidth(outerD), 0.001);
    float outerAlpha = 1.0 - smoothstep(-outerAa, outerAa, outerD);

    if (outerAlpha <= 0.0) {
        if (uDebugEnabled == 1) {
            FragColor = uDebugColor;
            return;
        }

        discard;
    }

    vec4 shapeColor;

    if (uHasBorder == 1) {
        float innerAa = max(fwidth(innerD), 0.001);
        float innerAlpha = 1.0 - smoothstep(-innerAa, innerAa, innerD);

        sideMask = step(0.5, sideMask);

        float borderMask = outerAlpha * (1.0 - innerAlpha) * sideMask;
        float fillMask = innerAlpha;

        vec4 fill = vec4(uColor.rgb, uColor.a * fillMask);
        vec4 border = vec4(uBorderColor.rgb, uBorderColor.a * borderMask);

        shapeColor = alphaOver(border, fill);
    } else {
        shapeColor = vec4(uColor.rgb, uColor.a * outerAlpha);
    }

    if (uDebugEnabled == 1) {
        FragColor = alphaOver(shapeColor, uDebugColor);
        return;
    }

    if (shapeColor.a <= 0.0) {
        discard;
    }

    shapeColor.a = shapeColor.a*uOpacity;
    shapeColor = clamp(shapeColor, vec4(0.0), vec4(1.0));

    FragColor = shapeColor;
}

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

        outputShape(outerD, innerD, 1.0);
        return;
    }

    if (uPrimType == 3) {
        float outerD = circleSDF(p, vSize);

        vec2 center = vSize * 0.5;
        float outerRadius = min(vSize.x, vSize.y) * 0.5;
        float innerRadius = max(outerRadius - thickness, 0.0);

        float innerD = length(p - center) - innerRadius;

        outputShape(outerD, innerD, 1.0);
        return;
    }

    float outerD = roundedRectSDF(p, vSize, uCornerRadius);

    vec4 sides = clamp(uBorderSides, vec4(0.0), vec4(1.0));

    float insetTop = thickness * sides.x;
    float insetRight = thickness * sides.y;
    float insetBottom = thickness * sides.z;
    float insetLeft = thickness * sides.w;

    vec2 innerOffset = vec2(insetLeft, insetTop);

    vec2 innerSize = max(
        vSize - vec2(insetLeft + insetRight, insetTop + insetBottom),
        vec2(0.0)
    );

    vec2 innerP = p - innerOffset;

    vec4 innerRadius = vec4(
        max(uCornerRadius.x - max(insetTop, insetLeft), 0.0),
        max(uCornerRadius.y - max(insetTop, insetRight), 0.0),
        max(uCornerRadius.z - max(insetBottom, insetRight), 0.0),
        max(uCornerRadius.w - max(insetBottom, insetLeft), 0.0)
    );

    float innerD = roundedRectSDF(innerP, innerSize, innerRadius);

    float sideMask = borderSideMask(p, vSize, sides, 2.0);

    outputShape(outerD, innerD, sideMask);
}
