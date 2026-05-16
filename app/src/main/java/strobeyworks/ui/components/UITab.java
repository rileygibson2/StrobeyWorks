package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pbh;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.sw;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Vec4;

public class UITab extends UIRectangle {
    
    private int numTabs;
    
    private UIRectangle elemLeft;
    private UIRectangle elemRight;
    
    public UITab(UILength width, UILength height, int numTabs) {
        super();
        this.numTabs = numTabs;
        
        style("width", width);
        style("height", height);
        clickable(true);
        
        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("flow-wrap", false);
        style("padding-top", px(5));
        
        style("color", col(UIColors.GRAY_008));
        style("corner-radius", new Vec4(20f, 20f, 0f, 0f));
        style("border-enabled", true);
        style("border-color", col(UIColors.GREEN));
        style("border-bottom", false);
        
        for (int i=0; i<numTabs; i++) {
            UIRectangle rect = new UIRectangle();
            rect.style("width", sw(0.12f))
            .style("height", pbh(0.9f));
            addChild(rect);
            
            rect.style("corner-radius", new Vec4(10f, 10f, 0f, 0f))
            .style("color", col(UIColors.TRANSPARENT))
            .style("border-color", col(UIColors.GREEN))
            .style("border-enabled", true)
            .style("border-bottom", false)
            .style("margin-left", sw(0.005f))
            .style("margin-top", pbh(0.1f));
        }
        
        elemLeft = new UIRectangle();
        elemLeft.style("width", pcw(0f))
        .style("height", pbh(0.2f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pbh(0.8f));
        elemLeft.style("color", col(UIColors.TRANSPARENT))
        //elemLeft.color(new Vec4(1f, 0f, 0f, 0.2f))
        .style("border-color", col(UIColors.GREEN))
        .style("corner-radius", new Vec4(0f, 0f, 20f, 0f))
        .style("border-enabled", true)
        .style("border-top", false)
        .style("border-left", false);
        addChild(elemLeft);
        
        elemRight = new UIRectangle();
        elemRight.style("width", pcw(0f))
        .style("height", pbh(0.2f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", pbh(0.8f));
        elemRight.style("color", col(UIColors.TRANSPARENT))
        //elemRight.color(new Vec4(1f, 0f, 0f, 0.2f))
        .style("border-color", col(UIColors.GREEN))
        .style("corner-radius", new Vec4(0f, 0f, 0f, 20f))
        .style("border-enabled", true)
        .style("border-top", false)
        .style("border-right", false);
        addChild(elemRight);
    }
    
    @Override
    public void initialise() {
        setTab(1);
    }
    
    public void setTab(int tab) {
        if (tab<=0||tab>numTabs) return;
        
        UIElement tabElem = getChildAtIndex(tab-1);
        elemLeft.style("width", px(tabElem.getLocalX()+2));
        elemRight.style("offset-left", px(tabElem.getLocalX()+tabElem.getLocalWidth()-1));
        elemRight.style("width", px(getLocalWidth()-tabElem.getLocalX()-tabElem.getLocalWidth()+1));
        
        for (int i=0; i<numTabs; i++) {
            getChildAtIndex(i).style("height", pbh(0.9f));
        }
        tabElem.style("height", pbh(0.7f));
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
