package strobeyworks;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import strobeyworks.render.Renderer;
import strobeyworks.utils.Vec3;

public class Camera {
    
    private Renderer renderer;
    private Vec3 position;
    private float yaw;
    private float pitch;
    private float fov;
    
    private Matrix4f viewMatrix;
    private Matrix4f projectionMatrix;
    
    public Camera(Renderer renderer) {
        this.renderer = renderer;
        position = new Vec3(0, 0, 0);
        pitch = 0f;
        yaw = -90f;
        fov = 60f;
    }

    public void init() {
        updateMatrices();
    }
    
    private void updateMatrices() {
        Vector3f eye = new Vector3f(position.x, position.y, position.z);
        Vector3f center = new Vector3f(eye).add(getFront());
        Vector3f up = new Vector3f(0, 1, 0);
        
        viewMatrix = new Matrix4f().lookAt(eye, center, up);
        
        projectionMatrix = new Matrix4f()
        .perspective(
            (float) Math.toRadians(fov),
            renderer.getParentWindow().getAspectRatio(),
            0.1f,
            100.0f
        );
    }
    
    private Vector3f getFront() {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        
        float x = (float) (Math.cos(yawRad) * Math.cos(pitchRad));
        float y = (float) Math.sin(pitchRad);
        float z = (float) (Math.sin(yawRad) * Math.cos(pitchRad));
        
        return new Vector3f(x, y, z).normalize();
    }
    
    public void setPosition(Vec3 position) {
        this.position = position;
        updateMatrices();
    }
    public void setOrientation(float yaw, float pitch, float fov) {
        this.yaw = yaw;
        this.pitch = pitch;
        this.fov = fov;
        updateMatrices();
    }
    
    public Vec3 getPosition() {return position;}
    public float getYaw() {return yaw;}
    public float getPitch() {return pitch;}
    public float getFov() {return fov;}
    public Matrix4f getViewMatrix() {return viewMatrix;}
    public Matrix4f getProjectionMatrix() {return projectionMatrix;}
    
    public void update(float dt) {
        boolean changed = false;
        
        float moveSpeed = 3.0f;
        float lookSpeed = 90.0f;
        float zoomSpeed = 40.0f;
        
        Vector3f front = getFront();
        Vector3f right = new Vector3f(front).cross(0, 1, 0).normalize();
        Vector3f up = new Vector3f(right).cross(front).normalize();
        IO io = renderer.getParentWindow().getIO();

        if (io.leftPressed) {
            float dragSpeed = 0.01f;
            
            position.x -= right.x * io.mouseDX * dragSpeed;
            position.y -= right.y * io.mouseDX * dragSpeed;
            position.z -= right.z * io.mouseDX * dragSpeed;
            
            position.x -= up.x * io.mouseDY * dragSpeed;
            position.y -= up.y * io.mouseDY * dragSpeed;
            position.z -= up.z * io.mouseDY * dragSpeed;
            
            changed = true;
        }
        
        if (io.keyDown(GLFW_KEY_W)) {
            position.x += front.x * moveSpeed * dt;
            position.y += front.y * moveSpeed * dt;
            position.z += front.z * moveSpeed * dt;
            changed = true;
        }
        if (io.keyDown(GLFW_KEY_S)) {
            position.x -= front.x * moveSpeed * dt;
            position.y -= front.y * moveSpeed * dt;
            position.z -= front.z * moveSpeed * dt;
            changed = true;
        }
        
        if (io.keyDown(GLFW_KEY_A)) {
            position.x -= right.x * moveSpeed * dt;
            position.y -= right.y * moveSpeed * dt;
            position.z -= right.z * moveSpeed * dt;
            changed = true;
        }
        if (io.keyDown(GLFW_KEY_D)) {
            position.x += right.x * moveSpeed * dt;
            position.y += right.y * moveSpeed * dt;
            position.z += right.z * moveSpeed * dt;
            changed = true;
        }
        
        if (io.keyDown(GLFW_KEY_Q)) {
            position.y -= moveSpeed * dt;
            changed = true;
        }
        if (io.keyDown(GLFW_KEY_E)) {
            position.y += moveSpeed * dt;
            changed = true;
        }
        
        float mouseSensitivity = 0.12f;
        
        if (io.rightPressed) {
            yaw += io.mouseDX * mouseSensitivity;
            pitch += io.mouseDY * mouseSensitivity;
            changed = true;
        }
        
        if (io.scrollDY != 0) {
            fov -= io.scrollDY * 3.0f;
            changed = true;
        }
        
        
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
        
        if (fov < 15.0f) fov = 15.0f;
        if (fov > 90.0f) fov = 90.0f;        
        
        if (changed) updateMatrices();
    }
    
}
