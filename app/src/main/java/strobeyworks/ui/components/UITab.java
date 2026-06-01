package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.sh;
import static strobeyworks.ui.core.UILength.sw;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.core.UIRenderer;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UITab extends UIRectangle {
    
    private UIFont font;
    private int numTabs;
    private UIRectangle follower;
    private UIElement selectedTab;

    private UIElement tabBar;
    private UIElement content;
    
    public UITab(UILength width, UILength height, UIFont font) {
        super();
        
        this.font = font;

        style("width", width)
        .style("height", height)
        .style("color", col(UIColors.RED));
        
        // Tab bar
        tabBar = new UIRectangle();
        tabBar.style("width", pbw(1f))
        .style("height", pbh(0.11f))
        .style("box", UIBoxMode.FIXED)
        .style("flow-direction", UIFlowDirection.ROW)
        .style("flow-wrap", false)
        .style("align-items", UIAlignItems.CENTER)
        .style("color", col(UIColors.GRAY_008))
        .style("corner-radius", new Vec4(10f))
        .style("border-enabled", true)
        .style("border-color", col(UIColors.GREEN))
        .style("border-thickness", px(1))
        .style("padding-top", px(10))
        .style("padding-bottom", px(10))
        .style("z-index", 1);
        
        // Follower
        follower = new UIRectangle();
        follower.style("width", sw(0.1f))
        .style("height", pbh(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColors.colWithAlpha(UIColors.TRANSPARENT, 0.3f))
        .style("corner-radius", new Vec4(12f))
        .style("border-enabled", true)
        .style("border-color", col(UIColors.GREEN))
        .style("border-thickness", px(1))
        .style("transition-duration", 0.2f);

        // Content
        content = new UIRectangle();
        content.style("width", pbw(1f))
        .style("height", pbh(0.9f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pbh(0.1f))
        .style("color", col(UIColors.GRAY_008))
        .style("corner-radius", new Vec4(0f, 0f, 20f, 20f))
        .style("border-color", col(UIColors.GREEN))
        .style("border-thickness", px(1))
        .style("border-top", false)
        .style("border-enabled", true);
        
        tabBar.addChild(follower);
        addChild(tabBar);
        addChild(content);
    }
    
    public void addTab(String title, String iconName) {
        float t = numTabs;
        numTabs++;
        
        UIRectangle tab = new UIRectangle();
        tab.style("width", pbw(0.1f))
        .style("height", pbh(0.9f));
        
        tab.style("corner-radius", new Vec4(10f))
        .style("color", UIColors.col(UIColors.TRANSPARENT))
        .style("border-enabled", false)
        .style("transition-duration", 0.2f)
        .style("align-items", UIAlignItems.CENTER)
        .style("overflow-x", UIOverflowMode.HIDDEN)
        .focussable(true);

        if (t>0) tab.style("margin-left", px(5));
        
        UIIcon icon = new UIIcon(iconName);
        icon.style("width", ppw(0.8f))
        .style("height", pph(0.8f))
        .style("margin-left", ppw(0.1f))
        .style("tint", col(UIColors.GREEN));
        
        UIText text = new UIText(font, title);
        text.style("color", col(UIColors.GREEN))
        .style("margin-left", px(10))
        .style("opacity", 0f);
        
        icon.onInitialise(() -> {
            icon.freeze("width");
            icon.freeze("margin-left");
        });
        
        tab.onGotFocus(event -> {
            // Expand tab
            text.style("opacity", 1f);
            float w = tab.getChildContentBounds().getWidth()+5;
            
            UIStyle s = new UIStyle().set("width", px(w));
            tab.transitionToStyle(s, "focus");
            
            // Move follower
            float x = t*tab.resolve(pbw(0.1f))+t*5;
            
            s = new UIStyle();
            s.set("offset-left", px(x))
            .set("width", px(w));
            follower.transitionToStyle(s, "animate");
            
            setTab(tab);
        });
        
        tab.onLostFocus(event -> {
            text.style("opacity", 0f);
            tab.transitionToStyle(tab.getAuthoredStyle(), "focus");
            
            // Reset follower
            Logger.debug("Click outside of tab, resetting follower");
            float x = t*selectedTab.resolve(pbw(0.1f))+t*5;
            
            UIStyle s = new UIStyle();
            s.set("offset-left", px(x))
            .set("width", pbw(0.1f));
            follower.transitionToStyle(s, "animate");
        });
        
        tab.addChild(icon);
        tab.addChild(text);
        tabBar.addChild(tab);
    }
    
    @Override
    public void initialise() {
        if (getChildCount()>1) {
            UIElement firstTab = getChildAtIndex(1);
            UIRenderer.getInstance().setFocussedElement(firstTab);
        }
        
        super.initialise();
    }
    
    public void setTab(UIElement tab) {
        selectedTab = tab;
        
    }
}
