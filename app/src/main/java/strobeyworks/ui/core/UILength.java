package strobeyworks.ui.core;

public class UILength {
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

    private UILength(float value, UIUnit unit) {
        this.value = value;
        this.unit = unit;
    }

    public static UILength px(int v) {
        return new UILength(v, UIUnit.PIXELS);
    }

    public static UILength px(float v) {
        return new UILength(v, UIUnit.PIXELS);
    }

    public static UILength pcw(float v) {
        return new UILength(v, UIUnit.PARENT_CONTENT_WIDTH);
    }

    public static UILength pch(float v) {
        return new UILength(v, UIUnit.PARENT_CONTENT_HEIGHT);
    }

    public static UILength pbw(float v) {
        return new UILength(v, UIUnit.PARENT_BOX_WIDTH);
    }

    public static UILength pbh(float v) {
        return new UILength(v, UIUnit.PARENT_BOX_HEIGHT);
    }

    public static UILength sw(float v) {
        return new UILength(v, UIUnit.SCREEN_WIDTH);
    }

    public static UILength sh(float v) {
        return new UILength(v, UIUnit.SCREEN_HEIGHT);
    }

    public UILength clone() {return new UILength(value, unit);}
}
