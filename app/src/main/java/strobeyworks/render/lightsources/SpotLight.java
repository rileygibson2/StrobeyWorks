package strobeyworks.render.lightsources;

import org.joml.Matrix4f;

import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec3;

public class SpotLight extends LightSource {
    
    private Vec3 position;
    private Vec3 direction;
    
    private float innerCutoffCos;
    private float outerCutoffCos;
    private float soften;
    
    private float constant;
    private float linear;
    private float quadratic;
    
    private float outerCutoffDegrees;
    private float shadowNear = 0.1f;
    private float shadowFar = 50f;
    
    public SpotLight(
        Vec3 position,
        Vec3 direction,
        float widthDegrees,
        Vec3 color,
        float intensity
    ) {
        super(color, intensity);
        
        this.position = position != null ? position : new Vec3(0f, 5f, 0f);
        this.direction = direction != null ? direction : new Vec3(0f, -1f, 0f);
        this.soften = 0f;
        setWidth(widthDegrees);
        
        this.constant = 1.0f;
        this.linear = 0.09f; //0.09
        this.quadratic = 0.032f; //0.032
    }
    
    public void setPosition(Vec3 position) {
        this.position = position;
        calculateLightSpaceMatrix();
        getIndicator().setPosition(position);
    }
    
    public void setDirection(Vec3 direction) {
        this.direction = direction;
        calculateLightSpaceMatrix();
    }
    
    public void setWidth(float widthDegrees) {
        setOuterCutoffDegrees(widthDegrees);
        updateInnerCutoff();
    }
    
    public void setSharpEdge() {softenEdge(0f);}
    
    public void softenEdge(float soften) {
        this.soften = Math.max(Math.min(soften, 1f), 0f);
        updateInnerCutoff();
    }
    
    private void updateInnerCutoff() {
        float sharpness = 1f-soften;
        innerCutoffCos = 1.0f+(outerCutoffCos-1.0f)*sharpness;
        
        float minGap = 0.0005f;
        if (innerCutoffCos-outerCutoffCos<minGap) innerCutoffCos = outerCutoffCos+minGap;
        if (innerCutoffCos>1.0f) innerCutoffCos = 1.0f;
    }
    
    private void setOuterCutoffDegrees(float outerCutoffDegrees) {
        this.outerCutoffDegrees = outerCutoffDegrees;
        this.outerCutoffCos = (float) Math.cos(Math.toRadians(outerCutoffDegrees));
        calculateLightSpaceMatrix();
        
    }
    
    public void setThrowDistance(float distance) {
        float targetBrightness = 0.05f; // 5% brightness at max throw
        float linear = 1.0f / distance;
        //0.1 bright 0.05 normal 0.01 dark at edge
        
        float quadratic = ((1.0f / targetBrightness) - 1.0f - linear * distance) / (distance * distance);
        
        setAttenuation(1.0f, linear, quadratic);
    }
    
    
    public void setAttenuation(float constant, float linear, float quadratic) {
        this.constant = constant;
        this.linear = linear;
        this.quadratic = quadratic;
    }
    
    public void pointAt(Vec3 reference) {
        setDirection(Utils.directionTo(this.position, reference));
    }
    
    protected void calculateLightSpaceMatrix() {
        Vec3 dir = getDirection().normalize();
        
        Vec3 target = new Vec3(
            position.x + dir.x,
            position.y + dir.y,
            position.z + dir.z
        );
        
        Vec3 up = getStableUp(dir);
        
        Matrix4f lightProjection = new Matrix4f().perspective(
            (float) Math.toRadians(outerCutoffDegrees * 2.0f),
            1.0f,
            shadowNear,
            shadowFar
        );
        
        Matrix4f lightView = new Matrix4f().lookAt(
            position.x, position.y, position.z,
            target.x, target.y, target.z,
            up.x, up.y, up.z
        );
        
        this.lightSpaceMatrix = new Matrix4f();
        lightProjection.mul(lightView, this.lightSpaceMatrix);
    }
    
    
    private Vec3 getStableUp(Vec3 dir) {
        if (Math.abs(dir.y) > 0.99f) {
            return new Vec3(0f, 0f, 1f);
        }
        
        return new Vec3(0f, 1f, 0f);
    }
    
    
    
    public Vec3 getPosition() {return position;}
    public Vec3 getDirection() {return direction;}
    
    public float getInnerCutoffCos() {return innerCutoffCos;}
    public float getOuterCutoffCos() {return outerCutoffCos;}
    
    public float getConstant() {return constant;}
    public float getLinear() {return linear;}
    public float getQuadratic() {return quadratic;}
}
