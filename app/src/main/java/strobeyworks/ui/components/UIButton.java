package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;

public class UIButton extends UIRectangle {
    
    @FunctionalInterface
    public interface UIButtonCallback {
        public void implement();
    }
    
    private UIIcon icon;
    
    private UIButtonCallback callback;
    
    public UIButton(UILength width, UILength height) {
        super(width, height);

        clickable(true);
        hoverable(true);
        
        box(UIBoxMode.FIXED);
        color(col(UIColors.TRANSPARENT));
        borderEnabled(true);
        borderColor(col(UIColors.GREEN));
        cornerRadius(10f);
        
        icon = new UIIcon(pcw(1.0f), pch(1.0f));
        icon.tint(col(UIColors.GREEN));
        icon.visible(false);
        addChild(icon);
    }

    @Override
    public void applyStyle(UIStyle style) {
        super.applyStyle(style);

        style.ifPresent(PrimitiveStyles.ICON_TINT, v -> icon.tint(v));
    }

    @Override
    public UIStyle captureStyle() {
        UIStyle style = super.captureStyle();

        style.set(PrimitiveStyles.ICON_TINT, icon.getTint());
        return style;
    }
    
    public UIButton icon(String iconName) {
        icon.texture(iconName)
        .visible(true);
        return this;
    }
    
    public UIButton clickedAction(UIButtonCallback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void gotHover(IOEvent event) {
        cacheStyle();
        applyHoverStyle();
    }
    
    @Override
    public void lostHover(IOEvent event) {
        applyCachedStyle();
    }

    @Override
    public void clicked(IOEvent event) {
        if (callback!=null) callback.implement();
    }
}
