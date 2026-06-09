#version 330 core

layout (location = 0) in vec3 aAgent;

uniform sampler2D uDepositTexture;

uniform float uDeltaTime;
uniform float uSpeed;
uniform float uSensorDistance;
uniform float uSensorAngle;
uniform float uTurnSpeed;

uniform float uRandomTurnStrength;
uniform float uRandomSpeedStrength;
uniform float uTime;

out vec3 oAgent;

float hash(float n) {
    return fract(sin(n) * 43758.5453123);
}

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float sense(vec2 position, float headingOffset) {
    float sensorHeading = aAgent.z + headingOffset;

    vec2 sensorDirection = vec2(
        cos(sensorHeading),
        sin(sensorHeading)
    );

    vec2 sensorPosition = position + sensorDirection * uSensorDistance;

    if (any(lessThan(sensorPosition, vec2(0.0))) ||
        any(greaterThan(sensorPosition, vec2(1.0)))) {
        return 0.0;
    }

    return texture(uDepositTexture, sensorPosition).r;
}

void main() {
    vec2 position = aAgent.xy;
    float heading = aAgent.z;

    // Sense
    float forward = sense(position, 0.0);
    float left = sense(position, uSensorAngle);
    float right = sense(position, -uSensorAngle);

    if (forward >= left && forward >= right) {
        // Continue forwards.
    } else if (left > right) {
        heading += uTurnSpeed * uDeltaTime;
    } else if (right > left) {
        heading -= uTurnSpeed * uDeltaTime;
    }

    // Randomise heading
    float frameSeed = floor(uTime * 60.0);
    float randomSigned = hash12(vec2(float(gl_VertexID), frameSeed)) * 2.0 - 1.0;
    heading += randomSigned * uRandomTurnStrength * uDeltaTime;

    // Randomise speed
    float randomSpeed = hash12(vec2(float(gl_VertexID) + 123.45, frameSeed + 67.89));
    float speedVariation = mix(0, 2, randomSpeed);
    float speedMultiplier = mix(1.0, speedVariation, uRandomSpeedStrength);
    float agentSpeed = uSpeed * speedMultiplier;

    // Apply movment
    vec2 direction = vec2(cos(heading), sin(heading));
    position += direction * agentSpeed * uDeltaTime;
    

    // Collision
    if (position.x < 0.0 || position.x > 1.0) {
        heading = 3.14159265 - heading;
    }
    if (position.y < 0.0 || position.y > 1.0) {
        heading = -heading;
    }
    position = clamp(position, vec2(0.0), vec2(1.0));

    oAgent = vec3(position, heading);
}