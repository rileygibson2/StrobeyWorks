package strobeyworks.ui.style;

import java.util.HashMap;
import java.util.Map;

import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIElement.UIAlignContent;
import strobeyworks.ui.primitives.UIElement.UIAlignItems;
import strobeyworks.ui.primitives.UIElement.UIBoxMode;
import strobeyworks.ui.primitives.UIElement.UIFlowDirection;
import strobeyworks.ui.primitives.UIElement.UIJustifyContent;
import strobeyworks.ui.primitives.UIElement.UIOverflowMode;
import strobeyworks.ui.primitives.UIElement.UIPositionMode;
import strobeyworks.utils.Vec4;

public final class StyleProps {
        
        private StyleProps() {}

        private static final Map<String, UIStyleProperty<?>> PROPERTIES = new HashMap<>();
        
        public static final UIStyleProperty<UIBoxMode> BOX = 
        register(new UIStyleProperty<>("box", UIBoxMode.class));

        public static final UIStyleProperty<UIFlowDirection> FLOW_DIRECTION = 
        register(new UIStyleProperty<>("flow-direction", UIFlowDirection.class));

        public static final UIStyleProperty<Boolean> FLOW_WRAP = 
        register(new UIStyleProperty<>("flow-wrap", Boolean.class));

        public static final UIStyleProperty<UIPositionMode> POSITION = 
        register(new UIStyleProperty<>("position", UIPositionMode.class));

        public static final UIStyleProperty<UIJustifyContent> JUSTIFY_CONTENT = 
        register(new UIStyleProperty<>("justify-content", UIJustifyContent.class));

        public static final UIStyleProperty<UIAlignItems> ALIGN_ITEMS = 
        register(new UIStyleProperty<>("align-items", UIAlignItems.class));

        public static final UIStyleProperty<UIAlignContent> ALIGN_CONTENT = 
        register(new UIStyleProperty<>("align-content", UIAlignContent.class));

        public static final UIStyleProperty<UIOverflowMode> OVERFLOW_X = 
        register(new UIStyleProperty<>("overflow-x", UIOverflowMode.class));

        public static final UIStyleProperty<UIOverflowMode> OVERFLOW_Y = 
        register(new UIStyleProperty<>("overflow-y", UIOverflowMode.class));


        public static final UIStyleProperty<UILength> WIDTH = 
        register(new UIStyleProperty<>("width", UILength.class));

        public static final UIStyleProperty<UILength> HEIGHT = 
        register(new UIStyleProperty<>("height", UILength.class));

        public static final UIStyleProperty<UILength> MAX_WIDTH = 
        register(new UIStyleProperty<>("max-width", UILength.class));

        public static final UIStyleProperty<UILength> MAX_HEIGHT = 
        register(new UIStyleProperty<>("max-height", UILength.class));

        public static final UIStyleProperty<UILength> MIN_WIDTH = 
        register(new UIStyleProperty<>("min-width", UILength.class));

        public static final UIStyleProperty<UILength> MIN_HEIGHT = 
        register(new UIStyleProperty<>("min-height", UILength.class));

        public static final UIStyleProperty<UILength> MARGIN_LEFT = 
        register(new UIStyleProperty<>("margin-left", UILength.class));

        public static final UIStyleProperty<UILength> MARGIN_RIGHT = 
        register(new UIStyleProperty<>("margin-right", UILength.class));

        public static final UIStyleProperty<UILength> MARGIN_TOP = 
        register(new UIStyleProperty<>("margin-top", UILength.class));

        public static final UIStyleProperty<UILength> MARGIN_BOTTOM = 
        register(new UIStyleProperty<>("margin-bottom", UILength.class));

        public static final UIStyleProperty<UILength> PADDING_LEFT = 
        register(new UIStyleProperty<>("padding-left", UILength.class));

        public static final UIStyleProperty<UILength> PADDING_RIGHT = 
        register(new UIStyleProperty<>("padding-right", UILength.class));

        public static final UIStyleProperty<UILength> PADDING_TOP = 
        register(new UIStyleProperty<>("padding-top", UILength.class));

        public static final UIStyleProperty<UILength> PADDING_BOTTOM = 
        register(new UIStyleProperty<>("padding-bottom", UILength.class));

        public static final UIStyleProperty<UILength> OFFSET_LEFT = 
        register(new UIStyleProperty<>("offset-left", UILength.class));

        public static final UIStyleProperty<UILength> OFFSET_RIGHT = 
        register(new UIStyleProperty<>("offset-right", UILength.class));

        public static final UIStyleProperty<UILength> OFFSET_TOP = 
        register(new UIStyleProperty<>("offset-top", UILength.class));

        public static final UIStyleProperty<UILength> OFFSET_BOTTOM = 
        register(new UIStyleProperty<>("offset-bottom", UILength.class));


        public static final UIStyleProperty<Float> OPACITY = 
        register(new UIStyleProperty<>("opacity", Float.class));

        public static final UIStyleProperty<Boolean> VISIBLE = 
        register(new UIStyleProperty<>("visible", Boolean.class));

        
        public static final UIStyleProperty<Boolean> BORDER_ENABLED = 
        register(new UIStyleProperty<>("border-enabled", Boolean.class));
        
        public static final UIStyleProperty<UILength> BORDER_THICKNESS = 
        register(new UIStyleProperty<>("border-thickness", UILength.class));
        
        public static final UIStyleProperty<Vec4> BORDER_COLOR = 
        register(new UIStyleProperty<>("border-color", Vec4.class));
        
        public static final UIStyleProperty<Boolean> BORDER_LEFT = 
        register(new UIStyleProperty<>("border-left", Boolean.class));
        
        public static final UIStyleProperty<Boolean> BORDER_RIGHT = 
        register(new UIStyleProperty<>("border-right", Boolean.class));
        
        public static final UIStyleProperty<Boolean> BORDER_TOP = 
        register(new UIStyleProperty<>("border-top", Boolean.class));
        
        public static final UIStyleProperty<Boolean> BORDER_BOTTOM = 
        register(new UIStyleProperty<>("border-bottom", Boolean.class));


        public static final UIStyleProperty<Vec4> COLOR = 
        register(new UIStyleProperty<>("color", Vec4.class));
        
        public static final UIStyleProperty<Vec4> CORNER_RADIUS = 
        register(new UIStyleProperty<>("corner-radius", Vec4.class));
        
        public static final UIStyleProperty<Boolean> OVAL = 
        register(new UIStyleProperty<>("oval", Boolean.class));

        public static final UIStyleProperty<Vec4> TINT = 
        register(new UIStyleProperty<>("tint", Vec4.class));
        
        public static final UIStyleProperty<UIFont> FONT = 
        register(new UIStyleProperty<>("font", UIFont.class));
        
        public static final UIStyleProperty<Vec4> ICON_TINT = 
        register(new UIStyleProperty<>("icon-tint", Vec4.class));
        
        //Transform
        public static final UIStyleProperty<Float> TRANSFORM_SCALEX = 
        register(new UIStyleProperty<>("transform-scale-x", Float.class));
        
        public static final UIStyleProperty<Float> TRANSFORM_SCALEY = 
        register(new UIStyleProperty<>("transform-scale-y", Float.class));
        
        // Transition
        public static final UIStyleProperty<Float> TRANSITION_DURATION = 
        register(new UIStyleProperty<>("transition-duration", Float.class));
        
        private static <T> UIStyleProperty<T> register(UIStyleProperty<T> property) {
                PROPERTIES.put(property.getName(), property);
                return property;
        }

        public static UIStyleProperty<?> getProperty(String name) {
                return PROPERTIES.get(name);
        }
}
