package strobeyworks.utils;

public class Vec3 {
    public float x, y, z;
    
    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vec3(Vec2 v2, float z) {
        this.x = v2.x;
        this.y = v2.y;
        this.z = z;
    }

    public Vec3(float a) {
        this.x = a;
        this.y = a;
        this.z = a;
    }
    
    public Vec3 toRadians() {
        return new Vec3((float) Math.toRadians(x), (float) Math.toRadians(y), (float) Math.toRadians(z));
    }
    
    public Vec3 normalize() {
        float len = (float) Math.sqrt(x*x+y*y+z*z);
        if (len==0.0f) return new Vec3(0f, -1f, 0f);
        return new Vec3(x/len, y/len, z/len);
    }
}
