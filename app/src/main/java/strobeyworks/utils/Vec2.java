package strobeyworks.utils;

public class Vec2 {
    public float x, y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(float a) {
        this.x = a;
        this.y = a;
    }

    public static Vec2 of(Vec2 v) {
        return new Vec2(v.x, v.y);
    }

    public String toString() {
        return ("(Vec2 x="+x+" y="+y+")");
    }
}
