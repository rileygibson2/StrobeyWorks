#version 330 core

in vec2 vUV;

out vec4 FragColor;

const float PI = 3.14159265359;

uniform float uTime;
uniform float uSpeed;
uniform int uGridSize;

uniform int uOctaves;
uniform float uLacunarity;
uniform float uPersistence;

uniform float uGamma;
uniform float uGain;

uniform int uWarp;
uniform float uWarpStrength;
uniform float uWarpScale;

uniform int uOctaveRidge;
uniform int uPostRidge;
uniform float uRidgePow;

uniform int uOctaveTurbulence;
uniform float uTurbulencePow;

uniform vec3 uColorLow;
uniform vec3 uColorHigh;

// Produces a repeatable pseudo-random value between 0 and 1
// for each integer grid coordinate.
float hash(vec3 p) {
    p = fract(p * vec3(123.34, 456.21, 789.56));
    p += dot(p, p.yzx + 45.32);

    return fract((p.x + p.y) * p.z);
}

// Gives each grid point a pseudo-random unit direction.
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

// Smooths interpolation near grid boundaries.
// This is Perlin's quintic fade curve: 6t^5 - 15t^4 + 10t^3.
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

    // GLSL prefers a compile-time loop limit.
    for (int octave = 0; octave < 10; octave++) {
        if (octave >= uOctaves) {
            break;
        }

        float n = perlinNoise(position * frequency);

        if (ridged) {
            n = 1.0 - abs(n);
            n = pow(clamp(n, 0.0, 1.0), uRidgePow);
        } else if (turbulence) {
            n = pow(abs(n), uTurbulencePow);
        }

        total += n * amplitude;
        amplitudeSum += amplitude;

        frequency *= uLacunarity;
        amplitude *= uPersistence;
    }

    return total / amplitudeSum;
}

float warpedFbm(vec3 position) {
    vec3 warpPosition = position * uWarpScale;

    vec3 warp = vec3(
        fbm(warpPosition + vec3(12.7, 3.1, 8.4), false, false),
        fbm(warpPosition + vec3(5.2, 19.3, 1.7), false, false),
        fbm(warpPosition + vec3(9.8, 2.4, 23.6), false, false)
    );

    // fbm is centred near zero, so this bends coordinates in both directions.
    return fbm(
        position + warp * uWarpStrength,
        uOctaveRidge == 1 ? true : false,
        uOctaveTurbulence == 1 ? true : false
    );
}

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
    vec3 position = vec3(
        vUV * uGridSize,
        uTime * uSpeed
    );

    // Noise
    float noise;
    if (uWarp==1) {
        noise = warpedFbm(position);
    } else {
        noise = fbm(
            position,
            uOctaveRidge == 1 ? true : false,
            uOctaveTurbulence == 1 ? true : false
        );
    }

    // Post processing
    if (uPostRidge == 1) {
        noise = 1.0 - abs(noise);
        noise = pow(noise, uRidgePow);
    } else if (uOctaveRidge == 0 && uOctaveTurbulence == 0) {
        noise = noise * 0.5 + 0.5;
    }

    noise = pow(clamp(noise, 0.0, 1.0), uGamma) * uGain;
    noise = clamp(noise, 0.0, 1.0);
    
    vec3 color = mix(uColorLow, uColorHigh, noise);

    FragColor = vec4(color, 1.0);
}