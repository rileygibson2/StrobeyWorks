#version 330 core

in vec2 vLocalPos;
flat in vec2 vSize;

uniform vec3 uColor;
uniform vec4 uCornerRadius;
uniform float uRadius;

out vec4 FragColor;

float roundedRectAlpha(vec2 localPos, vec2 size, vec4 radii) {
    vec2 p = (localPos + 0.5) * size;

    float radius = 0.0;

    bool left = p.x < size.x * 0.5;
    bool top = p.y < size.y * 0.5;

    if (left && top) {
        radius = radii.x; // top-left
    } else if (!left && top) {
        radius = radii.y; // top-right
    } else if (!left && !top) {
        radius = radii.z; // bottom-right
    } else {
        radius = radii.w; // bottom-left
    }

    radius = clamp(radius, 0.0, min(size.x, size.y) * 0.5);

    vec2 cornerCenter = vec2(
        left ? radius : size.x - radius,
        top ? radius : size.y - radius
    );

    vec2 dist = abs(p - cornerCenter);

    bool inCornerRegion =
        (left ? p.x < radius : p.x > size.x - radius) &&
        (top ? p.y < radius : p.y > size.y - radius);

    if (!inCornerRegion || radius <= 0.0) {
        return 1.0;
    }

    float d = length(p - cornerCenter) - radius;

    float aa = fwidth(d);
    return 1.0 - smoothstep(0.0, aa, d);
}

void main() {
    float alpha = roundedRectAlpha(vLocalPos, vSize, uCornerRadius);

    if (alpha <= 0.0) {
        discard;
    }

    FragColor = vec4(uColor, alpha);
}
