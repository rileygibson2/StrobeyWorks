package strobeyworks.utils;

public class Vec2I {
    public int x, y;

    public Vec2I(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vec2I(int a) {
        this.x = a;
        this.y = a;
    }

    public static Vec2I of(Vec2I v) {
        return new Vec2I(v.x, v.y);
    }

    public String toString() {
        return ("(Vec2I x="+x+" y="+y+")");
    }
}
