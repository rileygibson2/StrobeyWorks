#version 330 core

in vec2 vUV;

uniform sampler2D uDepositTexture;
uniform float uOpacityCuttoff;

out vec4 FragColor;

vec3 colBase = vec3(1.0, 1.0, 1.0);
vec3 colA = vec3(1.0, 1.0, 1.0);
vec3 colB = vec3(0.0, 0.0, 0.2);

void main() {
    float pheromone = texture(uDepositTexture, vUV).r;

    if (pheromone <= uOpacityCuttoff) {
        FragColor = vec4(colBase, 0.0);
        return;
    }

    FragColor = vec4(mix(colA, colB, pheromone), 1.0);

    //FragColor = vec4(vec3(pheromone*0.0, pheromone*0.5, pheromone*1.0), pheromone);
}