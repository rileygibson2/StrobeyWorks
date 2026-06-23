#version 330 core

in vec2 vLocalPos;
in vec2 vScreenPos;
flat in vec2 vSize;

uniform vec4 uColor;
uniform float uOpacity;

uniform int uDebugEnabled;
uniform vec4 uDebugColor;

uniform int uClipEnabled;
uniform vec4 uClipBounds; // minX, minY, maxX, maxY in UI coordinates

uniform vec2 uStart;      // local pixels inside connection box
uniform vec2 uEnd;        // local pixels inside connection box
uniform float uThickness; // pixels

out vec4 FragColor;

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

float distanceToSegment(vec2 p, vec2 a, vec2 b) {
    vec2 ab = b - a;
    float lenSq = dot(ab, ab);

    if (lenSq <= 0.0001) {
        return length(p - a);
    }

    float t = clamp(dot(p - a, ab) / lenSq, 0.0, 1.0);
    vec2 closest = a + ab * t;

    return length(p - closest);
}

void main() {
    applyClip();

    // Your quad is center-based, so convert from -0.5..0.5 to 0..size pixels.
    vec2 p = (vLocalPos + vec2(0.5)) * vSize;

    float d = distanceToSegment(p, uStart, uEnd);

    float edgeSoftness = 1.0;
    float alpha = 1.0 - smoothstep(
        uThickness,
        uThickness + edgeSoftness,
        d
    );

    vec4 color = vec4(uColor.rgb, uColor.a * alpha * uOpacity);

    if (uDebugEnabled == 1) {
        color = mix(color, uDebugColor, 0.5);
    }

    FragColor = color;
}