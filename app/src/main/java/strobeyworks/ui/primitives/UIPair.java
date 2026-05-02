package strobeyworks.ui.primitives;

public class UIPair {
    public enum UIUnit {
        PIXELS,
        PARENT_WIDTH,
        PARENT_HEIGHT,
        SCREEN_WIDTH,
        SCREEN_HEIGHT
    }

    public float value;
    public UIUnit unit;

    private UIPair(float value, UIUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static UIPair px(int v) {
        return new UIPair(v, UIUnit.PIXELS);
    }

    public static UIPair pw(float v) {
        return new UIPair(v, UIUnit.PARENT_WIDTH);
    }

    public static UIPair ph(float v) {
        return new UIPair(v, UIUnit.PARENT_HEIGHT);
    }

    public static UIPair sw(float v) {
        return new UIPair(v, UIUnit.SCREEN_WIDTH);
    }

    public static UIPair sh(float v) {
        return new UIPair(v, UIUnit.SCREEN_HEIGHT);
    }

    public UIPair clone() {return new UIPair(value, unit);}
}
