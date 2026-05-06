package strobeyworks.ui.components.interactable;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pbh;
import static strobeyworks.ui.core.UIPair.pbw;
import static strobeyworks.ui.core.UIPair.pch;
import static strobeyworks.ui.core.UIPair.pcw;

import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIElement;
import strobeyworks.ui.primitives.UIRectangle;

public class UICheckBox extends UIInteractableComponent<Boolean, Boolean> implements IOSubscriber {
    
    private UIElement inner;
    
    public UICheckBox(UIPair width, UIPair height, boolean circular) {
        super(width, height, UIInteractableAdaptor.BOOLEAN_IDENTITY);
        
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
    protected Boolean getDefaultLocalValue() {
        return false;
    }

    @Override
    protected void implementLocalValueOnUI() {
        inner.visible(getLocalValue());
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        handleIOEvent(event);
    }
    
    @Override
    public boolean handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            setLocalValue(!getLocalValue());
            commitLocalValue();
            return false;
            
            default:
            return true;
        }
    }
}
