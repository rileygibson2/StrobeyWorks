#version 330 core

in vec2 vUV;

uniform sampler2D uDepositTexture;

out vec4 FragColor;

void main() {
    float pheromone = texture(uDepositTexture, vUV).r;
    FragColor = vec4(vec3(pheromone*0.0, pheromone*0.5, pheromone*1.0), 1.0);
}