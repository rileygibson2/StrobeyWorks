package strobeyworks.ui.components;

import static strobeyworks.ui.UIColors.col;
import static strobeyworks.ui.primitives.UIPair.pch;
import static strobeyworks.ui.primitives.UIPair.pcw;
import static strobeyworks.ui.primitives.UIPair.pbh;
import static strobeyworks.ui.primitives.UIPair.pbw;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.ui.UIColors;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIPair;
import strobeyworks.ui.primitives.UIRectangle;

public class UICheckBox extends UIInteractableComponent<Boolean> implements IOSubscriber {
    
    private UIElement inner;
    
    public UICheckBox(UIPair width, UIPair height, boolean circular) {
        super(width, height);
        this.value = false;
        
        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        alignItems(UIAlignItems.CENTER);
        borderColor(col(UIColors.GREEN));
        color(col(UIColors.TRANSPARENT));
        cornerRadius(10f);
        
        // Inner
        if (circular) {
            borderColor(col(UIColors.TRANSPARENT));

            UICircle c = new UICircle(pbw(1f), pbh(1f));
            c.position(UIPositionMode.ABSOLUTE);
            c.borderColor(col(UIColors.GREEN))
            .color(col(UIColors.TRANSPARENT));

            inner = new UICircle(pcw(0.8f), pch(0.8f));
            inner.position(UIPositionMode.ABSOLUTE)
            .offsetLeft(pcw(0.1f))
            .offsetTop(pch(0.1f));
            ((UICircle) inner).color(col(UIColors.GREEN));

            addChild(c);
            addChild(inner);
        }
        else {
            inner = new UIRectangle(pcw(0.8f), pch(0.8f));
            inner.position(UIPositionMode.ABSOLUTE)
            .offsetLeft(pcw(0.1f))
            .offsetTop(pch(0.1f));
            ((UIRectangle) inner).color(col(UIColors.GREEN))
            .cornerRadius(10f);

            addChild(inner);
        }
    }
    
    @Override
    public void initialise() {
        valueUpdated();
    }
    
    @Override
    public void valueUpdated() {
        inner.visible(value);
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        handleIOEvent(event);
    }
    
    @Override
    public boolean handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            userSetValue(!value);
            
            default:
            return true;
        }
    }
}
