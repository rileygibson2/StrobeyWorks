package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;
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
    
    private int numTabs;
    private UIRectangle follower;
    
    public UITab(UILength width, UILength height) {
        super();
        
        style("width", width);
        style("height", height);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("flow-wrap", false);
        style("align-items", UIAlignItems.CENTER);
        
        style("color", col(UIColors.GRAY_008));
        style("corner-radius", new Vec4(10f));
        style("border-enabled", true);
        style("border-color", col(UIColors.GREEN));
        style("border-thickness", px(1));
        
        follower = new UIRectangle();
        follower.style("width", sw(0.1f))
        .style("height", pbh(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("color", UIColors.colWithAlpha(UIColors.GREEN, 0.3f))
        .style("corner-radius", new Vec4(15f))
        .style("transition-duration", 0.2f);
        
        addChild(follower);
    }
    
    public void addTab(String title, UIFont font) {
        float t = numTabs;
        numTabs++;
        
        UIRectangle tab = new UIRectangle();
        tab.style("width", pbw(0.1f))
        .style("height", pbh(0.9f));
        
        tab.style("corner-radius", new Vec4(10f))
        .style("color", UIColors.col(UIColors.TRANSPARENT))
        .style("border-enabled", false)
        .style("margin-left", px(5))
        .style("transition-duration", 0.2f)
        .style("align-items", UIAlignItems.CENTER)
        .style("overflow-x", UIOverflowMode.HIDDEN)
        .focussable(true);
        
        UIIcon icon = new UIIcon("down_arrow");
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
            float x = t*tab.resolve(pbw(0.1f))+(t+1)*5;

            s = new UIStyle();
            s.set("offset-left", px(x))
            .set("width", px(w));
            follower.transitionToStyle(s, "animate");
        });
        
        tab.onLostFocus(event -> {
            text.style("opacity", 0f);
            tab.transitionToStyle(tab.getAuthoredStyle(), "focus");
        });
        
        tab.addChild(icon);
        tab.addChild(text);
        addChild(tab);
    }
    
    @Override
    public void initialise() {
        if (getChildCount()>0) {
            UIRenderer.getInstance().setFocussedElement(getChildAtIndex(0));
            setTab(0);
        }

        super.initialise();
    }
    
    public void setTab(int tab) {
        
        
        
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            
            for (int i=0; i<numTabs; i++) {
                if (getChildAtIndex(i).contains(event.getMouseX(), event.getMouseY())) setTab(i+1);
            }
            
            default: return;
        }
    }
}
