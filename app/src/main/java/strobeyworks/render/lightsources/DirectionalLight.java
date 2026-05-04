package strobeyworks.render.lightsources;

import org.joml.Matrix4f;

import strobeyworks.utils.Vec3;

public class DirectionalLight extends LightSource {
    
    private Vec3 direction;
    
    public DirectionalLight(Vec3 direction, Vec3 color, float intensity) {
        super(color, intensity);
        setDirection(direction!=null ? direction : new Vec3(0f, 1f, 0f));
    }
    
    protected void calculateLightSpaceMatrix() {
        Vec3 sceneCenter = new Vec3(0f, 1f, 0f);
        float lightDistance = 20f;
        
        Vec3 dir = getDirection().normalize();
        Vec3 lightPos = new Vec3(
            sceneCenter.x - dir.x * lightDistance,
            sceneCenter.y - dir.y * lightDistance,
            sceneCenter.z - dir.z * lightDistance
        );
        
        Matrix4f lightProjection = new Matrix4f().ortho(
            -20f, 20f,
            -20f, 20f,
            1f, 60f
        );
        
        Matrix4f lightView = new Matrix4f().lookAt(
            lightPos.x, lightPos.y, lightPos.z,
            sceneCenter.x, sceneCenter.y, sceneCenter.z,
            0f, 1f, 0f
        );
        
        this.lightSpaceMatrix = new Matrix4f();
        lightProjection.mul(lightView, this.lightSpaceMatrix);
    }
    
    public void setDirection(Vec3 direction) {
        this.direction = direction;
        calculateLightSpaceMatrix();
    }
    
    public Vec3 getDirection() {return direction;}

}
