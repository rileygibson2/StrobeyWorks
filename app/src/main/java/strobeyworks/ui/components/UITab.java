package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.sw;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIFont;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIIcon;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.primitives.UIText;
import strobeyworks.utils.Vec4;

public class UITab extends UIRectangle {
    
    private int numTabs;
    private UIRectangle follower;
    
    public UITab(UILength width, UILength height) {
        super();
        
        style("width", width);
        style("height", height);
        clickable(true);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("flow-wrap", false);
        style("align-items", UIAlignItems.CENTER);
        
        style("color", col(UIColors.GRAY_01));
        style("corner-radius", new Vec4(10f));
        style("border-enabled", true);
        style("border-color", col(UIColors.GREEN));

        follower = new UIRectangle();
        follower.style("width", sw(0.1f))
        .style("height", pbh(0.9f))
        .style("color", col(UIColors.TRANSPARENT))
        .style("border-enabled", true)
        .style("border-color", col(UIColors.GREEN));
    }
    
    public void addTab(String title, UIFont font) {
        numTabs++;
        
        UIRectangle tab = new UIRectangle();
        tab.style("width", sw(0.1f))
        .style("height", pbh(0.9f));
        
        tab.style("corner-radius", new Vec4(10f))
        .style("color", UIColors.col(UIColors.TRANSPARENT))
        .style("border-enabled", false)
        .style("margin-left", sw(0.005f))
        .style("transition-duration", 0.2f)
        .style("align-items", UIAlignItems.CENTER)
        .style("overflow-x", UIOverflowMode.HIDDEN);
        
        tab.hoverStyle("width", sw(0.2f))
        .hoverable(true);
        
        UIIcon icon = new UIIcon("down_arrow");
        icon.style("width", ppw(0.8f))
        .style("height", pph(0.8f))
        .style("margin-left", ppw(0.1f));
        
        UIText text = new UIText(font, title);
        text.style("color", col(UIColors.WHITE))
        .style("margin-left", px(10))
        .style("visible", false)
        .hoverStyle("visible", true);
        
        icon.onInitialise(() -> {
            icon.freezeWidth();
            Logger.debug("eeeee");
        });

        tab.onGotHover(event -> {
            for (UIElement c : tab.getAllChildren()) c.gotHover(event);
        });
        
        tab.onLostHover(event -> {
            for (UIElement c : tab.getAllChildren()) c.lostHover(event);
        });
        
        tab.addChild(icon);
        tab.addChild(text);
        addChild(tab);
        
    }
    
    @Override
    public void initialise() {
        setTab(1);
        super.initialise();
    }
    
    public void setTab(int tab) {
        if (tab<=0||tab>numTabs) return;
        
        
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
