package strobeyworks.ui.components.input.notifications;

import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.platform.Transition;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFontManager;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIIcon.UIIconFitMode;
import strobeyworks.ui.primitives.UIRectFactory;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UIBanner extends UIRectangle {
    public enum UIBannerMode {
        SUCCESS,
        WARNING,
        ERROR,
        INFO,
        HELP
    }
    
    private final String[] icons = {
        "tick_circle"
    };
    
    private final UIColor[] colors = {
        UIColor.rgb(0.5f, 1f, 0.5f)
    };
    
    public UIBanner(UIBannerMode mode, String title, String message) {
        style("width", px(250));
        style("height", px(40));
        style("position", UIPositionMode.ABSOLUTE);
        style("offset-top", px(20));
        style("align-items", UIAlignItems.CENTER);
        
        style("color", UIColor.rgb(0.3f));
        style("corner-radius", new Vec4(0, 4, 4, 0));
        
        UIRectangle line = UIRectFactory.sized(px(2), pbh(1f));
        line.style("position", UIPositionMode.ABSOLUTE)
        .style("color", colors[mode.ordinal()]);
        
        UIIcon icon = new UIIcon(icons[mode.ordinal()]);
        icon.style("width", pch(0.5f))
        .style("height", pch(0.5f))
        .style("margin-left", px(15))
        .style("icon-fit-mode", UIIconFitMode.FIT)
        .style("tint", colors[mode.ordinal()]);
        
        UIRectangle right = new UIRectangle();
        right.style("box", UIBoxMode.FLEX)
        .style("min-height", pch(1f))
        .style("margin-left", px(20))
        .style("flow-direction", UIFlowDirection.COLUMN)
        .style("justify-content", UIJustifyContent.CENTER);
        
        UIText t1 = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 18f), title);
        t1.style("color", UIColor.rgb(0.9f));
        
        UIText t2 = new UIText(UIFontManager.getUIFont("RobotoMono-Medium.ttf", 14f), message);
        t2.style("color", UIColor.rgb(0.7f));
        
        right.addChild(t1);
        right.addChild(t2);
        
        addChild(line);
        addChild(icon);
        addChild(right);
    }
    
    public void fadeOut(int delay) {
        Transition t = new Transition(1f, delay);
        t.setCompletedAction(() -> {
            UIRenderer.getInstance().removeBanner(this);
        });
        
        UIStyle hidden = new UIStyle();
        hidden.set(StyleProps.OPACITY, 0f);
        transitionToStyle(hidden, t);
    }
}
