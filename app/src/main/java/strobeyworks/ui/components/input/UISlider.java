package strobeyworks.ui.components.input;

import static strobeyworks.ui.core.UIColors.col;
import static strobeyworks.ui.core.UIColors.colWithAlpha;
import static strobeyworks.ui.core.UILength.pph;
import static strobeyworks.ui.core.UILength.ppw;
import static strobeyworks.ui.core.UILength.px;

import strobeyworks.platform.IOEvent;
import strobeyworks.ui.core.UIColors;
import strobeyworks.ui.core.UILength;
import strobeyworks.ui.primitives.UICircle;
import strobeyworks.ui.primitives.UIRectangle;
import strobeyworks.ui.style.StyleProps;
import strobeyworks.ui.style.UIStyle;
import strobeyworks.utils.Utils;
import strobeyworks.utils.Vec4;

public class UISlider extends UIValueControl<Float, Float> {    
    
    private UICircle knob;
    private UIRectangle followingRect;
    private float bounds = 0.99f;
    
    public UISlider(UILength width, UILength height) {
        super(UIValueAdaptor.FLOAT_IDENTITY);
        
        style("width", width);
        style("height", height);
        wantsPointer(true);

        style("box", UIBoxMode.FIXED);
        style("flow-direction", UIFlowDirection.ROW);
        style("flow-wrap", false);
        style("padding-left", px(0));
        style("padding-right", px(0));
        style("padding-top", px(0));
        style("padding-bottom", px(0));
        style("align-items", UIAlignItems.CENTER);
        
        style("border-enabled", true);
        style("border-color", col(UIColors.GREEN));
        style("color", col(UIColors.GRAY_008));
        style("corner-radius", new Vec4(20f));
        
        UIStyle style = new UIStyle();
        style.set(StyleProps.TRANSFORM_SCALEX, 1.2f)
        .set(StyleProps.TRANSFORM_SCALEY, 1.2f);

        knob = new UICircle();
        knob.style("width", pph(1f))
        .style("height", pph(1f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-left", pph(0.05f));
        
        knob.style("border-color", col(UIColors.GREEN))
        .style("color", col(UIColors.GRAY_008))
        .style("oval", false)
        .style("border-enabled", true)
        .hoverStyle(style)
        .style("transition-duration", 0.2f)
        .hoverable(true);
        
        UICircle knobInner = new UICircle();
        knobInner.style("width", ppw(0.8f))
        .style("height", pph(0.8f))
        .style("position", UIPositionMode.ABSOLUTE)
        .style("offset-top", ppw(0.1f))
        .style("offset-left", pph(0.1f));
        
        knobInner.style("border-color", col(UIColors.GREEN))
        .style("color", col(UIColors.GRAY_008))
        .style("oval", false)
        .style("border-enabled", true);
        
        followingRect = new UIRectangle();
        followingRect.style("width", ppw(0f))
        .style("height", pph(1f))
        .style("position", UIPositionMode.ABSOLUTE);
        
        followingRect.style("color", col(UIColors.GREEN))
        .style("corner-radius", new Vec4(100f, 0f, 0f, 20f));
        
        addChild(followingRect);
        addChild(knob);
        knob.addChild(knobInner);
    }
    
    @Override
    protected Float getDefaultLocalValue() {
        return 0f;
    }
    
    @Override
    protected void implementLocalValueOnUI() {
        float value = getLocalValue();
        float offset = (1-bounds)*0.5f;
        float parentW = resolve(getWidth());
        float knobW = knob.resolve(knob.getWidth());
        float fullTravel = parentW-knobW;
        
        float cV = offset*fullTravel+value*(fullTravel*bounds);
        knob.style("offset-left", px(cV));
        
        float rV = value*fullTravel*bounds+knobW*0.5f;
        followingRect.style("width", px(rV));
        
        float a = Utils.smoothFalloffBefore(0.05f, value);
        followingRect.style("color", colWithAlpha(UIColors.GREEN, a));
    }
    
    private void setValueFromMouse(float mouseX) {
        float localX = mouseX - getLocalX();
        float value = localX / getLocalWidth();
        
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
