#version 330 core

in vec2 vScreenPos;
in vec2 vLocalPos;
flat in vec2 vSize;

out vec4 FragColor;

vec3 hsbToRgb(vec3 hsb) {
    vec3 rgb = clamp(
        abs(mod(hsb.x * 6.0 + vec3(0.0, 4.0, 2.0), 6.0) - 3.0) - 1.0,
        0.0,
        1.0
    );

    rgb = rgb * rgb * (3.0 - 2.0 * rgb);

    return hsb.z * mix(vec3(1.0), rgb, hsb.y);
}

void main() {
    vec2 local = clamp(vLocalPos + 0.5, 0.0, 1.0);

    float hue = 1.0-local.x;
    float saturation = 1.0-local.y;
    float brightness = 1.0;

    vec3 rgb = hsbToRgb(vec3(hue, saturation, brightness));
    FragColor = vec4(rgb, 1.0);
}
