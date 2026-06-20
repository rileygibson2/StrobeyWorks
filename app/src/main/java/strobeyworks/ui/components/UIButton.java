package strobeyworks.ui.components;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.ui.style.UIStyleProperty;
import strobeyworks.utils.Vec4;

public class UIButton extends UIRectangle {
    
    @FunctionalInterface
    public interface UIButtonCallback {
        public void implement();
    }
    
    private UIIcon icon;
    private UIText text;
    
    private UIButtonCallback callback;
    
    public UIButton(String iconName) {
        clickable(true);
        hoverable(true);
        
        style("transition-duration", 0.3f);
        style("box", UIBoxMode.FIXED);
        style("color", UIColor.rgb(0.3f));
        style("border-enabled", true);
        style("border-color", UIColor.green());
        style("corner-radius", new Vec4(10f));
        
        icon = new UIIcon();
        icon.style("width", pcw(1.0f))
        .style("height", pch(1.0f))
        .style("tint", UIColor.green())
        .style("visible", true);
        icon.texture(iconName);
        addChild(icon);
    }
    
    public UIButton(UIFont font, String t) {
        clickable(true);
        hoverable(true);
        
        style("color", UIColor.rgb(0.3f));
        style("corner-radius", new Vec4(5f));
        style("justify-content", UIJustifyContent.CENTER);
        style("align-items", UIAlignItems.CENTER);
        
        text = new UIText(font, t);
        text.style("color", UIColor.rgb(0.7f));
        addChild(text);
    }
    
    @Override
    protected void applyStyleProperty(UIStyleProperty<?> property, Object value) {
        if (icon!=null) {
            if (property==StyleProps.BUTTON_ICON_TINT) icon.style(StyleProps.TINT, value);
            if (property==StyleProps.TRANSFORM_SCALEX) icon.style(property, value);
            if (property==StyleProps.TRANSFORM_SCALEY) icon.style(property, value);
        }
        super.applyStyleProperty(property, value);
    }
    
    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();
        
        if (icon!=null) style.set(StyleProps.BUTTON_ICON_TINT, icon.getTint());
        return style;
    }
    
    public UIButton icon(String iconName) {
        if (icon==null) return this;
        icon.texture(iconName);
        icon.style("visible", true);
        return this;
    }
}
