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
}
