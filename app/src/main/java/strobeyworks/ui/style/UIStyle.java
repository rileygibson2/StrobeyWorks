package strobeyworks.ui.style;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.style.values.UIColor;

public abstract class UIStyle {
    
    public enum UIRectangleStyleProperty {
        COLOR(UIColor.class),
        CORNER_RADIUS(UIPair.class),
        BORDER_ENABLED(Boolean.class),
        BORDER_COLOR(UIColor.class),
        BORDER_THICKNESS(UIPair.class),
        BORDER_RIGHT(UIPair.class),
        BORDER_LEFT(UIPair.class),
        BORDER_TOP(UIPair.class),
        BORDER_BOTTOM(UIPair.class);

        private final Class<?> c;
        
        UIRectangleStyleProperty(Class<?> c) {
            this.c = c;
        }
        
        public Class<?> getType() {
            return c;
        }
    }
    
    Set<UIRectangleStyleProperty> validProperties;
    Map<UIRectangleStyleProperty, Object> properties;

    public UIStyle() {
        validProperties = new HashSet<>();
        properties = new HashMap<>();
    }

    protected void setProperty(UIRectangleStyleProperty property, Object value) {
        Class<?> t = property.getType();
        if (validProperties.contains(property)&&t.isInstance(value)) properties.put(property, value);
    }

    protected Object getProperty(UIRectangleStyleProperty property) {
        return properties.get(property);
    }
}
