#version 330 core

layout (location = 0) in vec3 aAgent;

uniform sampler2D uDepositTexture;

uniform float uDeltaTime;
uniform float uSpeed;
uniform float uSensorDistance;
uniform float uSensorAngle;
uniform float uTurnSpeed;

out vec3 oAgent;

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

    vec2 direction = vec2(cos(heading), sin(heading));
    position += direction * uSpeed * uDeltaTime;
    
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