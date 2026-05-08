package strobeyworks.ui.core;

import strobeyworks.utils.Vec4;

public final class UIColors {
    
    public static final Vec4 TRANSPARENT = new Vec4(0f, 0f, 0f, 0f);
    public static final Vec4 BLACK = new Vec4(0f, 0f, 0f, 1f);
    public static final Vec4 WHITE = new Vec4(1f, 1f, 1f, 1f);
    
    
    public static final Vec4 GRAY_008 = new Vec4(0.08f, 0.08f, 0.08f, 1f);
    public static final Vec4 GRAY_01 = new Vec4(0.1f, 0.1f, 0.1f, 1f);
    public static final Vec4 GRAY_02 = new Vec4(0.2f, 0.2f, 0.2f, 1f);
    public static final Vec4 GRAY_03 = new Vec4(0.3f, 0.3f, 0.3f, 1f);
    public static final Vec4 GRAY_04 = new Vec4(0.4f, 0.4f, 0.4f, 1f);
    public static final Vec4 GRAY_05 = new Vec4(0.5f, 0.5f, 0.5f, 1f);
    
    
    public static final Vec4 RED = new Vec4(1f, 0f, 0f, 1f);
    public static final Vec4 GREEN = new Vec4(0f, 1f, 0f, 1f);
    public static final Vec4 LIGHT_GREEN = new Vec4(0f, 1f, 0.2f, 1f);
    public static final Vec4 BLUE = new Vec4(0f, 0, 1f, 1f);
    public static final Vec4 PURPLE = new Vec4(0.5f, 0, 1f, 1f);

    public static final Vec4 BG_GREEN = new Vec4(0f, 0.2f, 0.08f, 1f);
    
    public static Vec4 col(Vec4 color) {
        return color.clone();
    }

    public static Vec4 colWithAlpha(Vec4 color, float a) {
        Vec4 c = color.clone();
        c.a = a;
        return c;
    }
}
