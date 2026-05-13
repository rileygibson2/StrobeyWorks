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

    public Vec4 lerp(Vec4 to, float i) {
        float rN = r+(to.r-r)*i;
        float gN = g+(to.g-g)*i;
        float bN = b+(to.b-b)*i;
        float aN = a+(to.a-a)*i;
        return new Vec4(rN, gN, bN, aN);
    }

    public Vec4 clone() {return new Vec4(this.r, this.g, this.b, this.a);}
}
