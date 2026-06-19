#version 330 core

uniform float uPheramoneContribution;

out float FragPheromone;

void main() {
    FragPheromone = uPheramoneContribution;
}