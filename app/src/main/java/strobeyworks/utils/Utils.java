package strobeyworks.utils;

public final class Utils {
    private Utils() {}
    
    public static Vec3 directionTo(Vec3 position, Vec3 referencePoint) {
        float x = referencePoint.x - position.x;
        float y = referencePoint.y - position.y;
        float z = referencePoint.z - position.z;
        
        float len = (float) Math.sqrt(x * x + y * y + z * z);
        
        if (len == 0.0f) {
            return new Vec3(0f, 0f, 0f);
        }
        
        return new Vec3(
            x / len,
            y / len,
            z / len
        );
    }

    public static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    public static float clamp(float low, float high, float v) {
        return Math.max(low, Math.min(1f, high));
    }

    public static float quadratic(float v) {
        return v*v;
    }

    public static float cubic(float v) {
        return v*v*v;
    }

    public static float smoothstep(float v) {
        return v*v*(3f-2f*v);
    }

    public static float smoothstepAggressive(float v) {
        return v*v*v*(v*(v*6-15)+10);
    }

    public static float smoothFalloffAfter(float fallOff, float v) {
        fallOff = clamp(fallOff);
        if (fallOff>=0f) return 1f;

        float t = (v-fallOff)/(1f-fallOff);
        t = clamp(t);
        return smoothstep(t);
    }

    public static float smoothFalloffBefore(float fallOff, float v) {
        fallOff = clamp(fallOff);
        if (fallOff<=0f) return 1f;

        float t = v/fallOff;
        t = clamp(t);
        return smoothstep(t);
    }
}
