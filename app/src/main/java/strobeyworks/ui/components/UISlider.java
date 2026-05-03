package strobeyworks.ui.components;

import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIPair;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Vec3;

import static strobeyworks.ui.primitives.UIPair.ph;
import static strobeyworks.ui.primitives.UIPair.pw;
import static strobeyworks.ui.primitives.UIPair.px;
import static strobeyworks.ui.primitives.UIPair.sh;
import static strobeyworks.ui.primitives.UIPair.sw;

import strobeyworks.Animation;
import strobeyworks.SWMain;
import strobeyworks.logger.Logger;
import strobeyworks.ui.UIColors;
import strobeyworks.ui.UIIOEvent;

public class UISlider extends UIComponent {
    
    private UIRectangle rect;
    private UICircle circle;
    
    private float bounds = 0.95f;
    
    private boolean dragging;
    
    public UISlider(UIPair width, UIPair height) {
        super(width, height);
        
        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        flowWrap(false);
        padding(px(0));
        alignItems(UIAlignItems.CENTER);
        
        rect = new UIRectangle(pw(1f), ph(1f));
        rect.position(UIPositionMode.FLOW)
        .margin(px(0));
        addChild(rect);
        
        rect.borderColor(UIColors.color(UIColors.GREEN))
        .color(UIColors.color(UIColors.BLACK))
        .cornerRadius(20f);
        
        circle = new UICircle(ph(0.9f), ph(0.9f));
        circle.position(UIPositionMode.ABSOLUTE)
        .margin(px(0))
        .offsetTop(ph(0.05f))
        .offsetLeft(ph(0.05f));
        addChild(circle);
        
        circle.borderColor(UIColors.color(UIColors.GREEN))
        .color(UIColors.color(UIColors.BLACK))
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
    public boolean handleIOEvent(UIIOEvent event) {
        switch (event.getEventType()) {
            case LEFT_PRESS :
            Logger.debug("Slider circle aquired");
            setValueFromMouse(event.getMouseX());
            dragging = true;
            return false;
            
            case DRAG :
            if (dragging) {
                setValueFromMouse(event.getMouseX());
                return false;
            }
            return true;
            
            case LEFT_RELEASE :
            Logger.debug("Slider circle released");
            setValueFromMouse(event.getMouseX());
            dragging = false;
            return false;
            
            default:
            return true;
        }
    }
}
