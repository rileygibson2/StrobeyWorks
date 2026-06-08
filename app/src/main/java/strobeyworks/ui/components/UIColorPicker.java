package strobeyworks.ui.components;

import static strobeyworks.ui.core.UILength.px;
import static strobeyworks.ui.core.UILength.pbw;
import static strobeyworks.ui.core.UILength.pbh;

import strobeyworks.logger.Logger;
import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColor;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.utils.Bindable;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec3;
import strobeyworks.utils.Vec4;

public class UIColorPicker extends UIRectangle {
    
    private Bindable<UIColor> boundColor;
    
    private UICircle cursor;
    private UICircle inner;

    private float hue;
    private float sat;
    
    public UIColorPicker() {
        wantsPointer(true);
        hoverable(true);
        
        style("transition-duration", 0.3f);
        style("box", UIBoxMode.FIXED);
        style("color", UIColor.TRANSPARENT);
        style("border-enabled", true);
        style("border-color", UIColor.GREEN);
        style("corner-radius", new Vec4(10f));
        
        boundColor = Bindable.of(UIColor.WHITE);
        setHueSat();
        
        cursor = new UICircle();
        cursor.style("width", px(10))
        .style("height", px(10))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("justify-content", UIJustifyContent.CENTER)
        .style("align-items", UIAlignItems.CENTER)
        .style("color", UIColor.WHITE);

        inner = new UICircle();
        inner.style("width", px(8))
        .style("height", px(8))
        .style("color", UIColor.WHITE);
        
        addChild(cursor);
        cursor.addChild(inner);
    }
    
    @Override
    public void initialise() {
        super.initialise();
        repositionCursor();
    }
    
    public UIColorPicker bindColor(Bindable<UIColor> color) {
        this.boundColor = color;
        setHueSat();
        if (isInitialised()) repositionCursor();
        return this;
    }

    private void setHueSat() {
        UIColor col = boundColor.getValue();
        hue = col.getHue();
        sat = col.getSaturation();
    }
    
    private void repositionCursor() {
        UIColor rgb = boundColor.getValue();
        
        float w = getScreenWidth();
        float h = getScreenHeight();
        float cW = cursor.getScreenWidth();
        float cH = cursor.getScreenHeight();
        
        
        float oX = (w*(1-hue))-(cW/2);
        float oY = (h*(1-sat))-(cH/2);
        
        oX = Utils.clamp(0f, w-cW, oX);
        oY = Utils.clamp(0f, h-cH, oY);
        
        cursor.style("offset-left", px(oX))
        .style("offset-top", px(oY));
        
        inner.style("color", boundColor.getValue());
    }
    
    private void setValueFromMouse(float mouseX, float mouseY) {
        mouseX -= getScreenX();
        mouseY -= getScreenY();
        float x = Utils.clamp01(mouseX/getScreenWidth());
        float y = Utils.clamp01(mouseY/getScreenHeight());
        
        UIColor b = boundColor.getValue();
        b.setHue(1-x);
        b.setSaturation(1-y);
        boundColor.setValue(b);
        hue = 1-x;
        sat = 1-y;

        repositionCursor();
    }
    
    @Override
    public void gotPointer(IOEvent event) {
        setValueFromMouse(event.getMouseX(), event.getMouseY());
    }
    
    @Override
    public void lostPointer(IOEvent event) {
        setValueFromMouse(event.getMouseX(), event.getMouseY());
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG :
            setValueFromMouse(event.getMouseX(), event.getMouseY());
            break;
            
            default: return;
        }
    }
}
