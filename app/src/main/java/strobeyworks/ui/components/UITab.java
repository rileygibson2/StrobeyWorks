package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pbh;
import static strobeyworks.ui.core.UIPair.pbw;
import static strobeyworks.ui.core.UIPair.pch;
import static strobeyworks.ui.core.UIPair.pcw;
import static strobeyworks.ui.core.UIPair.px;
import static strobeyworks.ui.core.UIPair.sw;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.core.UIQuad;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Vec4;

public class UITab extends UIComponent implements IOSubscriber {
    
    private int numTabs;

    private UIRectangle elemLeft;
    private UIRectangle elemRight;
    
    public UITab(UIPair width, UIPair height, int numTabs) {
        super(width, height);
        this.numTabs = numTabs;

        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        flowWrap(false);
        padding(new UIQuad(px(5), px(0), px(0), px(0)));

        color(col(UIColors.GRAY_008));
        cornerRadius(new Vec4(20f, 20f, 0f, 0f));
        borderColor(col(UIColors.GREEN));
        borderBottom(false);
        
        for (int i=0; i<numTabs; i++) {
            UIRectangle rect = new UIRectangle(sw(0.12f), pbh(0.9f));
            addChild(rect);

            rect.cornerRadius(new Vec4(10f, 10f, 0f, 0f))
            .color(col(UIColors.TRANSPARENT))
            .borderColor(col(UIColors.GREEN))
            .borderBottom(false)
            .marginLeft(sw(0.005f))
            .marginTop(pbh(0.1f));
        }

        elemLeft = new UIRectangle(pcw(0f), pbh(0.2f));
        elemLeft.position(UIPositionMode.ABSOLUTE)
        .offsetTop(pbh(0.8f));
        elemLeft.color(col(UIColors.TRANSPARENT))
        //elemLeft.color(new Vec4(1f, 0f, 0f, 0.2f))
        .borderColor(col(UIColors.GREEN))
        .cornerRadius(new Vec4(0f, 0f, 20f, 0f))
        .borderTop(false)
        .borderLeft(false);
        addChild(elemLeft);

        elemRight = new UIRectangle(pcw(0f), pbh(0.2f));
        elemRight.position(UIPositionMode.ABSOLUTE)
        .offsetTop(pbh(0.8f));
        elemRight.color(col(UIColors.TRANSPARENT))
        //elemRight.color(new Vec4(1f, 0f, 0f, 0.2f))
        .borderColor(col(UIColors.GREEN))
        .cornerRadius(new Vec4(0f, 0f, 0f, 20f))
        .borderTop(false)
        .borderRight(false);
        addChild(elemRight);
    }

    @Override
    public void initialise() {
        setTab(1);
    }

    public void setTab(int tab) {
        if (tab<=0||tab>numTabs) return;
        Logger.debug("TAB "+tab);

        UIElement tabElem = getChildAtIndex(tab-1);
        elemLeft.width(px(tabElem.getMeasuredX()+2));
        elemRight.offsetLeft(px(tabElem.getMeasuredX()+tabElem.getMeasuredWidth()-1));
        elemRight.width(px(getMeasuredWidth()-tabElem.getMeasuredX()-tabElem.getMeasuredWidth()+1));

        for (int i=0; i<numTabs; i++) {
            getChildAtIndex(i).height(pbh(0.9f));
        }
        tabElem.height(pbh(0.7f));
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        handleIOEvent(event);
    }
    
    @Override
    public boolean handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :

            for (int i=0; i<numTabs; i++) {
                if (getChildAtIndex(i).containsResolved(event.getMouseX(), event.getMouseY())) setTab(i+1);
            }

            return true;
            
            default:
            return true;
        }
    }
}
