#version 330 core

in vec2 vUV;

uniform sampler2D uDepositTexture;
uniform vec2 uTexelSize;
uniform float uDeltaTime;
uniform float uDiffusion;
uniform float uDecay;

out float FragPheromone;

void main() {
    float centre = texture(uDepositTexture, vUV).r;

    float blurred = 0.0;
    for (int x = -1; x <= 1; x++) {
        for (int y = -1; y <= 1; y++) {
            vec2 offset = vec2(x, y) * uTexelSize;
            blurred += texture(uDepositTexture, vUV + offset).r;
        }
    }
    blurred /= 9.0;

    float pheromone = mix(centre, blurred, uDiffusion);
    pheromone *= exp(-uDecay * uDeltaTime);

    FragPheromone = pheromone;
}