#version 330 core

in vec2 vUV;

out vec4 FragColor;

const float PI = 3.14159265359;

uniform float uTime;

uniform float uSpeedValue;
uniform float uGridSizeValue;

uniform float uSeedOffsetValue;
uniform float uSaltValue;

uniform float uOctavesValue;
uniform float uLacunarity;
uniform float uPersistence;

uniform float uGammaValue;
uniform float uGainValue;

uniform int uWarpValue;
uniform float uWarpStrengthValue;
uniform float uWarpScaleValue;

uniform int uOctaveRidgeValue;
uniform int uPostRidgeValue;
uniform float uRidgePowValue;

uniform int uOctaveTurbulenceValue;
uniform float uTurbulencePowValue;

uniform vec3 uColorLow;
uniform vec3 uColorHigh;

float hash(vec3 p) {
    vec3 saltVec = vec3(
        fract(uSaltValue * 0.1031),
        fract(uSaltValue * 0.1137),
        fract(uSaltValue * 0.1371)
    );

    p = fract(p * vec3(123.34, 456.21, 789.56) + saltVec);
    p += dot(p, p.yzx + 45.32);

    return fract((p.x + p.y) * p.z);
}

vec3 gradient(vec3 gridPoint) {
    float z = hash(gridPoint) * 2.0 - 1.0;
    float angle = hash(gridPoint + vec3(17.13, 31.71, 47.77)) * 2.0 * PI;
    float radius = sqrt(max(0.0, 1.0 - z * z));

    return vec3(
        radius * cos(angle),
        radius * sin(angle),
        z
    );
}

vec3 fade(vec3 t) {
    return t * t * t * (t * (t * 6.0 - 15.0) + 10.0);
}

float perlinNoise(vec3 position) {
    vec3 cell = floor(position);
    vec3 local = fract(position);
    vec3 blend = fade(local);

    float c000 = dot(gradient(cell + vec3(0, 0, 0)), local - vec3(0, 0, 0));
    float c100 = dot(gradient(cell + vec3(1, 0, 0)), local - vec3(1, 0, 0));
    float c010 = dot(gradient(cell + vec3(0, 1, 0)), local - vec3(0, 1, 0));
    float c110 = dot(gradient(cell + vec3(1, 1, 0)), local - vec3(1, 1, 0));

    float c001 = dot(gradient(cell + vec3(0, 0, 1)), local - vec3(0, 0, 1));
    float c101 = dot(gradient(cell + vec3(1, 0, 1)), local - vec3(1, 0, 1));
    float c011 = dot(gradient(cell + vec3(0, 1, 1)), local - vec3(0, 1, 1));
    float c111 = dot(gradient(cell + vec3(1, 1, 1)), local - vec3(1, 1, 1));

    float x00 = mix(c000, c100, blend.x);
    float x10 = mix(c010, c110, blend.x);
    float x01 = mix(c001, c101, blend.x);
    float x11 = mix(c011, c111, blend.x);

    float y0 = mix(x00, x10, blend.y);
    float y1 = mix(x01, x11, blend.y);

    return mix(y0, y1, blend.z);
}

float fbm(vec3 position, bool ridged, bool turbulence) {
    float total = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    float amplitudeSum = 0.0;

    for (int octave = 0; octave < 10; octave++) {
        if (octave >= int(uOctavesValue)) {
            break;
        }

        float n = perlinNoise(position * frequency);

        if (ridged) {
            n = 1.0 - abs(n);
            n = pow(clamp(n, 0.0, 1.0), uRidgePowValue);
        } else if (turbulence) {
            n = pow(abs(n), uTurbulencePowValue);
        }

        total += n * amplitude;
        amplitudeSum += amplitude;

        frequency *= uLacunarity;
        amplitude *= uPersistence;
    }

    return total / amplitudeSum;
}

float warpedFbm(vec3 position) {
    vec3 warpPosition = position * uWarpScaleValue;

    vec3 warp = vec3(
        fbm(warpPosition + vec3(12.7, 3.1, 8.4), false, false),
        fbm(warpPosition + vec3(5.2, 19.3, 1.7), false, false),
        fbm(warpPosition + vec3(9.8, 2.4, 23.6), false, false)
    );

    return fbm(
        position + warp * uWarpStrengthValue,
        uOctaveRidgeValue == 1,
        uOctaveTurbulenceValue == 1
    );
}

void main() {
    vec3 position = vec3(
        vUV * int(uGridSizeValue),
        uTime * uSpeedValue
    );

    position += vec3(
        uSeedOffsetValue * 0.01,
        uSeedOffsetValue * 0.013,
        uSeedOffsetValue * 0.017
    );

    float noise;
    if (uWarpValue == 1) {
        noise = warpedFbm(position);
    } else {
        noise = fbm(
            position,
            uOctaveRidgeValue == 1,
            uOctaveTurbulenceValue == 1
        );
    }

    if (uPostRidgeValue == 1) {
        noise = 1.0 - abs(noise);
        noise = pow(noise, uRidgePowValue);
    } else if (uOctaveRidgeValue == 0 && uOctaveTurbulenceValue == 0) {
        noise = noise * 0.5 + 0.5;
    }

    noise = pow(clamp(noise, 0.0, 1.0), uGammaValue) * uGainValue;
    noise = clamp(noise, 0.0, 1.0);

    vec3 color = mix(uColorLow, uColorHigh, noise);

    FragColor = vec4(color, 1.0);
}