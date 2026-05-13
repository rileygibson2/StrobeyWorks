package strobeyworks.ui.style;

import strobeyworks.ui.core.UIFont;
import strobeyworks.utils.Vec4;

public final class PrimitiveStyles extends UIStyle {

        public static final UIStyleProperty<Vec4> COLOR = new UIStyleProperty<>("color", Vec4.class);
        public static final UIStyleProperty<Vec4> CORNER_RADIUS = new UIStyleProperty<>("corner-radius", Vec4.class);
        public static final UIStyleProperty<Boolean> BORDER_ENABLED = new UIStyleProperty<>("border-enabled", Boolean.class);
        public static final UIStyleProperty<Float> BORDER_THICKNESS = new UIStyleProperty<>("border-thickness", Float.class);
        public static final UIStyleProperty<Vec4> BORDER_COLOR = new UIStyleProperty<>("border-color", Vec4.class);
        public static final UIStyleProperty<Boolean> BORDER_LEFT = new UIStyleProperty<>("border-left", Boolean.class);
        public static final UIStyleProperty<Boolean> BORDER_RIGHT = new UIStyleProperty<>("border-right", Boolean.class);
        public static final UIStyleProperty<Boolean> BORDER_TOP = new UIStyleProperty<>("border-top", Boolean.class);
        public static final UIStyleProperty<Boolean> BORDER_BOTTOM = new UIStyleProperty<>("border-bottom", Boolean.class);

        public static final UIStyleProperty<Vec4> TINT = new UIStyleProperty<>("tint", Vec4.class);
        public static final UIStyleProperty<UIFont> FONT = new UIStyleProperty<>("font", UIFont.class);

        public static final UIStyleProperty<Vec4> ICON_TINT = new UIStyleProperty<>("icon-tint", Vec4.class);

        //Transform
        public static final UIStyleProperty<Float> TRANSFORM_SCALEX = new UIStyleProperty<>("transform-scale-x", Float.class);
        public static final UIStyleProperty<Float> TRANSFORM_SCALEY = new UIStyleProperty<>("transform-scale-y", Float.class);

        // Transition
        public static final UIStyleProperty<Float> TRANSITION_DURATION = new UIStyleProperty<>("transition-duration", Float.class);

}
