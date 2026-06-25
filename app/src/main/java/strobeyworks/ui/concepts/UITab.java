package strobeyworks.ui.concepts;

import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import java.util.ArrayList;
import java.util.List;

import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;

public class UITab extends UIRectangle {
    
    private UIFont font;
    private UIElement selectedTab;
    
    private UIElement tabBar;
    private UIRectangle follower;
    private UIElement content;
    
    private List<UIElement> tabRoots;
    
    public UITab(UIFont font) {
        this.font = font;
        tabRoots = new ArrayList<>();
        
        style("flow-direction", UIFlowDirection.COLUMN);
        
        // Tab bar
        tabBar = new UIRectangle();
        tabBar.style("width", pcw(1f))
        .style("height", px(40))
        .style("box", UIBoxMode.FIXED)
        .style("flow-direction", UIFlowDirection.ROW)
        .style("flow-wrap", false)
        .style("align-items", UIAlignItems.CENTER)
        .style("color", UIColor.rgb(0.12f))
        .style("z-index", 1);
        
        // Follower
        follower = new UIRectangle();
        follower.style("width", ppw(1f))
        .style("height", px(2))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColor.rgb(0.7f));
        
        // Content
        content = new UIRectangle();
        content.style("width", pcw(1f))
        .style("height", px(10))
        .style("grow", 1f);
        
        tabBar.addChild(follower);
        addChild(tabBar);
        addChild(content);
    }
    
    public void addTab(String title, UIElement tabRoot) {
        float t = tabRoots.size();
        
        UIRectangle tab = new UIRectangle();
        tab.style("box", UIBoxMode.FLEX)
        .style("margin-left", px(20))
        .style("align-items", UIAlignItems.CENTER)
        .style("min-height", pch(1.0f))
        .clickable(true);
        
        if (t==0) tab.style("margin-left", px(10));
        
        UIText text = new UIText(font, title);
        text.style("color", UIColor.rgb(0.7f))
        .style("opacity", 0.5f);
        
        tab.onClicked(event -> {
            setTab(tab);
        });
        
        tab.addChild(text);
        tabBar.addChild(tab);
        
        tabRoots.add(tabRoot);
    }
    
    public void setTab(int tabIndex) {
        UIElement tab = tabBar.getChildAtIndex(tabIndex+1);
        if (tab!=null) setTab(tab);
    }
    
    private void setTab(UIElement tab) {
        // Reset old tab text opacity
        if (selectedTab!=null) {
            selectedTab.getChildAtIndex(0).style("opacity", 0.5f);
        }
        selectedTab = tab;
        
        // Set text opacity
        tab.getChildAtIndex(0).style("opacity", 1f);
        
        // Move follower
        follower.style("width", px(tab.getLocalWidth()))
        .style("offset-left", px(tab.getLocalX()))
        .style("offset-top", pch(0.9f));
        
        
        content.removeAllContentChildren();
        int i = tabBar.getIndexOfChild(tab)-1;
        UIElement tabRoot = tabRoots.get(i);
        if (tabRoot!=null) content.addChild(tabRoot);
        
        if (tabRoot instanceof UITabbable) ((UITabbable)tabRoot).tabbedTo();
    }
}
