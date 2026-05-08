package strobeyworks.ui.components;

import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.core.UITexture;
import strobeyworks.ui.core.UITextureManager;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pch;
import static strobeyworks.ui.core.UIPair.pcw;
import static strobeyworks.ui.core.UIPair.px;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;

public class UIButton extends UIRectangle {
    
    @FunctionalInterface
    public interface UIButtonCallback {
        public void implement();
    }
    
    private UIIcon icon;
    
    private UIButtonCallback callback;
    
    public UIButton(UIPair width, UIPair height) {
        super(width, height);

        clickable(true);
        hoverable(true);
        
        box(UIBoxMode.FIXED);
        color(col(UIColors.TRANSPARENT));
        borderColor(col(UIColors.GREEN));
        cornerRadius(10f);
        
        icon = new UIIcon(pcw(1.0f), pch(1.0f));
        icon.tint(col(UIColors.GREEN));
        icon.visible(false);
        addChild(icon);
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
        Logger.debug("AAAAA");
        color(col(UIColors.GRAY_05));
    }
    
    @Override
    public void lostHover(IOEvent event) {
        
        Logger.debug("OOOOO");
        color(col(UIColors.TRANSPARENT));
    }

    @Override
    public void clicked(IOEvent event) {
        if (callback!=null) callback.implement();
    }
}
