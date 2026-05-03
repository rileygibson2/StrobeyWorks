package strobeyworks.ui.primitives;

public class UIPair {
    public enum UIUnit {
        PIXELS,
        PARENT_CONTENT_WIDTH,
        PARENT_CONTENT_HEIGHT,
        PARENT_BOX_WIDTH,
        PARENT_BOX_HEIGHT,
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

    public static UIPair px(float v) {
        return new UIPair(v, UIUnit.PIXELS);
    }

    public static UIPair pcw(float v) {
        return new UIPair(v, UIUnit.PARENT_CONTENT_WIDTH);
    }

    public static UIPair pch(float v) {
        return new UIPair(v, UIUnit.PARENT_CONTENT_HEIGHT);
    }

    public static UIPair pbw(float v) {
        return new UIPair(v, UIUnit.PARENT_BOX_WIDTH);
    }

    public static UIPair pbh(float v) {
        return new UIPair(v, UIUnit.PARENT_BOX_HEIGHT);
    }

    public static UIPair sw(float v) {
        return new UIPair(v, UIUnit.SCREEN_WIDTH);
    }

    public static UIPair sh(float v) {
        return new UIPair(v, UIUnit.SCREEN_HEIGHT);
    }

    public UIPair clone() {return new UIPair(value, unit);}
}
