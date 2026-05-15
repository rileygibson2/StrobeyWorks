package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UILength.pch;
import static strobeyworks.ui.core.UILength.pcw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.PrimitiveStyles;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Utils;

public class UISlider extends UIValueControl<Float, Float> {    
    
    private UICircle circle;
    private UIRectangle fRect;
    private float bounds = 0.99f;
    
    public UISlider(UILength width, UILength height) {
        super(width, height, UIValueAdaptor.FLOAT_IDENTITY);
        
        wantsPointer(true);

        box(UIBoxMode.FIXED);
        flowDirection(UIFlowDirection.ROW);
        flowWrap(false);
        padding(px(0));
        alignItems(UIAlignItems.CENTER);
        
        borderEnabled(true);
        borderColor(col(UIColors.GREEN));
        color(col(UIColors.GRAY_008));
        cornerRadius(20f);
        
        UIStyle style = new UIStyle();
        style.set(PrimitiveStyles.TRANSFORM_SCALEX, 1.2f)
        .set(PrimitiveStyles.TRANSFORM_SCALEY, 1.2f);

        circle = new UICircle(pch(0.9f), pch(0.9f));
        circle.position(UIPositionMode.ABSOLUTE)
        .margin(px(0))
        .offsetTop(pch(0.05f))
        .offsetLeft(pch(0.05f));
        
        circle.borderEnabled(true)
        .borderColor(col(UIColors.GREEN))
        .color(col(UIColors.GRAY_008))
        .oval(false)
        .hoverStyle(style)
        .transitionDuration(0.2f)
        .hoverable(true);
        
        UICircle c2 = new UICircle(pcw(0.8f), pch(0.8f));
        c2.position(UIPositionMode.ABSOLUTE)
        .offsetTop(pcw(0.1f))
        .offsetLeft(pch(0.1f));
        
        c2.borderEnabled(true)
        .borderColor(col(UIColors.GREEN))
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
    protected Float getDefaultLocalValue() {
        return 0f;
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        float value = getLocalValue();
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
    }
    
    private void setValueFromMouse(float mouseX) {
        float localX = mouseX - getLayoutX();
        float value = localX / getLayoutWidth();
        
        setLocalValue(Math.max(Math.min(value, 1f), 0f));
        commitLocalValue();
    }

    @Override
    public void gotPointer(IOEvent event) {
        setValueFromMouse(event.getMouseX());
    }

    @Override
    public void lostPointer(IOEvent event) {
        setValueFromMouse(event.getMouseX());
    }
    
    @Override
    public void handleIOEvent(IOEvent event) {
        switch (event.getEventType()) {
            case DRAG :
            setValueFromMouse(event.getMouseX());
            break;
            
            default: return;
        }
    }
}
