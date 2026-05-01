#version 330 core

#define MAX_DIRECTIONAL_LIGHTS 8
#define MAX_SPOT_LIGHTS 8

struct DirectionalLight {
    vec3 direction;
    vec3 color;
    float intensity;
};

struct SpotLight {
    vec3 position;
    vec3 direction;
    vec3 color;
    float intensity;

    float innerCutoffCos;
    float outerCutoffCos;

    float constant;
    float linear;
    float quadratic;
};

in vec3 vWorldPos;
in vec3 vNormal;

uniform int uDirectionalLightCount;
uniform DirectionalLight uDirectionalLights[MAX_DIRECTIONAL_LIGHTS];
uniform sampler2D uDirectionalShadowMaps[MAX_DIRECTIONAL_LIGHTS];
uniform mat4 uDirectionalLightSpaceMatrices[MAX_DIRECTIONAL_LIGHTS];
uniform int uDirectionalShadowEnabled[MAX_DIRECTIONAL_LIGHTS];

uniform int uSpotLightCount;
uniform SpotLight uSpotLights[MAX_SPOT_LIGHTS];
uniform sampler2D uSpotShadowMaps[MAX_SPOT_LIGHTS];
uniform mat4 uSpotLightSpaceMatrices[MAX_SPOT_LIGHTS];
uniform int uSpotShadowEnabled[MAX_SPOT_LIGHTS];

uniform vec3 uObjectColor;
uniform vec3 uViewPosition;

uniform float uAmbientStrength;
uniform float uSpecularStrength;
uniform float uShininess;

out vec4 FragColor;

float calculateDirectionalShadow(int lightIndex, vec3 worldPos, vec3 normal, vec3 lightDir) {
    if (uDirectionalShadowEnabled[lightIndex] == 0) {
        return 0.0;
    }

    vec4 lightSpacePos = uDirectionalLightSpaceMatrices[lightIndex] * vec4(worldPos, 1.0);

    vec3 projectedCoords = lightSpacePos.xyz / lightSpacePos.w;
    projectedCoords = projectedCoords * 0.5 + 0.5;

    if (projectedCoords.z > 1.0) {
        return 0.0;
    }

    if (
        projectedCoords.x < 0.0 || projectedCoords.x > 1.0 ||
        projectedCoords.y < 0.0 || projectedCoords.y > 1.0
    ) {
        return 0.0;
    }

    float currentDepth = projectedCoords.z;
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.0005);

    float shadow = 0.0;
    float samples = 0.0;
    float pcfRadius = 1.5;
    vec2 texelSize = pcfRadius / vec2(textureSize(uDirectionalShadowMaps[lightIndex], 0));

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 sampleCoord = projectedCoords.xy + vec2(x, y) * texelSize;

            if (
                sampleCoord.x < 0.0 || sampleCoord.x > 1.0 ||
                sampleCoord.y < 0.0 || sampleCoord.y > 1.0
            ) {
                continue;
            }

            float closestDepth = texture(uDirectionalShadowMaps[lightIndex], sampleCoord).r;
            shadow += currentDepth - bias > closestDepth ? 1.0 : 0.0;
            samples += 1.0;
        }
    }

    if (samples == 0.0) {
        return 0.0;
    }

    return shadow / samples;
}

float calculateSpotShadow(int lightIndex, vec3 worldPos, vec3 normal, vec3 lightDir) {
    if (uSpotShadowEnabled[lightIndex] == 0) {
        return 0.0;
    }

    vec4 lightSpacePos = uSpotLightSpaceMatrices[lightIndex] * vec4(worldPos, 1.0);

    vec3 projectedCoords = lightSpacePos.xyz / lightSpacePos.w;
    projectedCoords = projectedCoords * 0.5 + 0.5;

    if (projectedCoords.z > 1.0) {
        return 0.0;
    }

    if (
        projectedCoords.x < 0.0 || projectedCoords.x > 1.0 ||
        projectedCoords.y < 0.0 || projectedCoords.y > 1.0
    ) {
        return 0.0;
    }

    float currentDepth = projectedCoords.z;
    float bias = max(0.005 * (1.0 - dot(normal, lightDir)), 0.0005);

    float shadow = 0.0;
    float samples = 0.0;
    float pcfRadius = 1.5;
    vec2 texelSize = pcfRadius / vec2(textureSize(uSpotShadowMaps[lightIndex], 0));

    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 sampleCoord = projectedCoords.xy + vec2(x, y) * texelSize;

            if (
                sampleCoord.x < 0.0 || sampleCoord.x > 1.0 ||
                sampleCoord.y < 0.0 || sampleCoord.y > 1.0
            ) {
                continue;
            }

            float closestDepth = texture(uSpotShadowMaps[lightIndex], sampleCoord).r;
            shadow += currentDepth - bias > closestDepth ? 1.0 : 0.0;
            samples += 1.0;
        }
    }

    if (samples == 0.0) {
        return 0.0;
    }

    return shadow / samples;
}

vec3 calculateDirectionalLight(int lightIndex, DirectionalLight light, vec3 normal, vec3 viewDir) {
    vec3 lightDir = normalize(-light.direction);

    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * light.color * light.intensity * uObjectColor;

    vec3 specular = vec3(0.0);

    if (diff > 0.0) {
        vec3 reflectDir = reflect(-lightDir, normal);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), uShininess);
        specular = uSpecularStrength * spec * light.color * light.intensity;
    }

    float shadow = calculateDirectionalShadow(lightIndex, vWorldPos, normal, lightDir);

    return (1.0 - shadow) * (diffuse + specular);
}

vec3 calculateSpotLight(int lightIndex, SpotLight light, vec3 normal, vec3 viewDir) {
    vec3 lightToFragment = normalize(vWorldPos - light.position);
    vec3 spotlightDirection = normalize(light.direction);

    float theta = dot(lightToFragment, spotlightDirection);

    float epsilon = max(light.innerCutoffCos - light.outerCutoffCos, 0.0005);
    float coneIntensity = clamp((theta - light.outerCutoffCos) / epsilon, 0.0, 1.0);

    vec3 lightDir = normalize(light.position - vWorldPos);

    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * light.color * light.intensity * uObjectColor;

    vec3 specular = vec3(0.0);

    if (diff > 0.0) {
        vec3 reflectDir = reflect(-lightDir, normal);
        float spec = pow(max(dot(viewDir, reflectDir), 0.0), uShininess);
        specular = uSpecularStrength * spec * light.color * light.intensity;
    }

    float distanceToLight = length(light.position - vWorldPos);
    float attenuation = 1.0 / (
        light.constant +
        light.linear * distanceToLight +
        light.quadratic * distanceToLight * distanceToLight
    );

    float shadow = calculateSpotShadow(lightIndex, vWorldPos, normal, lightDir);

    return (1.0 - shadow) * (diffuse + specular) * attenuation * coneIntensity;
}

void main() {
    vec3 normal = normalize(vNormal);
    vec3 viewDir = normalize(uViewPosition - vWorldPos);

    vec3 result = uAmbientStrength * uObjectColor;

    for (int i = 0; i < uDirectionalLightCount; i++) {
        result += calculateDirectionalLight(i, uDirectionalLights[i], normal, viewDir);
    }

    for (int i = 0; i < uSpotLightCount; i++) {
        result += calculateSpotLight(i, uSpotLights[i], normal, viewDir);
    }

    FragColor = vec4(result, 1.0);
}
