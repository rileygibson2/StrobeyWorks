package strobeyworks.ui.components;

import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Vec4;

public class UITab extends UIRectangle {
    
    private UIFont font;
    private UIElement selectedTab;

    private UIElement tabBar;
    private UIRectangle follower;
    private UIElement content;

    private List<UIElement> tabRoots;
    
    public UITab(UILength width, UILength height, UIFont font) {
        super();
        
        this.font = font;
        tabRoots = new ArrayList<>();

        style("width", width)
        .style("height", height);
        
        // Tab bar
        tabBar = new UIRectangle();
        tabBar.style("width", pbw(1f))
        .style("height", pbh(0.11f))
        .style("box", UIBoxMode.FIXED)
        .style("flow-direction", UIFlowDirection.ROW)
        .style("flow-wrap", false)
        .style("align-items", UIAlignItems.CENTER)
        .style("color", UIColor.gray008())
        .style("corner-radius", new Vec4(10f))
        .style("border-enabled", true)
        .style("border-color", UIColor.green())
        .style("border-thickness", px(1))
        .style("z-index", 1);
        
        // Follower
        follower = new UIRectangle();
        follower.style("width", ppw(1f))
        .style("height", pph(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.transparent().setAlpha(0.3f))
        .style("corner-radius", new Vec4(12f))
        .style("border-enabled", true)
        .style("border-color", UIColor.green())
        .style("border-thickness", px(1))
        .style("transition-duration", 0.2f);

        // Content
        content = new UIRectangle();
        content.style("width", pbw(1f))
        .style("height", pbh(0.9f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pbh(0.1f))
        .style("color", UIColor.gray008())
        .style("corner-radius", new Vec4(0f, 0f, 20f, 20f))
        .style("border-color", UIColor.green())
        .style("border-thickness", px(1))
        .style("border-top", false)
        .style("border-enabled", true);
        
        tabBar.addChild(follower);
        addChild(tabBar);
        addChild(content);
    }
    
    public void addTab(String title, String iconName, UIElement tabRoot) {
        float t = tabRoots.size();
        
        UIRectangle tab = new UIRectangle();
        tab.style("width", pbw(0.1f))
        .style("height", pbh(0.9f));
        
        tab.style("corner-radius", new Vec4(10f))
        .style("color", UIColor.transparent())
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
        .style("tint", UIColor.green());
        
        UIText text = new UIText(font, title);
        text.style("color", UIColor.green())
        .style("margin-left", px(10))
        .style("opacity", 0f);
        
        icon.onInitialise(() -> {
            icon.freeze("width");
            icon.freeze("margin-left");
        });
        
        tab.onGotFocus(event -> {
            // Expand tab
            text.style("opacity", 1f);
            float w = tab.getChildContentBounds().getWidth()+10;
            
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
            float x = t*selectedTab.resolve(pbw(0.1f))+t*5;
            
            UIStyle s = new UIStyle();
            s.set("offset-left", px(x))
            .set("width", pbw(0.1f));
            follower.transitionToStyle(s, "animate");
        });
        
        tab.addChild(icon);
        tab.addChild(text);
        tabBar.addChild(tab);

        tabRoots.add(tabRoot);
    }
    
    public void setTab(int tabIndex) {
        UIElement tab = tabBar.getChildAtIndex(tabIndex+1);
        if (tab!=null) setTab(tab);
    }

    private void setTab(UIElement tab) {
        selectedTab = tab;
        int i = tabBar.getChildIndex(tab)-1;
        
        content.removeAllChildren();
        UIElement tabRoot = tabRoots.get(i);
        if (tabRoot!=null) content.addChild(tabRoot);
    }
}
