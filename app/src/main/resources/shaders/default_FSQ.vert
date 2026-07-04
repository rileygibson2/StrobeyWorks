#version 330 core

layout (location = 0) in vec3 aPos;

out vec2 vUV;

// Default for -0.5 to 0.5 full screen quad passing screen position and uv coords

void main() {
    vec2 screenPos = aPos.xy * 2.0;
    vUV = aPos.xy + 0.5;
    
    gl_Position = vec4(screenPos, 0.0, 1.0);
}