package strobeyworks.ui.components;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIPair.pch;
import static strobeyworks.ui.core.UIPair.pcw;
import static strobeyworks.ui.core.UIPair.px;

import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.platform.IOEvent.IOEventType;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UIPair;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Utils;

public class UISlider extends UIInteractableComponent<Float> implements IOSubscriber {
    
    private UICircle circle;
    private UIRectangle fRect;
    private float bounds = 0.99f;
    
    private boolean dragging;
    
    public UISlider(UIPair width, UIPair height) {
        super(width, height);
        
        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        flowWrap(false);
        padding(px(0));
        alignItems(UIAlignItems.CENTER);
        
        // Main box
        
        borderColor(col(UIColors.GREEN));
        color(col(UIColors.GRAY_008));
        cornerRadius(20f);
        
        // Following circle
        circle = new UICircle(pch(0.9f), pch(0.9f));
        circle.position(UIPositionMode.ABSOLUTE)
        .margin(px(0))
        .offsetTop(pch(0.05f))
        .offsetLeft(pch(0.05f));
        
        circle.borderColor(col(UIColors.GREEN))
        .color(col(UIColors.GRAY_008))
        .oval(false);

        UICircle c2 = new UICircle(pcw(0.8f), pch(0.8f));
        c2.position(UIPositionMode.ABSOLUTE)
        .offsetTop(pcw(0.1f))
        .offsetLeft(pch(0.1f));
        
        c2.borderColor(col(UIColors.GREEN))
        .color(col(UIColors.GRAY_008))
        .oval(false);

        fRect = new UIRectangle(pcw(0f), pch(1f));
        fRect.position(UIPositionMode.ABSOLUTE);
        
        fRect.color(col(UIColors.GREEN))
        .cornerRadius(100f, 0f, 0f, 20f);

        addChild(fRect);
        addChild(circle);
        circle.addChild(c2);
    }

    @Override
    public void initialise() {
        if (!isBound()) setValue(0f);
        else implementValueOnUI();
    }
    
    @Override
    protected void implementValueOnUI() {
        float value = getValue();
        float offset = (1-bounds)*0.5f;
        float parentW = resolveLocal(getWidth());
        float circW = circle.resolveLocal(circle.getWidth());
        float fullTravel = parentW-circW;
        
        float cV = offset*fullTravel+value*(fullTravel*bounds);
        circle.offsetLeft(px(cV));

        float rV = value*fullTravel*bounds+circW*0.5f;
        fRect.width(px(rV));

        float a = Utils.smoothFalloffBefore(0.05f, value);
        fRect.getColor().a = a;

        Math.pow(2, 2);
    }
    
    private void setValueFromMouse(float mouseX) {
        float localX = mouseX - getResolvedX();
        float value = localX / getResolvedWidth();
        
        setValue(Math.max(Math.min(value, 1f), 0f));
    }
    
    @Override
    public void receiveIOEvent(IOEvent event) {
        handleIOEvent(event);
    }
    
    @Override
    public boolean handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            if (dragging) return false;
            dragging = true;
            event.getIO().subscribe(IOEventType.DRAG, this);

            setValueFromMouse(event.getMouseX());
            return false;
            
            case DRAG :
            if (dragging) {
                setValueFromMouse(event.getMouseX());
                return false;
            }
            return true;
            
            case LEFT_RELEASE :
            if (!dragging) return false;
            dragging = false;
            event.getIO().unsubscribe(IOEventType.DRAG, this);
            
            setValueFromMouse(event.getMouseX());
            return false;
            
            default:
            return true;
        }
    }
}
