package strobeyworks.utils;

public class Vec4 {
    public float r, g, b, a;
    
    public Vec4(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public Vec4(float a) {
        this.r = a;
        this.g = a;
        this.b = a;
        this.a = a;
    }
}
