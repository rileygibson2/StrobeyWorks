package strobeyworks.ui.primitives;

public class UIPair {
    public enum Unit {
        PIXELS,
        PARENT_WIDTH,
        PARENT_HEIGHT,
        SCREEN_WIDTH,
        SCREEN_HEIGHT
    }

    private final float value;
    private final Unit unit;

    private UIPair(float value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static UIPair px(int v) {
        return new UIPair(v, Unit.PIXELS);
    }

    public static UIPair pw(float v) {
        return new UIPair(v, Unit.PARENT_WIDTH);
    }

    public static UIPair ph(float v) {
        return new UIPair(v, Unit.PARENT_HEIGHT);
    }

    public static UIPair sw(float v) {
        return new UIPair(v, Unit.SCREEN_WIDTH);
    }

    public static UIPair sh(float v) {
        return new UIPair(v, Unit.SCREEN_HEIGHT);
    }

    public float getValue() {
        return value;
    }

    public Unit getUnit() {
        return unit;
    }
}
