package strobeyworks.ui;

import strobeyworks.utils.Vec4;

public class UIColors {

    public static final float[] TRANSPARENT = {0f, 0f, 0f, 0f};
    public static final float[] BLACK = {0f, 0f, 0f, 1f};
    public static final float[] WHITE = {1f, 1f, 1f, 1f};

    public static final float[] GRAY_01 = {0.1f, 0.1f, 0.1f, 1f};
    public static final float[] GRAY_02 = {0.2f, 0.2f, 0.2f, 1f};
    public static final float[] GRAY_03 = {0.3f, 0.3f, 0.3f, 1f};
    public static final float[] GRAY_04 = {0.4f, 0.4f, 0.4f, 1f};
    public static final float[] GRAY_05 = {0.5f, 0.5f, 0.5f, 1f};


    public static final float[] RED = {1f, 0f, 0f, 1f};
    public static final float[] GREEN = {0f, 1f, 0f, 1f};
    public static final float[] LIGHT_GREEN = {0f, 1f, 0.2f, 1f};


    public static final float[] BG_GREEN = {0f, 0.2f, 0.08f, 1f};


    private UIColors() {}

    public static Vec4 color(float[] color) {
        return new Vec4(color[0], color[1], color[2], color[3]);
    }
}
