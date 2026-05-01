package strobeyworks.object;

import org.joml.Matrix4f;

import strobeyworks.utils.Vec3;

public class SceneObject {
    
    private Mesh mesh;
    public Vec3 position;
    private Vec3 rotation;
    private Vec3 scale;
    private Vec3 color;
    private Matrix4f modelMatrix;

    /*vec3 nColor = normalize(vNormal) * 0.5 + 0.5;
    FragColor = vec4(nColor, 1.0);*/
    
    public SceneObject(Mesh mesh) {
        this.mesh = mesh;
        this.position = new Vec3(0, 0, 0);
        this.rotation = new Vec3(0, 0, 0);
        this.scale = new Vec3(1f);
        this.color = new Vec3(1f);
        updateModelMatrix();
    }
    
    public SceneObject(Mesh mesh, Vec3 position) {
        this.mesh = mesh;
        this.position = position != null ? position : new Vec3(0, 0, 0);
        this.rotation = new Vec3(0, 0, 0);
        this.scale = new Vec3(1f);
        this.color = new Vec3(1f);
        updateModelMatrix();
    }

    public SceneObject(Mesh mesh, Vec3 position, Vec3 rotation) {
        this.mesh = mesh;
        this.position = position != null ? position : new Vec3(0, 0, 0);
        this.rotation = rotation != null ? rotation : new Vec3(0, 0, 0);
        this.scale = new Vec3(1f);
        this.color = new Vec3(1f);
        updateModelMatrix();
    }

    public SceneObject(Mesh mesh, Vec3 position, Vec3 rotation, Vec3 scale) {
        this.mesh = mesh;
        this.position = position != null ? position : new Vec3(0, 0, 0);
        this.rotation = rotation != null ? rotation : new Vec3(0, 0, 0);
        this.scale = scale != null ? scale : new Vec3(1f);
        this.color = new Vec3(1f);
        updateModelMatrix();
    }
    
    private void updateModelMatrix() {
        modelMatrix = new Matrix4f()
        .translate(position.x, position.y, position.z)
        .rotateX(rotation.x)
        .rotateY(rotation.y)
        .rotateZ(rotation.z)
        .scale(scale.x, scale.y, scale.z);
        
    }
    
    public void setPosition(Vec3 position) {
        this.position = position;
        updateModelMatrix();
    }
    public void setRotation(Vec3 rotation) {
        this.rotation = rotation;
        updateModelMatrix();
    }
    public void setScale(Vec3 scale) {
        this.scale = scale;
        updateModelMatrix();
    }
    public void setColor(Vec3 color) {this.color = color;}
    
    public Vec3 getPosition() {return position;}
    public Vec3 getRotation() {return rotation;}
    public Vec3 getScale() {return scale;}
    public Mesh getMesh() {return mesh;}
    public Vec3 getColor() {return color;}
    
    public Matrix4f getModelMatrix() {return modelMatrix;}
}
