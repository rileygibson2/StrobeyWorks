package strobeyworks.ui;

import strobeyworks.utils.Vec3;

public class UIColors {

    public static final float[] BLACK = {0f, 0f, 0f};
    public static final float[] WHITE = {1f, 1f, 1f};
    public static final float[] RED = {1f, 0f, 0f};
    public static final float[] GREEN = {0f, 1f, 0f};
    public static final float[] LIGHT_GREEN = {0f, 1f, 0.2f};


    private UIColors() {}

    public static Vec3 color(float[] color) {
        return new Vec3(color[0], color[1], color[2]);
    }
}
