package strobeyworks.ui.components;

import static strobeyworks.ui.primitives.UIPair.pch;
import static strobeyworks.ui.primitives.UIPair.pcw;
import static strobeyworks.ui.primitives.UIPair.px;

import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.platform.IOSubscriber;
import strobeyworks.ui.UIColors;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIPair;
import strobeyworks.ui.primitives.UIRectangle;

public class UISlider extends UIComponent implements IOSubscriber {
    
    private UIRectangle rect;
    private UICircle circle;
    
    private float bounds = 0.99f;
    
    private boolean dragging;
    
    public UISlider(UIPair width, UIPair height) {
        super(width, height);
        
        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        flowWrap(false);
        padding(px(0));
        alignItems(UIAlignItems.CENTER);
        
        rect = new UIRectangle(pcw(1f), pch(1f));
        rect.position(UIPositionMode.FLOW)
        .margin(px(0));
        addChild(rect);
        
        rect.borderColor(UIColors.color(UIColors.GREEN))
        .color(UIColors.color(UIColors.TRANSPARENT))
        .cornerRadius(20f);
        
        circle = new UICircle(pch(0.9f), pch(0.9f));
        circle.position(UIPositionMode.ABSOLUTE)
        .margin(px(0))
        .offsetTop(pch(0.05f))
        .offsetLeft(pch(0.05f));
        addChild(circle);
        
        circle.borderColor(UIColors.color(UIColors.GREEN))
        .color(UIColors.color(UIColors.TRANSPARENT))
        .oval(false);
    }
    
    public UISlider setValue(float value) {
        value = Math.max(Math.min(value, 1f), 0f);
        
        float offset = (1-bounds)*0.5f;
        float rectW = rect.resolveLocal(rect.getWidth());
        float circW = circle.resolveLocal(circle.getWidth());
        float travel = rectW-circW;
        
        float v = offset*travel+value*(travel*bounds);
        circle.offsetLeft(px(v));
        return this;
    }
    
    private void setValueFromMouse(float mouseX) {
        float localX = mouseX - getResolvedX();
        float value = localX / getResolvedWidth();
        
        setValue(value);
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
            event.getIO().subscribe(this);

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
            event.getIO().unsubscribe(this);
            
            setValueFromMouse(event.getMouseX());
            return false;
            
            default:
            return true;
        }
    }
}
