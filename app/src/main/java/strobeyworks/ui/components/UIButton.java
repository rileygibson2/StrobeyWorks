package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;

public class UIButton extends UIRectangle {
    
    @FunctionalInterface
    public interface UIButtonCallback {
        public void implement();
    }
    
    private UIIcon icon;
    
    private UIButtonCallback callback;
    
    public UIButton() {
        
        clickable(true);
        hoverable(true);

        style("transition-duration", 0.3f);
        style("box", UIBoxMode.FIXED);
        style("color", col(UIColors.TRANSPARENT));
        style("border-enabled", true);
        style("border-color", col(UIColors.GREEN));
        style("corner-radius", 10f);
        
        icon = new UIIcon();
        icon.style("width", pcw(1.0f))
        .style("height", pch(1.0f))
        .style("tint", col(UIColors.GREEN))
        .style("visible", false);
        addChild(icon);
    }
    
    @Override
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        if (property==StyleProps.ICON_TINT) icon.style(property, value);
        if (property==StyleProps.TRANSFORM_SCALEX) icon.style(property, value);
        if (property==StyleProps.TRANSFORM_SCALEY) icon.style(property, value);
        super.applyStyleProperty(property, value);
    }
    
    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        style.set(StyleProps.ICON_TINT, icon.getTint());
        return style;
    }
    
    public UIButton icon(String iconName) {
        icon.texture(iconName);
        icon.style("visible", true);
        return this;
    }
    
    public UIButton clickedAction(UIButtonCallback callback) {
        this.callback = callback;
        return this;
    }
    
    @Override
    public void clicked(IOEvent event) {
        if (callback!=null) callback.implement();
    }
}
